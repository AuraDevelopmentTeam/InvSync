package world.jnc.invsync.event;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.zip.DataFormatException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.property.item.FoodRestorationProperty;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.entity.ChangeEntityExperienceEvent;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.event.entity.living.humanoid.ChangeGameModeEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;
import org.spongepowered.api.event.item.inventory.UseItemStackEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.scheduler.Task;
import world.jnc.invsync.InventorySync;
import world.jnc.invsync.util.database.DataSource;
import world.jnc.invsync.util.serializer.PlayerSerializer;

@RequiredArgsConstructor
public class PlayerEvents implements AutoCloseable {
  private final DataSource dataSource;
  private final Map<UUID, Task> waitingPlayers = new HashMap<>();
  private final SortedSet<UUID> successfulJoined = new TreeSet<>();

  @Listener
  public void onPlayerJoin(ClientConnectionEvent.Join event) {
    final @NonNull Player player = event.getTargetEntity();
    final UUID uuid = player.getUniqueId();

    synchronized (waitingPlayers) {
      Task task =
          Task.builder()
              .execute(
                  new WaitingForPreviousServerToFinish(
                      player, InventorySync.getConfig().getGeneral().getMaxWait()))
              .intervalTicks(1)
              .submit(InventorySync.getInstance());

      waitingPlayers.put(uuid, task);
    }
  }

  @Listener(order = Order.POST)
  public void onPlayerJoinComplete(ClientConnectionEvent.Join event) {
    final @NonNull Player player = event.getTargetEntity();

    successfulJoined.add(player.getUniqueId());

    if (InventorySync.getConfig().getGeneral().getDebug()) {
      InventorySync.getLogger()
          .info("Player " + DataSource.getPlayerString(player) + " has joined successfully.");
    }
  }

  @Listener
  public void onPlayerLeave(ClientConnectionEvent.Disconnect event) throws IOException {
    final @NonNull Player player = event.getTargetEntity();
    final UUID uuid = player.getUniqueId();

    // If it can't be removed, it was not in the set, which means the player hasn't fully joined
    // yet. Therefore we don't save the player.
    if (!successfulJoined.remove(uuid)) {
      final boolean debug = InventorySync.getConfig().getGeneral().getDebug();
      final Logger logger = InventorySync.getLogger();
      final String message =
          "Serializing data of "
              + DataSource.getPlayerString(player)
              + " has been cancelled, due to them not having joined fully.";

      if (debug) {
        logger.warn(message);
      } else {
        logger.debug(message);
      }

      // Abort saving here
      return;
    }

    savePlayer(player, true);

    synchronized (waitingPlayers) {
      if (waitingPlayers.containsKey(uuid)) {
        waitingPlayers.remove(uuid).cancel();
      }
    }
  }

  @Listener
  public void onPlayerItemPickUp(ChangeInventoryEvent.Pickup event, @First Player player) {
    synchronized (waitingPlayers) {
      if (waitingPlayers.containsKey(player.getUniqueId())) {
        event.setCancelled(true);
      }
    }
  }

  @Listener
  public void onPlayerExperiencePickUp(ChangeEntityExperienceEvent event, @First Player player) {
    synchronized (waitingPlayers) {
      if (waitingPlayers.containsKey(player.getUniqueId())) {
        event.setCancelled(true);
      }
    }
  }

  @Listener
  public void onPlayerEat(UseItemStackEvent.Finish event, @First Player player) {
    if (!event.getItemStackInUse().getProperty(FoodRestorationProperty.class).isPresent()) return;

    synchronized (waitingPlayers) {
      if (waitingPlayers.containsKey(player.getUniqueId())) {
        event.setCancelled(true);
      }
    }
  }

  @Listener
  public void onPlayerDamage(DamageEntityEvent event) {
    Entity player = event.getTargetEntity();

    if (!(player instanceof Player)) return;

    synchronized (waitingPlayers) {
      if (waitingPlayers.containsKey(player.getUniqueId())) {
        event.setCancelled(true);
      }
    }
  }

  @Listener
  public void onPlayerChangeGamemode(ChangeGameModeEvent event, @First Player player) {
    // TODO: Use Cause once SpongeCommon#1355 is fixed
    synchronized (waitingPlayers) {
      if (waitingPlayers.containsKey(player.getUniqueId())) {
        event.setCancelled(true);
      }
    }
  }

  public void saveAllPlayers() throws IOException, DataFormatException {
    for (Player player : Sponge.getGame().getServer().getOnlinePlayers()) {
      // This is either called when reloading (we don't want to clear the cache)
      // or when shutting down, where it doesn't matter
      savePlayer(player, false);
    }

    InventorySync.getLogger().debug("Saved all player inventories");
  }

  @Override
  public void close() throws IOException, DataFormatException {
    saveAllPlayers();
  }

  private void loadPlayer(@NonNull Player player)
      throws ClassNotFoundException, IOException, DataFormatException {
    Optional<byte[]> result = dataSource.loadInventory(player);

    if (result.isPresent()) {
      PlayerSerializer.deserializePlayer(player, result.get());
    } else {
      savePlayer(player, false);
    }

    dataSource.setActive(player);
  }

  private void savePlayer(@NonNull Player player, boolean removeFromCache) throws IOException {
    dataSource.saveInventory(player, PlayerSerializer.serializePlayer(player, removeFromCache));
  }

  private class WaitingForPreviousServerToFinish implements Consumer<Task> {
    private final Player player;
    private final long endTime;

    public WaitingForPreviousServerToFinish(Player player, long maxWait) {
      this.player = player;
      endTime = System.currentTimeMillis() + maxWait;
    }

    @Override
    public void accept(Task task) {
      boolean timeOk = endTime > System.currentTimeMillis();
      UUID uuid = player.getUniqueId();

      if (dataSource.isActive(player) && timeOk) return;

      try {
        if (!timeOk) {
          InventorySync.getLogger()
              .warn(
                  "Loading player "
                      + DataSource.getPlayerString(player)
                      + " failed because the previous server did not finish writing the data in time. Will synchronize anyway!");
          InventorySync.getLogger().info("Try increasing global.maxWait in the config");
        }

        synchronized (waitingPlayers) {
          waitingPlayers.remove(uuid);
        }

        loadPlayer(player);
      } catch (ClassNotFoundException | IOException | DataFormatException e) {
        InventorySync.getLogger()
            .warn("Loading player " + DataSource.getPlayerString(player) + " failed!", e);
      } finally {
        synchronized (waitingPlayers) {
          if (waitingPlayers.containsKey(uuid)) {
            waitingPlayers.remove(uuid);
          }
        }

        task.cancel();
      }
    }
  }
}

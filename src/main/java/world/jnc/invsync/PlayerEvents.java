package world.jnc.invsync;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.zip.DataFormatException;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.scheduler.Task;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import world.jnc.invsync.config.Config;
import world.jnc.invsync.util.InventorySerializer;

@RequiredArgsConstructor
public class PlayerEvents implements AutoCloseable {
	private final DataSource dataSource;
	private Map<UUID, Task> waitingPlayers = new HashMap<>();

	@Listener
	public void onPlayerJoin(ClientConnectionEvent.Join event)
			throws IOException, ClassNotFoundException, DataFormatException {
		@NonNull
		Player player = event.getTargetEntity();
		UUID uuid = player.getUniqueId();

		synchronized (waitingPlayers) {
			Task task = Task.builder()
					.execute(new WaitingForOtherServerToFinish(player, Config.Values.Global.getMaxWait()))
					.intervalTicks(1).submit(InventorySync.getInstance());

			waitingPlayers.put(uuid, task);
		}
	}

	@Listener
	public void onPlayerLeave(ClientConnectionEvent.Disconnect event) throws IOException {
		@NonNull
		Player player = event.getTargetEntity();
		UUID uuid = player.getUniqueId();

		savePlayer(player);

		synchronized (waitingPlayers) {
			if (waitingPlayers.containsKey(uuid)) {
				waitingPlayers.remove(uuid).cancel();
			}
		}
	}

	@Listener
	public void onItemPickUp(ChangeInventoryEvent.Pickup event, @First Player player) {
		synchronized (waitingPlayers) {
			if (waitingPlayers.containsKey(player.getUniqueId())) {
				event.setCancelled(true);
			}
		}
	}

	public void saveAllPlayers() throws IOException, DataFormatException {
		for (Player player : Sponge.getGame().getServer().getOnlinePlayers()) {
			savePlayer(player);
		}

		InventorySync.getLogger().debug("Saved all player inventories");
	}

	@Override
	public void close() throws IOException, DataFormatException {
		saveAllPlayers();
	}

	private void loadPlayer(@NonNull Player player) throws ClassNotFoundException, IOException, DataFormatException {
		Optional<byte[]> result = dataSource.loadInventory(player);

		if (result.isPresent()) {
			InventorySerializer.deserializePlayer(player, result.get());
		} else {
			savePlayer(player);
		}

		dataSource.setActive(player);
	}

	private void savePlayer(@NonNull Player player) throws IOException {
		dataSource.saveInventory(player, InventorySerializer.serializePlayer(player));
	}

	private class WaitingForOtherServerToFinish implements Consumer<Task> {
		private final Player player;
		private final long endTime;

		public WaitingForOtherServerToFinish(Player player, int maxWait) {
			this.player = player;
			endTime = System.currentTimeMillis() + maxWait;
		}

		@Override
		public void accept(Task task) {
			if (dataSource.isActive(player) && (endTime > System.currentTimeMillis()))
				return;

			try {
				loadPlayer(player);

				synchronized (waitingPlayers) {
					waitingPlayers.remove(player.getUniqueId());
				}

				task.cancel();
			} catch (ClassNotFoundException | IOException | DataFormatException e) {
				InventorySync.getLogger().warn("Loading player " + DataSource.getPlayerString(player) + " failed!", e);
			}
		}

	}
}

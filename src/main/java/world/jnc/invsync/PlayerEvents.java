package world.jnc.invsync;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.zip.DataFormatException;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.scheduler.Task;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import world.jnc.invsync.util.InventorySerializer;
import world.jnc.invsync.util.Pair;

@RequiredArgsConstructor
public class PlayerEvents implements AutoCloseable {
	private final DataSource dataSource;
	private List<UUID> waitingPlayers = new LinkedList<>();

	@Listener
	public void onPlayerJoin(ClientConnectionEvent.Join event)
			throws IOException, ClassNotFoundException, DataFormatException {
		@NonNull
		Player player = event.getTargetEntity();
		UUID uuid = player.getUniqueId();

		synchronized (waitingPlayers) {
			waitingPlayers.add(uuid);
		}

		Task.builder().execute(() -> {
			try {
				loadPlayer(player);

				synchronized (waitingPlayers) {
					waitingPlayers.remove(uuid);
				}
			} catch (ClassNotFoundException | IOException | DataFormatException e) {
				InventorySync.getLogger().warn("Loading player " + DataSource.getPlayerString(player) + " failed!", e);
			}
		}).delay(100, TimeUnit.MILLISECONDS).submit(InventorySync.getInstance());
	}

	@Listener
	public void onPlayerLeave(ClientConnectionEvent.Disconnect event) throws IOException, DataFormatException {
		savePlayer(event.getTargetEntity());
	}

	@Listener
	public void onItemPickUp(ChangeInventoryEvent.Pickup event, @First Player player) {
		synchronized (waitingPlayers) {
			if (waitingPlayers.contains(player.getUniqueId())) {
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
		@NonNull
		Inventory inventory = player.getInventory();
		@NonNull
		Inventory enderInventory = player.getEnderChestInventory();

		Optional<Pair<byte[], byte[]>> result = dataSource.loadInventory(player);

		if (result.isPresent()) {
			Pair<byte[], byte[]> resultPair = result.get();

			InventorySerializer.deserializeInventory(resultPair.getLeft(), inventory);
			InventorySerializer.deserializeInventory(resultPair.getRight(), enderInventory);
		}
	}

	private void savePlayer(@NonNull Player player) throws IOException, DataFormatException {
		@NonNull
		Inventory inventory = player.getInventory();
		@NonNull
		Inventory enderInventory = player.getEnderChestInventory();

		dataSource.saveInventory(player, InventorySerializer.serializeInventory(inventory),
				InventorySerializer.serializeInventory(enderInventory));
	}
}

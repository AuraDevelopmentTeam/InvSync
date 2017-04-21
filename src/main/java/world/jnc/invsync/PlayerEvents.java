package world.jnc.invsync;

import java.io.IOException;
import java.util.zip.DataFormatException;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.item.inventory.Inventory;

import lombok.NonNull;
import world.jnc.invsync.util.InventorySerializer;

public class PlayerEvents {
	@Listener
	public void onPlayerJoin(ClientConnectionEvent.Join event)
			throws IOException, ClassNotFoundException, DataFormatException {
		@NonNull
		Player player = event.getTargetEntity();
		@NonNull
		Inventory inventory = player.getInventory();
		@NonNull
		Inventory enderInventory = player.getEnderChestInventory();

		byte[] inventoryBytes = InventorySerializer.serializeInventory(inventory);
		byte[] enderInventoryBytes = InventorySerializer.serializeInventory(enderInventory);

		InventorySync.getLogger().info(new String(inventoryBytes));
		InventorySync.getLogger().info(new String(enderInventoryBytes));

		InventorySerializer.deserializeInventory(inventoryBytes, inventory);
		InventorySerializer.deserializeInventory(enderInventoryBytes, enderInventory);
	}

	@Listener
	public void onPlayerLeave(ClientConnectionEvent.Disconnect event) {
		// TODO
	}
}

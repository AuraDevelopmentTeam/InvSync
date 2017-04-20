package world.jnc.invsync;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;

public class PlayerEvents {
	@Listener
	public void onPlayerJoin(ClientConnectionEvent.Join event) {
		Player player = event.getTargetEntity();
		
		InventorySync.getLogger().info(player.getInventory().toString());
	}

	@Listener
	public void onPlayerLeave(ClientConnectionEvent.Disconnect event) {
		;
	}
}

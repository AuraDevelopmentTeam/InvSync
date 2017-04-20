package world.jnc.invsync;

import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.plugin.Plugin;

import com.google.inject.Inject;

import lombok.Getter;

@Plugin(id = "invsync", name = "Inventory Sync", version = "@version@", description = "This plugin synchronizes the player inventory with a database", authors = {
		"The_BrainStone" })
public class InventorySync {
	@Inject
	@Getter
	private static Logger logger;

	@Listener
	public void init(GameInitializationEvent event) {
		Sponge.getEventManager().registerListeners(this, new PlayerEvents());
	}

	@Listener
	public void reload(GameReloadEvent event) {
		// Do reload stuff
	}
}

package world.jnc.invsync;

import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.Plugin;

import com.google.inject.Inject;

import lombok.NonNull;

@Plugin(id = "invsync", name = "Inventory Sync", version = "@version@", description = "This plugin synchronizes the player inventory with a database", authors = {
		"The_BrainStone" })
public class InventorySync {
	@NonNull
	private static InventorySync instance = null;

	@Inject
	@NonNull
	private Logger logger;

	public static Logger getLogger() {
		return instance.logger;
	}

	@Listener
	public void preInit(GamePreInitializationEvent event) {
		instance = this;
	}

	@Listener
	public void init(GameInitializationEvent event) {
		Sponge.getEventManager().registerListeners(this, new PlayerEvents());

		logger.info("Loaded successfully!");
	}

	@Listener
	public void reload(GameReloadEvent event) {
		// Do reload stuff
	}
}

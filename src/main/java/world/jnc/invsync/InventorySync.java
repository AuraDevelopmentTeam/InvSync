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

@Plugin(id = InventorySync.ID, name = InventorySync.NAME, version = InventorySync.VERSION, description = InventorySync.DESCRIPTION, authors = {
		InventorySync.AUTHOR })
public class InventorySync {
	public static final String ID = "invsync";
	public static final String NAME = "Inventory Sync";
	public static final String VERSION = "@version@";
	public static final String DESCRIPTION = "This plugin synchronizes the player inventory with a database";
	public static final String AUTHOR = "The_BrainStone";

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
		if (VERSION.contains("SNAPSHOT")) {
			logger.warn("WARNING! This is a snapshot version!");
			logger.warn("Use at your own risk!");
		}

		Sponge.getEventManager().registerListeners(this, new PlayerEvents());

		logger.info("Loaded successfully!");
	}

	@Listener
	public void reload(GameReloadEvent event) {
		// Do reload stuff
	}
}

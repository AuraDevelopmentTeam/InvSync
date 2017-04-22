package world.jnc.invsync;

import java.nio.file.Path;

import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.Plugin;

import com.google.inject.Inject;

import lombok.Getter;
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
	@Getter
	private static InventorySync instance = null;

	@Inject
	@NonNull
	private Logger logger;
	@Inject
	@DefaultConfig(sharedRoot = false)
	@NonNull
	private Path configFile;
	@Inject
	@ConfigDir(sharedRoot = false)
	@NonNull
	private Path configDir;
	@NonNull
	private Config config;

	public static Logger getLogger() {
		return instance.logger;
	}

	public static Path getConfigFile() {
		return instance.configFile;
	}

	public static Path getConfigDir() {
		return instance.configDir;
	}

	public static Config getConfig() {
		return instance.config;
	}

	@Listener
	public void preInit(GamePreInitializationEvent event) {
		instance = this;
	}

	@Listener
	public void init(GameInitializationEvent event) {
		logger.info("Initializing " + NAME + " Version " + VERSION);

		if (VERSION.contains("SNAPSHOT")) {
			logger.warn("WARNING! This is a snapshot version!");
			logger.warn("Use at your own risk!");
		}

		config = new Config(this, configFile, configDir);
		config.load();

		Sponge.getEventManager().registerListeners(this, new PlayerEvents());
		logger.debug("Registered events");

		logger.info("Loaded successfully!");
	}

	@Listener
	public void reload(GameReloadEvent event) {
		config.load();

		// TODO update more stuff

		logger.info("Reloaded successfully!");
	}
}

package world.jnc.invsync;

import java.nio.file.Path;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePostInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStoppingEvent;
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
	@NonNull
	private DataSource dataSource;

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

	public static DataSource getDataSource() {
		return instance.dataSource;
	}

	@Listener
	public void preInit(GamePreInitializationEvent event) {
		instance = this;
	}

	@Listener
	public void init(GameInitializationEvent event) throws SQLException {
		logger.info("Initializing " + NAME + " Version " + VERSION);

		if (VERSION.contains("SNAPSHOT")) {
			logger.warn("WARNING! This is a snapshot version!");
			logger.warn("Use at your own risk!");
		}

		config = new Config(this, configFile, configDir);
		config.load();

		dataSource = new DataSource();

		Sponge.getEventManager().registerListeners(this, new PlayerEvents());
		logger.debug("Registered events");

		logger.info("Loaded successfully!");
	}

	public void close(GameStoppingEvent event) {
		logger.info("Shutting down " + NAME + " Version " + VERSION);

		Sponge.getEventManager().unregisterPluginListeners(this);
		logger.debug("Unregistered events");

		dataSource = null;
		logger.debug("Closed database connection");

		config = null;
		logger.debug("Unloaded config");

		logger.info("Unloaded successfully!");

		logger = null;
	}

	@Listener
	public void reload(GameReloadEvent event) throws SQLException {
		Cause cause = Cause.source(this).build();

		// Unregistering everything
		GameStoppingEvent gameStoppingEvent = SpongeEventFactory.createGameStoppingEvent(cause);
		Sponge.getEventManager().post(gameStoppingEvent);

		// Starting over
		GamePreInitializationEvent gamePreInitializationEvent = SpongeEventFactory
				.createGamePreInitializationEvent(cause);
		Sponge.getEventManager().post(gamePreInitializationEvent);
		GameInitializationEvent gameInitializationEvent = SpongeEventFactory.createGameInitializationEvent(cause);
		Sponge.getEventManager().post(gameInitializationEvent);
		GamePostInitializationEvent gamePostInitializationEvent = SpongeEventFactory
				.createGamePostInitializationEvent(cause);
		Sponge.getEventManager().post(gamePostInitializationEvent);

		logger.info("Reloaded successfully!");
	}
}

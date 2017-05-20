package world.jnc.invsync;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

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
import world.jnc.invsync.config.Config;
import world.jnc.invsync.event.PlayerEvents;

@Plugin(id = InventorySync.ID, name = InventorySync.NAME, version = InventorySync.VERSION, description = InventorySync.DESCRIPTION, url = InventorySync.URL, authors = {
		InventorySync.AUTHOR })
public class InventorySync {
	public static final String ID = "invsync";
	public static final String NAME = "Inventory Sync";
	public static final String VERSION = "@version@";
	public static final String DESCRIPTION = "This plugin synchronizes the player inventory with a database";
	public static final String URL = "https://github.com/BrainStone/InvSync";
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
	private List<AutoCloseable> eventListeners = new LinkedList<>();

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
		if (VERSION.contains("development")) {
			logger.info("This is a unreleased development version!");
			logger.info("Things might not work properly!");
		}

		config = new Config(this, configFile, configDir);
		config.load();

		dataSource = new DataSource();

		addEventListener(new PlayerEvents(dataSource));
		logger.debug("Registered events");

		logger.info("Loaded successfully!");
	}

	public void postInit(GamePostInitializationEvent event) {
		// Nothing
	}

	@Listener
	public void reload(GameReloadEvent event) throws Exception {
		Cause cause = Cause.source(this).build();

		// Unregistering everything
		GameStoppingEvent gameStoppingEvent = SpongeEventFactory.createGameStoppingEvent(cause);
		stop(gameStoppingEvent);

		// Starting over
		GamePreInitializationEvent gamePreInitializationEvent = SpongeEventFactory
				.createGamePreInitializationEvent(cause);
		preInit(gamePreInitializationEvent);
		GameInitializationEvent gameInitializationEvent = SpongeEventFactory.createGameInitializationEvent(cause);
		init(gameInitializationEvent);
		GamePostInitializationEvent gamePostInitializationEvent = SpongeEventFactory
				.createGamePostInitializationEvent(cause);
		postInit(gamePostInitializationEvent);

		logger.info("Reloaded successfully!");
	}

	@Listener
	public void stop(GameStoppingEvent event) throws Exception {
		logger.info("Shutting down " + NAME + " Version " + VERSION);

		removeEventListeners();
		logger.debug("Unregistered events");

		dataSource = null;
		logger.debug("Closed database connection");

		config = null;
		logger.debug("Unloaded config");

		logger.info("Unloaded successfully!");
	}

	private void addEventListener(AutoCloseable listener) {
		eventListeners.add(listener);

		Sponge.getEventManager().registerListeners(this, listener);
	}

	private void removeEventListeners() throws Exception {
		for (AutoCloseable listener : eventListeners) {
			Sponge.getEventManager().unregisterListeners(listener);

			listener.close();
		}

		eventListeners.clear();
	}
}

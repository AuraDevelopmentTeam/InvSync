package world.jnc.invsync;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.NoSuchElementException;

import org.spongepowered.api.Sponge;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import world.jnc.invsync.util.DatabaseConnection;

public class Config {
	public static final String[] validStorageEngines = new String[] { "h2", "mysql" };

	@NonNull
	private final InventorySync instance;
	@NonNull
	@Getter
	private final Path configFile;
	@NonNull
	@Getter
	private final Path configDir;
	@NonNull
	private ConfigurationLoader<CommentedConfigurationNode> loader;
	@NonNull
	private ConfigurationNode rootNode;

	public Config(InventorySync instance, Path configFile, Path configDir) {
		this.instance = instance;
		this.configFile = configFile;
		this.configDir = configDir;
	}

	public void load() {
		if (!configFile.toFile().exists()) {
			try {
				Sponge.getAssetManager().getAsset(instance, "invsync.conf").get().copyToFile(configFile);
			} catch (IOException | NoSuchElementException e) {
				InventorySync.getLogger().error("Could not load default config!", e);

				return;
			}
		}

		loader = HoconConfigurationLoader.builder().setPath(configFile).build();

		try {
			rootNode = loader.load();
		} catch (IOException e) {
			InventorySync.getLogger().error("Config could not be loaded!", e);

			return;
		}

		ConfigurationNode storage = rootNode.getNode("storage");
		Values.storageEngine = storage.getNode("storageEngine").getString(validStorageEngines[0]);

		if (!Arrays.asList(validStorageEngines).contains(Values.storageEngine)) {
			InventorySync.getLogger().warn("Invalid storage engine in config: \"" + Values.storageEngine
					+ "\"! Defaulting to \"" + validStorageEngines[0] + "\"!");

			Values.storageEngine = validStorageEngines[0];
		}

		ConfigurationNode h2 = storage.getNode("h2");
		Values.H2.databaseFile = configDir.resolve(h2.getNode("databaseFile").getString("inventoryStorage.db"));

		ConfigurationNode mySQL = storage.getNode("MySQL");
		Values.MySQL.host = mySQL.getNode("host").getString("localhost");
		Values.MySQL.port = mySQL.getNode("port").getInt(DatabaseConnection.DEFAULT_MYSQL_PORT);
		Values.MySQL.database = mySQL.getNode("database").getString("invsync");
		Values.MySQL.user = mySQL.getNode("user").getString("invsync");
		Values.MySQL.password = mySQL.getNode("password").getString("sup3rS3cur3Pa55w0rd!");
		Values.MySQL.tablePrefix = mySQL.getNode("tablePrefix").getString("invsync_");

		if (Values.MySQL.port < 1) {
			InventorySync.getLogger().warn("MySQL port too low: " + Values.MySQL.port + "! Defaulting to 1!");

			Values.MySQL.port = 1;
		} else if (Values.MySQL.port > 65535) {
			InventorySync.getLogger().warn("MySQL port too high: " + Values.MySQL.port + "! Defaulting to 65535!");

			Values.MySQL.port = 65535;
		}

		InventorySync.getLogger().debug("Loaded config");
	}

	@UtilityClass
	public static class Values {
		@Getter
		protected static String storageEngine;

		@UtilityClass
		public static class H2 {
			@Getter
			protected static Path databaseFile;
		}

		@UtilityClass
		public static class MySQL {
			@Getter
			protected static String host;
			@Getter
			protected static int port;
			@Getter
			protected static String database;
			@Getter
			protected static String user;
			@Getter
			protected static String password;
			@Getter
			protected static String tablePrefix;
		}
	}
}

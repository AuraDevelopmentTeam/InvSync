package world.jnc.invsync.config;

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
import world.jnc.invsync.InventorySync;
import world.jnc.invsync.util.database.DatabaseConnection;

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

		@NonNull
		ConfigurationLoader<CommentedConfigurationNode> loader = HoconConfigurationLoader.builder().setPath(configFile)
				.build();
		@NonNull
		ConfigurationNode rootNode;

		try {
			rootNode = loader.load();
		} catch (IOException e) {
			InventorySync.getLogger().error("Config could not be loaded!", e);

			return;
		}

		ConfigurationNode global = rootNode.getNode("global");
		Values.Global.debug = global.getNode("debug").getBoolean(false);
		Values.Global.maxWait = global.getNode("maxWait").getInt(1000);

		ConfigurationNode synchronize = rootNode.getNode("synchronize");
		Values.Synchronize.enableInventory = synchronize.getNode("enableInventory").getBoolean(true);
		Values.Synchronize.enableEnderChest = synchronize.getNode("enableEnderChest").getBoolean(true);
		Values.Synchronize.enableGameMode = synchronize.getNode("enableGameMode").getBoolean(true);
		Values.Synchronize.enableExperience = synchronize.getNode("enableExperience").getBoolean(true);
		Values.Synchronize.enableHealth = synchronize.getNode("enableHealth").getBoolean(true);
		Values.Synchronize.enableHunger = synchronize.getNode("enableHunger").getBoolean(true);
		Values.Synchronize.enableAchievements = synchronize.getNode("enableAchievements").getBoolean(true);

		ConfigurationNode storage = rootNode.getNode("storage");
		Values.Storage.storageEngine = storage.getNode("storageEngine").getString(validStorageEngines[0]);

		if (!Arrays.asList(validStorageEngines).contains(Values.Storage.storageEngine)) {
			InventorySync.getLogger().warn("Invalid storage engine in config: \"" + Values.Storage.storageEngine
					+ "\"! Defaulting to \"" + validStorageEngines[0] + "\"!");

			Values.Storage.storageEngine = validStorageEngines[0];
		}

		ConfigurationNode h2 = storage.getNode("h2");
		Values.Storage.H2.databaseFile = configDir.resolve(h2.getNode("databaseFile").getString("inventoryStorage.db"));

		ConfigurationNode mySQL = storage.getNode("MySQL");
		Values.Storage.MySQL.host = mySQL.getNode("host").getString("localhost");
		Values.Storage.MySQL.port = mySQL.getNode("port").getInt(DatabaseConnection.DEFAULT_MYSQL_PORT);
		Values.Storage.MySQL.database = mySQL.getNode("database").getString("invsync");
		Values.Storage.MySQL.user = mySQL.getNode("user").getString("invsync");
		Values.Storage.MySQL.password = mySQL.getNode("password").getString("sup3rS3cur3Pa55w0rd!");
		Values.Storage.MySQL.tablePrefix = mySQL.getNode("tablePrefix").getString("invsync_");

		if (Values.Storage.MySQL.port < 1) {
			InventorySync.getLogger().warn("MySQL port too low: " + Values.Storage.MySQL.port + "! Defaulting to 1!");

			Values.Storage.MySQL.port = 1;
		} else if (Values.Storage.MySQL.port > 65535) {
			InventorySync.getLogger()
					.warn("MySQL port too high: " + Values.Storage.MySQL.port + "! Defaulting to 65535!");

			Values.Storage.MySQL.port = 65535;
		}

		InventorySync.getLogger().debug("Loaded config");
	}

	@UtilityClass
	public static class Values {
		@UtilityClass
		public static class Global {
			/** Hidden config value! Will not appear in default config. */
			@Getter
			private static boolean debug;
			@Getter
			private static int maxWait;
		}

		@UtilityClass
		public static class Synchronize {
			@Getter
			private static boolean enableInventory;
			@Getter
			private static boolean enableEnderChest;
			@Getter
			private static boolean enableGameMode;
			@Getter
			private static boolean enableExperience;
			@Getter
			private static boolean enableHealth;
			@Getter
			private static boolean enableHunger;
			@Getter
			private static boolean enableAchievements;
		}

		@UtilityClass
		public static class Storage {
			@Getter
			private static String storageEngine;

			@UtilityClass
			public static class H2 {
				@Getter
				private static Path databaseFile;
			}

			@UtilityClass
			public static class MySQL {
				@Getter
				private static String host;
				@Getter
				private static int port;
				@Getter
				private static String database;
				@Getter
				private static String user;
				@Getter
				private static String password;
				@Getter
				private static String tablePrefix;
			}
		}
	}
}

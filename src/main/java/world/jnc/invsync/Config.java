package world.jnc.invsync;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;

import lombok.Getter;
import lombok.NonNull;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;

public class Config {
	public static final String[] validStorageEngines = new String[] { "h2", "mysql" };

	@NonNull
	private Path configFile;
	@NonNull
	private Path configDir;
	@NonNull
	private ConfigurationLoader<CommentedConfigurationNode> loader;
	@NonNull
	private ConfigurationNode rootNode;

	public Config(Path configFile, Path configDir) {
		this.configFile = configFile;
		this.configDir = configDir;
	}

	public void load() {
		loader = HoconConfigurationLoader.builder().setPath(configFile).build();

		try {
			rootNode = loader.load();
		} catch (IOException e) {
			e.printStackTrace();
		}

		ConfigurationNode storage = rootNode.getNode("storage");
		Values.storageEngine = storage.getNode("storageEngine").getString(validStorageEngines[0]);

		if (!Arrays.asList(validStorageEngines).contains(Values.storageEngine)) {
			Values.storageEngine = validStorageEngines[0];
			storage.getNode("storageEngine").setValue(Values.storageEngine);
		}
		
		InventorySync.getLogger().debug("Loaded config");

		save();
	}

	public void save() {
		try {
			loader.save(rootNode);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static class Values {
		@Getter
		protected static String storageEngine;
	}
}

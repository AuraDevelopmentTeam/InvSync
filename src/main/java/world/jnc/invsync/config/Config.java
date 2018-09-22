package world.jnc.invsync.config;

import java.nio.file.Path;
import lombok.Getter;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import world.jnc.invsync.InventorySync;

@ConfigSerializable
public class Config {
  @Setting @Getter private General general = new General();

  @Setting(comment = "Which player data to synchronize")
  @Getter
  private Synchronize synchronize = new Synchronize();

  @Setting @Getter private Storage storage = new Storage();

  @ConfigSerializable
  public static class General {
    @Setting(comment = "Enable debug logging")
    @Getter
    private boolean debug = false;

    @Setting(
      comment =
          "Maximum amount of time to wait for the other server to finish writing the data. Time in ms\nIncrease value if you notice synchronizations failing"
    )
    @Getter
    private long maxWait = 1000L;
  }

  @ConfigSerializable
  public static class Synchronize {
    @Setting @Getter private boolean enableInventory = true;
    @Setting @Getter private boolean enableEnderChest = true;
    @Setting @Getter private boolean enableGameMode = true;
    @Setting @Getter private boolean enableExperience = true;
    @Setting @Getter private boolean enableHealth = true;
    @Setting @Getter private boolean enableHunger = true;
    @Setting @Getter private boolean enablePotionEffects = true;
  }

  @ConfigSerializable
  public static class Storage {
    @Setting(comment = "The stoage engine that should be used\nAllowed values: h2 mysql")
    @Getter
    private StorageEngine storageEngine = StorageEngine.h2;

    @Setting(comment = "Settings for the h2 storage engine")
    @Getter
    private H2 h2 = new H2();

    @Setting(value = "MySQL", comment = "Settings for the MySQL storage engine")
    @Getter
    private MySQL mysql = new MySQL();

    public boolean isH2() {
      return getStorageEngine() == Config.Storage.StorageEngine.h2;
    }

    public boolean isMySQL() {
      return getStorageEngine() == Config.Storage.StorageEngine.mysql;
    }

    public static enum StorageEngine {
      h2,
      mysql;
    }

    @ConfigSerializable
    public static class H2 {
      @Setting @Getter private String databaseFile = "inventoryStorage";

      public Path getAbsoluteDatabasePath() {
        return InventorySync.getConfigDir().resolve(getDatabaseFile()).toAbsolutePath();
      }
    }

    @ConfigSerializable
    public static class MySQL {
      @Setting @Getter private String host = "localhost";
      @Setting @Getter private int port = 3306;
      @Setting @Getter private String database = "invsync";
      @Setting @Getter private String user = "invsync";
      @Setting @Getter private String password = "sup3rS3cur3Pa55w0rd!";

      @Setting(comment = "Prefix for the plugin tables")
      @Getter
      private String tablePrefix = "invsync_";
    }
  }
}

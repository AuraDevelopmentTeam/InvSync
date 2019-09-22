package world.jnc.invsync.config;

import com.google.common.annotations.VisibleForTesting;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.SneakyThrows;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import world.jnc.invsync.InventorySync;

@ConfigSerializable
@Getter
public class Config {
  @Setting private General general = new General();

  @Setting(comment = "Which player data to synchronize")
  private Map<String, Boolean> synchronize = new HashMap<String, Boolean>();

  @Setting private Storage storage = new Storage();

  public boolean getSynchronize(String module) {
    if (!synchronize.containsKey(module)) {
      synchronize.put(module, true);
    }

    return synchronize.get(module);
  }

  @ConfigSerializable
  @Getter
  public static class General {
    public static final long DANGEROUS_AUTO_SAVE_INTERVAL = 30;

    @Setting(
      comment =
          "In which interval (in s) should the plugin autosave the players. (Values lower than "
              + DANGEROUS_AUTO_SAVE_INTERVAL
              + "s are not recommended!)\n"
              + "Values of 0 or lower disable auto saving."
    )
    private long autoSaveInterval = 0;

    @Setting(comment = "Enable debug logging")
    private boolean debug = false;

    @Setting(
      comment =
          "Maximum amount of time to wait for the other server to finish writing the data. Time in ms\n"
              + "Increase value if you notice synchronizations failing"
    )
    private long maxWait = 1000L;

    public boolean isAutoSaveEnabled() {
      return getAutoSaveInterval() > 0;
    }

    public boolean isAutoSaveIntervalDangerous() {
      return isAutoSaveEnabled() && (getAutoSaveInterval() < DANGEROUS_AUTO_SAVE_INTERVAL);
    }
  }

  @ConfigSerializable
  @Getter
  public static class Storage {
    @Setting(comment = "The stoage engine that should be used\n" + "Allowed values: h2 mysql")
    private StorageEngine storageEngine = StorageEngine.h2;

    @Setting(comment = "Settings for the h2 storage engine")
    private H2 h2 = new H2();

    @Setting(value = "MySQL", comment = "Settings for the MySQL storage engine")
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

      public static final String allowedValues =
          Arrays.stream(Config.Storage.StorageEngine.values())
              .map(Enum::name)
              .collect(Collectors.joining(", "));
    }

    @ConfigSerializable
    @Getter
    public static class H2 {
      @Setting(
        comment =
            "If this is a relative path, it will be relative to the InvSync config dir (should be \"config/invsync\"). Absolute\n"
                + "paths work too of course"
      )
      private String databaseFile = "inventoryStorage";

      public Path getAbsoluteDatabasePath() {
        return InventorySync.getConfigDir().resolve(getDatabaseFile()).toAbsolutePath();
      }
    }

    @ConfigSerializable
    @Getter
    public static class MySQL {
      private static final String UTF_8 = StandardCharsets.UTF_8.name();

      @Setting private String host = "localhost";
      @Setting private int port = 3306;
      @Setting private String database = "invsync";
      @Setting private String user = "invsync";
      @Setting private String password = "sup3rS3cur3Pa55w0rd!";

      @Setting(comment = "Prefix for the plugin tables")
      private String tablePrefix = "invsync_";

      public String getUserEncoded() {
        return urlEncode(getUser());
      }

      public String getPasswordEncoded() {
        return urlEncode(getPassword());
      }

      @VisibleForTesting
      @SneakyThrows(UnsupportedEncodingException.class)
      static String urlEncode(String str) {
        return URLEncoder.encode(str, UTF_8);
      }
    }
  }
}

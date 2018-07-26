package world.jnc.invsync.util.database;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.nio.ByteBuffer;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;
import lombok.Getter;
import lombok.NonNull;
import org.spongepowered.api.entity.living.player.Player;
import world.jnc.invsync.InventorySync;
import world.jnc.invsync.config.Config;

@SuppressFBWarnings(
  value = {
    "SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING",
    "SQL_NONCONSTANT_STRING_PASSED_TO_EXECUTE"
  },
  justification = "The database name needs to be dynamic in order to allow prefixes"
)
public class DataSource {
  private final DatabaseConnection connection;
  @Getter private final boolean h2;
  @Getter private final boolean mysql;

  private final String tableInventories;
  private final String tableInventoriesColumnUUID;
  private final String tableInventoriesColumnActive;
  private final String tableInventoriesColumnData;

  @NonNull private PreparedStatement insertInventory;
  @NonNull private PreparedStatement loadInventory;
  @NonNull private PreparedStatement setActive;
  @NonNull private PreparedStatement isActive;

  public static String getPlayerString(Player player) {
    return player.getName() + " (" + player.getUniqueId().toString() + ')';
  }

  private static byte[] getBytesFromUUID(UUID uuid) {
    ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
    bb.putLong(uuid.getMostSignificantBits());
    bb.putLong(uuid.getLeastSignificantBits());

    return bb.array();
  }

  public DataSource() throws SQLException {
    if ("h2".equals(Config.Values.Storage.getStorageEngine())) {
      h2 = true;
      mysql = false;

      connection = new H2DatabaseConnection(Config.Values.Storage.H2.getDatabaseFile());
    } else if ("mysql".equals(Config.Values.Storage.getStorageEngine())) {
      h2 = false;
      mysql = true;

      connection =
          new MysqlDatabaseConnection(
              Config.Values.Storage.MySQL.getHost(),
              Config.Values.Storage.MySQL.getPort(),
              Config.Values.Storage.MySQL.getDatabase(),
              Config.Values.Storage.MySQL.getUser(),
              Config.Values.Storage.MySQL.getPassword());
    } else throw new IllegalArgumentException("Invalid storage Engine!");

    tableInventories = getTableName("inventories");
    tableInventoriesColumnUUID = "UUID";
    tableInventoriesColumnActive = "Active";
    tableInventoriesColumnData = "Data";

    prepareTable();
    prepareStatements();
  }

  public void saveInventory(Player player, byte[] data) {
    if (!connection.verifyConnection()) {
      prepareStatements();
    }

    String playerName = getPlayerString(player);

    try {
      InventorySync.getLogger().debug("Saving inventory for player " + playerName);

      insertInventory.setBytes(1, getBytesFromUUID(player.getUniqueId()));
      insertInventory.setBytes(2, data);

      insertInventory.executeUpdate();
      insertInventory.clearParameters();
    } catch (SQLException e) {
      InventorySync.getLogger().error("Could not save invetory for player " + playerName, e);
    }
  }

  public Optional<byte[]> loadInventory(Player player) {
    if (!connection.verifyConnection()) {
      prepareStatements();
    }

    String playerName = getPlayerString(player);

    try {
      InventorySync.getLogger().debug("Loading inventory for player " + playerName);

      loadInventory.setBytes(1, getBytesFromUUID(player.getUniqueId()));

      try (ResultSet result = loadInventory.executeQuery()) {
        loadInventory.clearParameters();

        if (result.next()) return Optional.of(result.getBytes(tableInventoriesColumnData));
        else return Optional.empty();
      }
    } catch (SQLException e) {
      InventorySync.getLogger().error("Could not load invetory for player " + playerName, e);

      return Optional.empty();
    }
  }

  public void setActive(Player player) {
    if (!connection.verifyConnection()) {
      prepareStatements();
    }

    String playerName = getPlayerString(player);

    try {
      InventorySync.getLogger().debug("Set player " + playerName + " active");

      setActive.setBytes(1, getBytesFromUUID(player.getUniqueId()));

      setActive.executeUpdate();
      setActive.clearParameters();
    } catch (SQLException e) {
      InventorySync.getLogger().error("Could not set player " + playerName + " active", e);
    }
  }

  public boolean isActive(Player player) {
    if (!connection.verifyConnection()) {
      prepareStatements();
    }

    try {
      isActive.setBytes(1, getBytesFromUUID(player.getUniqueId()));

      try (ResultSet result = isActive.executeQuery()) {
        isActive.clearParameters();

        if (result.next()) return result.getBoolean(1);
        else return false;
      }
    } catch (SQLException e) {
      InventorySync.getLogger()
          .error("Could not check if " + getPlayerString(player) + " is active", e);

      return false;
    }
  }

  @Override
  protected void finalize() throws Throwable {
    if (insertInventory != null) {
      insertInventory.close();
    }

    if (loadInventory != null) {
      loadInventory.close();
    }

    super.finalize();
  }

  private String getTableName(String baseName) {
    String name;

    if (mysql) {
      name = Config.Values.Storage.MySQL.getTablePrefix() + baseName;
    } else if (h2) {
      name = baseName;
    } else return null;

    name = name.replaceAll("`", "``");

    return '`' + name + '`';
  }

  private void prepareTable() {
    try {
      StringBuilder createTable = new StringBuilder();

      createTable
          .append("CREATE TABLE IF NOT EXISTS ")
          .append(tableInventories)
          .append(" (")
          .append(tableInventoriesColumnUUID)
          .append(" BINARY(16) NOT NULL, ")
          .append(tableInventoriesColumnActive)
          .append(" BOOL NOT NULL, ")
          .append(tableInventoriesColumnData)
          .append(" MEDIUMBLOB NOT NULL, PRIMARY KEY (")
          .append(tableInventoriesColumnUUID)
          .append(")) DEFAULT CHARSET=utf8");

      connection.executeStatement(createTable.toString());

      InventorySync.getLogger().debug("Created table");
    } catch (SQLException e) {
      InventorySync.getLogger().error("Could not create table!", e);
    }
  }

  private void prepareStatements() {
    try {
      StringBuilder insertInventoryStr = new StringBuilder();
      StringBuilder getInventoryStr = new StringBuilder();
      StringBuilder setActiveStr = new StringBuilder();
      StringBuilder isActiveStr = new StringBuilder();

      insertInventoryStr
          .append("REPLACE INTO ")
          .append(tableInventories)
          .append(" (")
          .append(tableInventoriesColumnUUID)
          .append(", ")
          .append(tableInventoriesColumnActive)
          .append(", ")
          .append(tableInventoriesColumnData)
          .append(") VALUES (?, FALSE, ?)");
      getInventoryStr
          .append("SELECT ")
          .append(tableInventoriesColumnData)
          .append(" FROM ")
          .append(tableInventories)
          .append(" WHERE ")
          .append(tableInventoriesColumnUUID)
          .append(" = ? LIMIT 1");
      setActiveStr
          .append("UPDATE ")
          .append(tableInventories)
          .append(" SET ")
          .append(tableInventoriesColumnActive)
          .append(" = TRUE WHERE ")
          .append(tableInventoriesColumnUUID)
          .append(" = ? LIMIT 1");
      isActiveStr
          .append("SELECT ")
          .append(tableInventoriesColumnActive)
          .append(" FROM ")
          .append(tableInventories)
          .append(" WHERE ")
          .append(tableInventoriesColumnUUID)
          .append(" = ? LIMIT 1");

      insertInventory = connection.getPreparedStatement(insertInventoryStr.toString());
      loadInventory = connection.getPreparedStatement(getInventoryStr.toString());
      setActive = connection.getPreparedStatement(setActiveStr.toString());
      isActive = connection.getPreparedStatement(isActiveStr.toString());

      InventorySync.getLogger().debug("Prepared statements");
    } catch (SQLException e) {
      InventorySync.getLogger().error("Could not prepare statements!", e);
    }
  }
}

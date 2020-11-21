package world.jnc.invsync.util.database;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;
import lombok.Getter;
import org.spongepowered.api.entity.living.player.Player;
import world.jnc.invsync.InventorySync;
import world.jnc.invsync.config.Config;

@SuppressFBWarnings(
    value = {
      "SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING",
      "SQL_NONCONSTANT_STRING_PASSED_TO_EXECUTE"
    },
    justification = "The database name needs to be dynamic in order to allow prefixes")
public class DataSource {
  private final DatabaseConnection connection;
  @Getter private final Config.Storage storageConfig;

  private final String tableInventories;
  private final String tableInventoriesColumnUUID;
  private final String tableInventoriesColumnActive;
  private final String tableInventoriesColumnData;

  private final String insertInventoryQuery;
  private final String loadInventoryQuery;
  private final String setActiveQuery;
  private final String isActiveQuery;

  public static String getPlayerString(Player player) {
    if (player == null) return "<unknown>";
    else return player.getName() + " (" + player.getUniqueId().toString() + ')';
  }

  private static byte[] getBytesFromUUID(UUID uuid) {
    ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
    bb.putLong(uuid.getMostSignificantBits());
    bb.putLong(uuid.getLeastSignificantBits());

    return bb.array();
  }

  public DataSource() throws SQLException {
    storageConfig = InventorySync.getConfig().getStorage();

    if (storageConfig.isH2()) {
      connection = new H2DatabaseConnection(storageConfig.getH2());
    } else if (storageConfig.isMySQL()) {
      connection = new MysqlDatabaseConnection(storageConfig.getMysql());
    } else if (storageConfig.isPostgreSQL()) {
      connection = new PostgreSQLDatabaseConnection(storageConfig.getPostgreSQL());
    } else throw new IllegalArgumentException("Invalid storage Engine!");

    tableInventories = getTableName("inventories");
    tableInventoriesColumnUUID = "UUID";
    tableInventoriesColumnActive = "Active";
    tableInventoriesColumnData = "Data";

    prepareTable();

    StringBuilder insertInventoryStr = new StringBuilder();
    StringBuilder getInventoryStr = new StringBuilder();
    StringBuilder setActiveStr = new StringBuilder();
    StringBuilder isActiveStr = new StringBuilder();

    insertInventoryStr
        .append(storageConfig.isPostgreSQL() ? "INSERT" : "REPLACE")
        .append(" INTO ")
        .append(tableInventories)
        .append(" (")
        .append(tableInventoriesColumnUUID)
        .append(", ")
        .append(tableInventoriesColumnActive)
        .append(", ")
        .append(tableInventoriesColumnData)
        .append(") VALUES (?, FALSE, ?)");

    if (storageConfig.isPostgreSQL()) {
      insertInventoryStr
          .append(" ON CONFLICT (")
          .append(tableInventoriesColumnUUID)
          .append(") DO UPDATE SET ")
          .append(tableInventoriesColumnActive)
          .append(" = EXCLUDED.")
          .append(tableInventoriesColumnActive)
          .append(", ")
          .append(tableInventoriesColumnData)
          .append(" = EXCLUDED.")
          .append(tableInventoriesColumnData);
    }

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
        .append(" = ?");

    if (!storageConfig.isPostgreSQL()) setActiveStr.append(" LIMIT 1");

    isActiveStr
        .append("SELECT ")
        .append(tableInventoriesColumnActive)
        .append(" FROM ")
        .append(tableInventories)
        .append(" WHERE ")
        .append(tableInventoriesColumnUUID)
        .append(" = ? LIMIT 1");

    insertInventoryQuery = insertInventoryStr.toString();
    loadInventoryQuery = getInventoryStr.toString();
    setActiveQuery = setActiveStr.toString();
    isActiveQuery = isActiveStr.toString();
  }

  public void saveInventory(Player player, byte[] data) {
    String playerName = getPlayerString(player);

    try (PreparedStatement insertInventory = connection.getPreparedStatement(insertInventoryQuery);
        Connection connection = insertInventory.getConnection()) {
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
    String playerName = getPlayerString(player);

    try (PreparedStatement loadInventory = connection.getPreparedStatement(loadInventoryQuery);
        Connection connection = loadInventory.getConnection()) {
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
    String playerName = getPlayerString(player);

    try (PreparedStatement setActive = connection.getPreparedStatement(setActiveQuery);
        Connection connection = setActive.getConnection()) {
      InventorySync.getLogger().debug("Set player " + playerName + " active");

      setActive.setBytes(1, getBytesFromUUID(player.getUniqueId()));

      setActive.executeUpdate();
      setActive.clearParameters();
    } catch (SQLException e) {
      InventorySync.getLogger().error("Could not set player " + playerName + " active", e);
    }
  }

  public boolean isActive(Player player) {
    try (PreparedStatement isActive = connection.getPreparedStatement(isActiveQuery);
        Connection connection = isActive.getConnection()) {
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

  private String getTableName(String baseName) {
    String name;

    if (storageConfig.isH2()) {
      name = baseName;
    } else if (storageConfig.isMySQL()) {
      name = storageConfig.getMysql().getTablePrefix() + baseName;
    } else if (storageConfig.isPostgreSQL()) {
      return storageConfig.getPostgreSQL().getTablePrefix() + baseName;
    } else return null;

    name = name.replaceAll("`", "``");

    return '`' + name + '`';
  }

  private void prepareTable() {
    try {
      StringBuilder createTable = new StringBuilder();

      String uuidDataType = storageConfig.isPostgreSQL() ? "bytea" : "BINARY(16)";
      String invDataDataType = storageConfig.isPostgreSQL() ? "bytea" : "MEDIUMBLOB";

      createTable
          .append("CREATE TABLE IF NOT EXISTS ")
          .append(tableInventories)
          .append(" (")
          .append(tableInventoriesColumnUUID)
          .append(" ")
          .append(uuidDataType)
          .append(" NOT NULL, ")
          .append(tableInventoriesColumnActive)
          .append(" BOOL NOT NULL, ")
          .append(tableInventoriesColumnData)
          .append(" ")
          .append(invDataDataType)
          .append(" NOT NULL, PRIMARY KEY (")
          .append(tableInventoriesColumnUUID)
          .append("))");

      if (!storageConfig.isPostgreSQL()) createTable.append(" DEFAULT CHARSET=utf8");

      connection.executeStatement(createTable.toString());

      InventorySync.getLogger().debug("Created table");
    } catch (SQLException e) {
      InventorySync.getLogger().error("Could not create table!", e);
    }
  }
}

package world.jnc.invsync;

import java.nio.ByteBuffer;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

import org.spongepowered.api.entity.living.player.Player;

import lombok.Cleanup;
import lombok.NonNull;
import world.jnc.invsync.util.DatabaseConnection;
import world.jnc.invsync.util.Pair;

public class DataSource {
	private final DatabaseConnection connection;
	private final boolean h2;
	private final boolean mysql;

	private final String tableInventories;
	private final String tableInventoriesColumnUUID;
	private final String tableInventoriesColumnInventory;
	private final String tableInventoriesColumnEnderchest;

	@NonNull
	private PreparedStatement insertInventory;
	@NonNull
	private PreparedStatement loadInventory;

	private static byte[] getBytesFromUUID(UUID uuid) {
		ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
		bb.putLong(uuid.getMostSignificantBits());
		bb.putLong(uuid.getLeastSignificantBits());

		return bb.array();
	}

	public DataSource() throws SQLException {
		if ("h2".equals(Config.Values.getStorageEngine())) {
			h2 = true;
			mysql = false;

			connection = new DatabaseConnection(Config.Values.H2.getDatabaseFile());
		} else if ("mysql".equals(Config.Values.getStorageEngine())) {
			h2 = false;
			mysql = true;

			connection = new DatabaseConnection(Config.Values.MySQL.getHost(), Config.Values.MySQL.getPort(),
					Config.Values.MySQL.getDatabase(), Config.Values.MySQL.getUser(),
					Config.Values.MySQL.getPassword());
		} else
			throw new IllegalArgumentException("Invalid storage Engine!");

		tableInventories = getTableName("inventories");
		tableInventoriesColumnUUID = "UUID";
		tableInventoriesColumnInventory = "Inventory";
		tableInventoriesColumnEnderchest = "Enderchest";

		prepareTable();
		prepareStatements();
	}

	public void saveInventory(Player player, byte[] inventoryData, byte[] enderChestData) {
		String playerName = getPlayerString(player);

		try {
			InventorySync.getLogger().debug("Saving inventory for player " + playerName);

			insertInventory.setBytes(1, getBytesFromUUID(player.getUniqueId()));
			insertInventory.setBytes(2, inventoryData);
			insertInventory.setBytes(3, enderChestData);

			insertInventory.executeUpdate();
			insertInventory.clearParameters();
		} catch (SQLException e) {
			InventorySync.getLogger().error("Could not save invetory for player " + playerName, e);
		}
	}

	public Optional<Pair<byte[], byte[]>> loadInventory(Player player) {
		String playerName = getPlayerString(player);

		try {
			InventorySync.getLogger().debug("Loading inventory for player " + playerName);

			loadInventory.setBytes(1, getBytesFromUUID(player.getUniqueId()));

			@Cleanup
			ResultSet result = loadInventory.executeQuery();
			loadInventory.clearParameters();

			if (result.next())
				return Optional.of(Pair.of(result.getBytes(tableInventoriesColumnInventory),
						result.getBytes(tableInventoriesColumnEnderchest)));
			else
				return Optional.empty();
		} catch (SQLException e) {
			InventorySync.getLogger().error("Could not load invetory for player " + playerName, e);

			return Optional.empty();
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
			name = Config.Values.MySQL.getTablePrefix() + baseName;
		} else if (h2) {
			name = baseName;
		} else
			return null;

		name = name.replaceAll("`", "``");

		return '`' + name + '`';
	}

	private void prepareTable() {
		try {
			StringBuilder createTable = new StringBuilder();

			createTable.append("CREATE TABLE IF NOT EXISTS ").append(tableInventories).append(" (")
					.append(tableInventoriesColumnUUID).append(" BINARY(16) NOT NULL, ")
					.append(tableInventoriesColumnInventory).append(" MEDIUMBLOB NOT NULL, ")
					.append(tableInventoriesColumnEnderchest)
					.append(" MEDIUMBLOB NOT NULL, PRIMARY KEY (UUID)) DEFAULT CHARSET=utf8");

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

			insertInventoryStr.append("REPLACE INTO ").append(tableInventories).append(" (")
					.append(tableInventoriesColumnUUID).append(", ").append(tableInventoriesColumnInventory)
					.append(", ").append(tableInventoriesColumnEnderchest).append(") VALUES (?, ?, ?)");
			getInventoryStr.append("SELECT Inventory, Enderchest, FROM ").append(tableInventories)
					.append(" WHERE UUID = ? LIMIT 1");

			insertInventory = connection.getPreparedStatement(insertInventoryStr.toString());
			loadInventory = connection.getPreparedStatement(getInventoryStr.toString());

			InventorySync.getLogger().debug("Prepared statements");
		} catch (SQLException e) {
			InventorySync.getLogger().error("Could not prepare statements!", e);
		}
	}

	private String getPlayerString(Player player) {
		return player.getName() + " (" + player.getUniqueId().toString() + ')';
	}
}

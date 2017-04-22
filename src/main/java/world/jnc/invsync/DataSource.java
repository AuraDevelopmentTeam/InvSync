package world.jnc.invsync;

import java.sql.SQLException;

import world.jnc.invsync.util.DatabaseConnection;

public class DataSource {
	private final DatabaseConnection connection;
	private final boolean h2;
	private final boolean mysql;

	private final String tableInventories;

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

		prepareTable();
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
			connection.executeStatement("CREATE TABLE IF NOT EXISTS " + tableInventories
					+ " (UUID BINARY(16) NOT NULL,Inventory MEDIUMBLOB NOT NULL,PRIMARY KEY (UUID)) DEFAULT CHARSET=utf8;");

			InventorySync.getLogger().debug("Creating table if not exists");
		} catch (SQLException e) {
			InventorySync.getLogger().error("Could not create table!", e);
		}
	}
}

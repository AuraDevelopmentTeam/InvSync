package world.jnc.invsync.util.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.sql.SqlService;

import lombok.Cleanup;
import lombok.Getter;
import world.jnc.invsync.InventorySync;

public abstract class DatabaseConnection {
	public static final int DEFAULT_MYSQL_PORT = 3306;
	protected static SqlService sql;

	@Getter
	protected Connection connection;
	protected String connectionURL;

	protected static DataSource getDataSource(String jdbcUrl) throws SQLException {
		if (sql == null) {
			sql = Sponge.getServiceManager().provide(SqlService.class).get();
		}

		return sql.getDataSource(jdbcUrl);
	}

	protected DatabaseConnection(String connectionURL) throws SQLException {
		connect(connectionURL);
	}

	private void connect(String connectionURL) throws SQLException {
		this.connectionURL = connectionURL;

		InventorySync.getLogger().debug("Connecting to: " + connectionURL.replaceFirst(":[^:]*@", ":*****@"));

		connection = getDataSource(connectionURL).getConnection();
	}

	private void reconnect() throws SQLException {
		InventorySync.getLogger().debug("Reconnecting to: " + connectionURL.replaceFirst(":[^:]*@", ":*****@"));

		connection = getDataSource(connectionURL).getConnection();
	}

	public boolean isConnectionActive() {
		try {
			return (connection != null) && connection.isValid(0);
		} catch (SQLException e) {
			e.printStackTrace();

			return false;
		}
	}

	public boolean verifyConnection() {
		if (!isConnectionActive()) {
			try {
				reconnect();
			} catch (SQLException e) {
				InventorySync.getLogger().error("Reconnecting failed!", e);
			}

			return false;
		}

		return true;
	}

	public Statement getStatement() throws SQLException {
		if (!isConnectionActive())
			throw new SQLException("MySQL-connection is not active!");

		return connection.createStatement();
	}

	public PreparedStatement getPreparedStatement(String statement) throws SQLException {
		InventorySync.getLogger().debug("Preparing statement: " + statement);

		return connection.prepareStatement(statement);
	}

	public ResultSet executeQuery(String query) throws SQLException {
		@Cleanup
		Statement statement = getStatement();

		return statement.executeQuery(query);
	}

	public boolean executeStatement(String query) throws SQLException {
		@Cleanup
		Statement statement = getStatement();

		return statement.execute(query);
	}

	public int executeUpdate(String query) throws SQLException {
		@Cleanup
		Statement statement = getStatement();

		return statement.executeUpdate(query);
	}

	@Override
	protected void finalize() throws Throwable {
		try {
			if ((connection != null) && !connection.isClosed()) {
				connection.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		super.finalize();
	}
}

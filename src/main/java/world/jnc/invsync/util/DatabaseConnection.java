package world.jnc.invsync.util;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.sql.SqlService;

import lombok.Cleanup;
import lombok.Getter;

public class DatabaseConnection {
	public static final int DEFAULT_MYSQL_PORT = 3306;
	protected static SqlService sql;

	protected static DataSource getDataSource(String jdbcUrl) throws SQLException {
		if (sql == null) {
			sql = Sponge.getServiceManager().provide(SqlService.class).get();
		}

		return sql.getDataSource(jdbcUrl);
	}

	protected static String urlEncode(String toEncode) {
		try {
			return URLEncoder.encode(toEncode, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// Won't happen anyways!
			e.printStackTrace();

			return null;
		}
	}
	
	@Getter
	protected Connection connection;

	/**
	 * Opens a MySQL database connection.
	 * 
	 * @param host
	 *            Host to connect to
	 * @param port
	 *            Port of the host. Default 3306. See
	 *            {@link DatabaseConnection#DEFAULT_MYSQL_PORT}
	 * @param database
	 *            The database to connect to
	 * @param user
	 *            User for the connection
	 * @param password
	 *            Password for the user
	 * @throws SQLException
	 */
	public DatabaseConnection(String host, int port, String database, String user, String password)
			throws SQLException {
		StringBuilder connectionURL = new StringBuilder();

		connectionURL.append("jdbc:mysq://").append(urlEncode(host)).append(':').append(port).append('/')
				.append(urlEncode(database)).append("?user=").append(urlEncode(user)).append("&password=")
				.append(urlEncode(password));

		connection = getDataSource(connectionURL.toString()).getConnection();
	}

	/**
	 * Opens a h2 database connection.
	 * 
	 * @param databaseFile
	 *            storage of the database file
	 * @throws SQLException
	 */
	public DatabaseConnection(File databaseFile) throws SQLException {
		StringBuilder connectionURL = new StringBuilder();

		connectionURL.append("jdbc:h2:").append(databaseFile.getAbsolutePath());

		connection = getDataSource(connectionURL.toString()).getConnection();
	}

	public boolean isConnectionActive() {
		try {
			return (connection != null) && connection.isValid(0);
		} catch (SQLException e) {
			e.printStackTrace();

			return false;
		}
	}

	public Statement getStatement() throws SQLException {
		if (!isConnectionActive())
			throw new SQLException("MySQL-connection is not active!");

		return connection.createStatement();
	}

	public ResultSet executeQuery(String query) throws SQLException {
		@Cleanup
		Statement statement = getStatement();

		return statement.executeQuery(query);
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

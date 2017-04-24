package world.jnc.invsync.util.database;

import java.sql.SQLException;

public class MysqlDatabaseConnection extends DatabaseConnection {
	private static final String URLFormat = "jdbc:mysql://%s:%s@%s:%d/%s";

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
	public MysqlDatabaseConnection(String host, int port, String database, String user, String password)
			throws SQLException {
		super(String.format(URLFormat, user, password, host, port, database));
	}
}

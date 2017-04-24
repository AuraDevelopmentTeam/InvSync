package world.jnc.invsync.util.database;

import java.nio.file.Path;
import java.sql.SQLException;

public class H2DatabaseConnection extends DatabaseConnection {
	/**
	 * Opens a h2 database connection.
	 *
	 * @param databaseFile
	 *            storage of the database file
	 * @throws SQLException
	 */
	public H2DatabaseConnection(Path databaseFile) throws SQLException {
		super("jdbc:h2:" + databaseFile.toAbsolutePath());
	}
}

package world.jnc.invsync.util.database;

import java.nio.file.Path;
import java.sql.SQLException;

public class H2DatabaseConnection extends DatabaseConnection {
	private static final String URLFormat = "jdbc:h2:%s;AUTO_SERVER=TRUE";
	
	/**
	 * Opens a h2 database connection.
	 *
	 * @param databaseFile
	 *            storage of the database file
	 * @throws SQLException
	 */
	public H2DatabaseConnection(Path databaseFile) throws SQLException {
		super(String.format(URLFormat, databaseFile.toAbsolutePath()));
	}
}

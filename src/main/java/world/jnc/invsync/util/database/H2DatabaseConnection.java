package world.jnc.invsync.util.database;

import java.sql.SQLException;
import world.jnc.invsync.config.Config;

public class H2DatabaseConnection extends DatabaseConnection {
  private static final String URLFormat = "jdbc:h2:%s;AUTO_SERVER=TRUE";

  /**
   * Opens a h2 database connection.
   *
   * @param h2 storage of the database file
   * @throws SQLException
   */
  public H2DatabaseConnection(Config.Storage.H2 h2) throws SQLException {
    super(String.format(URLFormat, h2.getAbsoluteDatabasePath()));
  }
}

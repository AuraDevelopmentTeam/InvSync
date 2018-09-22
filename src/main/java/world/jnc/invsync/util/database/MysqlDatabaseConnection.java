package world.jnc.invsync.util.database;

import java.sql.SQLException;
import world.jnc.invsync.config.Config;

public class MysqlDatabaseConnection extends DatabaseConnection {
  private static final String URLFormat = "jdbc:mysql://%s:%s@%s:%d/%s";

  /**
   * Opens a MySQL database connection.
   *
   * @param mysql MySQL config object
   * @throws SQLException
   */
  public MysqlDatabaseConnection(Config.Storage.MySQL mysql) throws SQLException {
    super(
        String.format(
            URLFormat,
            mysql.getUser(),
            mysql.getPassword(),
            mysql.getHost(),
            mysql.getPort(),
            mysql.getDatabase()));
  }
}

package world.jnc.invsync.util.database;

import java.sql.SQLException;
import world.jnc.invsync.config.Config;

public class MysqlDatabaseConnection extends DatabaseConnection {
  private static final String URLFormat = "jdbc:mysql://%1$s:%2$s@%3$s:%4$d/%5$s";
  private static final String URLFormatNoPassword = "jdbc:mysql://%1$s@%3$s:%4$d/%5$s";

  /**
   * Opens a MySQL database connection.
   *
   * @param mysql MySQL config object
   * @throws SQLException
   */
  public MysqlDatabaseConnection(Config.Storage.MySQL mysql) throws SQLException {
    super(
        String.format(
            mysql.getPassword().isEmpty() ? URLFormatNoPassword : URLFormat,
            mysql.getUserEncoded(),
            mysql.getPasswordEncoded(),
            mysql.getHost(),
            mysql.getPort(),
            mysql.getDatabase()));
  }
}

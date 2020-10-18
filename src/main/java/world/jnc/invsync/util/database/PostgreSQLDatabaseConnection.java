package world.jnc.invsync.util.database;

import java.sql.SQLException;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import world.jnc.invsync.config.Config;

public class PostgreSQLDatabaseConnection extends DatabaseConnection {
  private static final String URLFormat =
      "jdbc:postgresql://%1$s:%2$d/%3$s?user=%4$s&password=%5$s%6$s";
  private static final String URLFormatNoPassword =
      "jdbc:postgresql://%1$s:%2$d/%3$s?user=%4$s%6$s";

  private static String formatProperties(Map<String, String> properties) {
    if (properties == null || properties.isEmpty()) {
      return StringUtils.EMPTY;
    }

    StringBuilder props = new StringBuilder();
    properties.forEach((k, v) -> props.append("&").append(k).append("=").append(v));
    return props.toString();
  }

  /**
   * Opens a PostgreSQL database connection.
   *
   * @param postgresql PostgreSQL config object
   * @throws SQLException
   */
  protected PostgreSQLDatabaseConnection(Config.Storage.PostgreSQL postgresql) throws SQLException {
    super(
        String.format(
            postgresql.getPassword().isEmpty() ? URLFormatNoPassword : URLFormat,
            postgresql.getHost(),
            postgresql.getPort(),
            postgresql.getDatabase(),
            postgresql.getUserEncoded(),
            postgresql.getPasswordEncoded(),
            formatProperties(postgresql.getProperties())));
  }
}

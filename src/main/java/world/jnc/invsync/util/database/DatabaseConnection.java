package world.jnc.invsync.util.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;
import lombok.Getter;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.sql.SqlService;
import world.jnc.invsync.InventorySync;

public abstract class DatabaseConnection {
  public static final int DEFAULT_MYSQL_PORT = 3306;

  @Getter(lazy = true)
  private static final SqlService sql = Sponge.getServiceManager().provide(SqlService.class).get();

  @Getter protected DataSource dataSource;
  protected String connectionURL;

  protected static DataSource getDataSource(String jdbcUrl) throws SQLException {
    return getSql().getDataSource(jdbcUrl);
  }

  protected DatabaseConnection(String connectionURL) throws SQLException {
    connect(connectionURL);
  }

  private void connect(String connectionURL) throws SQLException {
    this.connectionURL = connectionURL;

    InventorySync.getLogger()
        .debug("Connecting to: " + connectionURL.replaceFirst(":[^:]*@", ":*****@"));

    dataSource = getDataSource(connectionURL);
  }

  public Connection getConnection() throws SQLException {
    return dataSource.getConnection();
  }

  public Statement getStatement() throws SQLException {
    return getConnection().createStatement();
  }

  public PreparedStatement getPreparedStatement(String statement) throws SQLException {
    InventorySync.getLogger().debug("Preparing statement: " + statement);

    return getConnection().prepareStatement(statement);
  }

  public ResultSet executeQuery(String query) throws SQLException {
    try (Statement statement = getStatement();
        Connection connection = statement.getConnection()) {
      return statement.executeQuery(query);
    }
  }

  public boolean executeStatement(String query) throws SQLException {
    try (Statement statement = getStatement();
        Connection connection = statement.getConnection()) {
      return statement.execute(query);
    }
  }

  public int executeUpdate(String query) throws SQLException {
    try (Statement statement = getStatement();
        Connection connection = statement.getConnection()) {
      return statement.executeUpdate(query);
    }
  }
}

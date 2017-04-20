package world.jnc.invsync.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.mysql.cj.jdbc.MysqlDataSource;

import lombok.Cleanup;
import lombok.Getter;

public class MysqlConnection {
	@Getter
	private Connection connection;

	public MysqlConnection(String host, int port, String database, String user, String password) {
		MysqlDataSource dataSource = new MysqlDataSource();
		dataSource.setServerName(host);
		dataSource.setPort(port);
		dataSource.setUser(user);
		dataSource.setPassword(password);

		try {
			connection = dataSource.getConnection();
		} catch (SQLException e) {
			connection = null;

			e.printStackTrace();
		}
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
			if ((connection != null) && !connection.isClosed())
				connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		super.finalize();
	}
}

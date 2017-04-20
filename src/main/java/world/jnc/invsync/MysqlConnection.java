package world.jnc.invsync;

import java.sql.Connection;
import java.sql.SQLException;

import com.mysql.cj.jdbc.MysqlDataSource;

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

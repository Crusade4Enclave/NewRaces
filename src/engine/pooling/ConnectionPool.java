/*
 * Copyright 2013 MagicBane Emulator Project
 * All Rights Reserved   
 */
package engine.pooling;

import engine.gameManager.ConfigManager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionPool extends LinkedObjectPool<Connection> {

	static {
		//Register the Driver
		try {
			Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
		} catch (InstantiationException | ClassNotFoundException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

	public ConnectionPool() {
		super(10);
	}

	@Override
	protected Connection makeNewObject() {
		// Protocol
		String sqlURI = "jdbc:mysql://";
		sqlURI += ConfigManager.MB_DATABASE_ADDRESS.getValue() + ':' + ConfigManager.MB_DATABASE_PORT.getValue();
		sqlURI += '/' + ConfigManager.MB_DATABASE_NAME.getValue() + '?';
		sqlURI += "useServerPrepStmts=true";
		sqlURI += "&cachePrepStmts=false";
		sqlURI += "&cacheCallableStmts=true";
		sqlURI += "&characterEncoding=utf8";

		Connection out = null;
		try {
			out = DriverManager.getConnection(sqlURI, ConfigManager.MB_DATABASE_USER.getValue(),
					ConfigManager.MB_DATABASE_PASS.getValue());
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return out;
	}

	@Override
	protected void resetObject(Connection obj) {

	}
}

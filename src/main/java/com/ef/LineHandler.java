package com.ef;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.function.Consumer;

/**
 *
 * @author natc <nathaniel.camomot@legalmatch.com>
 */
public class LineHandler implements Consumer<String[]> {

	public LineHandler() {
		String userName = "root";
		String password = "";
		String url = "jdbc:mysql://localhost:3306/library";

		// Connection is the only JDBC resource that we need
		// PreparedStatement and ResultSet are handled by jOOQ, internally
		try (Connection conn = DriverManager.getConnection(url, userName, password)) {
			// ...
		} // For the sake of this tutorial, let's keep exception handling simple
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void accept(String[] t) {

	}

}

package com.ef;

import org.h2.jdbcx.JdbcDataSource;
import org.h2.tools.RunScript;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

public class TestUtils {

    public static String inetNToA(long intIP) {
        try {
            return InetAddress.getByName(String.valueOf(intIP)).getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static long inetAToN(String ipAddress) {
        String[] ipAddressInArray = ipAddress.split("\\.");

        long result = 0;
        for (int i = 0; i < ipAddressInArray.length; i++) {

            int power = 3 - i;
            int ip = Integer.parseInt(ipAddressInArray[i]);
            result += ip * Math.pow(256, power);

        }
        return result;
    }

    public static DataSource createTestDataSource() {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:testdb;mode=mysql;INIT=RUNSCRIPT FROM 'classpath:create_test_tables.sql'");
        ds.setUser("sa");
        ds.setPassword("");

        return new DataSource() {

            @Override
            public Connection getConnection() throws SQLException {
                Connection conn = ds.getConnection();
                RunScript.execute(conn, new StringReader("CREATE ALIAS INET_NTOA FOR \"com.ef.TestUtils.inetNToA\";"));
                RunScript.execute(conn, new StringReader("CREATE ALIAS INET_ATON FOR \"com.ef.TestUtils.inetAToN\";"));
                return conn;
            }

            @Override
            public Connection getConnection(String username, String password)  {
                return null;
            }

            @Override
            public <T> T unwrap(Class<T> iface)  {
                return null;
            }

            @Override
            public boolean isWrapperFor(Class<?> iface)  {
                return false;
            }

            @Override
            public PrintWriter getLogWriter()  {
                return null;
            }

            @Override
            public void setLogWriter(PrintWriter out)  {

            }

            @Override
            public void setLoginTimeout(int seconds) {

            }

            @Override
            public int getLoginTimeout()  {
                return 0;
            }

            @Override
            public Logger getParentLogger()  {
                return null;
            }
        };
    }
}

package com.ef;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author natc <nathanielcamomot@gmail.com>
 */
class IpChecker {
    private static final Logger log = LoggerFactory.getLogger(IpChecker.class);
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final DataSource dataSource;

    public IpChecker(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public String query(LocalDateTime startDate, Duration duration, int threshold) throws SQLException {
        log.debug("Checking IPs, {} {} {}", startDate, duration, threshold);
        // try-resources, auto close
        try (
                Connection conn = dataSource.getConnection();
                Statement stmt = conn.createStatement();
                PreparedStatement insert =
                        conn.prepareStatement("INSERT INTO blocked(ip, reason) VALUES (?, ?)")

        ) {
            ResultSet result = stmt.executeQuery(
                    "SELECT ip, inet_ntoa(ip), count(ip) hits " +
                            "FROM access_logs " +
                            "WHERE ts BETWEEN '" + startDate.format(formatter) +
                            "' AND date_add('" + startDate.format(formatter) + "', INTERVAL 1 " +
                            (duration == Duration.DAILY ? "DAY) " : "HOUR) ") +
                            "GROUP BY ip " +
                            "HAVING hits >= " + threshold
            );

            System.out.println("Blocked IPs: ");
            while (result.next()) {
                System.out.println(result.getString(2) + " - " + result.getInt(3) + " requests");
                insert.setLong(1, result.getLong(1));
                insert.setString(2, result.getInt(2) + " - " + result.getInt(3) +
                        " requests (startDate=" + startDate.format(formatter) + ", duration=" + duration + ", threshold=" + threshold + ")");
                insert.addBatch();
            }
            insert.executeBatch();
        }
        return "test";
    }
}

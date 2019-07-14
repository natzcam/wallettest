package com.ef;

import ch.qos.logback.classic.Level;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

import static picocli.CommandLine.Option;

/**
 * @author natc <nathanielcamomot@gmail.com>
 */
@SuppressWarnings({"CanBeFinal", "FieldCanBeLocal"})
@Command(
        description = "Checks if an IP made requests over the threshold!",
        version = "1.0-SNAPSHOT"
)
class Parser implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(Parser.class);

    @Option(names = {"--startDate"}, description = "start date-time", required = true)
    private LocalDateTime startDate;

    @Option(names = {"--duration"}, description = "hourly, daily", required = true)
    private Duration duration;

    @Option(names = {"--threshold"}, description = "threshold", required = true)
    private int threshold;

    @Option(names = {"--accesslog"}, description = "access log")
    private File accessLog;

    @Option(names = "--debug", description = "show verbose logging (default: ${DEFAULT-VALUE})")
    private boolean debug = false;

    @Option(names = "--config", description = "app configuration file (default: ${DEFAULT-VALUE})")
    private File appConfig = new File("app.properties");

    @Option(names = "--datasource", description = "datasource configuration file (default: ${DEFAULT-VALUE})")
    private File dataSourceConfig = new File("datasource.properties");

    private DataSource createDataSource(Properties config) {
        // use connection pooling because we are in multithreaded territory
        HikariConfig hikariConfig = new HikariConfig(config);
        return new HikariDataSource(hikariConfig);
    }

    private void prepareTables(DataSource dataSource) throws SQLException, IOException {
        try (
                Connection conn = dataSource.getConnection();
                Statement stmt = conn.createStatement()
        ) {
            stmt.execute(Utils.streamToString(getClass().getResourceAsStream("/create_tables.sql")));
        }
    }

    private void cleanLogTable(DataSource dataSource) throws SQLException, IOException {
        try (
                Connection conn = dataSource.getConnection();
                Statement stmt = conn.createStatement()
        ) {
            stmt.execute("TRUNCATE TABLE access_logs");
        }
    }

    @Override
    public void run() {

        // by default level is info, but if debug option is set, raise the root logger to debug level
        if (debug) {
            ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
            root.setLevel(Level.DEBUG);
        }

        try {
            Config config = new Config(appConfig, dataSourceConfig);
            DataSource dataSource = createDataSource(config.getDataSourceProperties());

            // ensure tables
            prepareTables(dataSource);

            if (accessLog != null) {
                if (accessLog.exists()) {
                    if (accessLog.isFile()) {

                        //clean data
                        cleanLogTable(dataSource);

                        DBLoader dbLoader = new DBLoader(dataSource, config.getThreads(), config.getQueueLimit());
                        dbLoader.load(accessLog, config.getDelimiter(), config.getBatchSize());
                    } else {
                        log.warn("--accesslog is not a file, skipping batch load");
                    }
                } else {
                    log.warn("--accesslog does not exist, skipping batch load");
                }
            } else {
                log.warn("You did not specify --accesslog, skipping batch load");
            }

            IpChecker ipChecker = new IpChecker(dataSource);
            ipChecker.query(startDate, duration, threshold);
        } catch (Exception e) {
            log.error("Error running Parser", e);
        }
    }


    public static void main(String[] args) {
        CommandLine cmd = new CommandLine(new Parser());
        cmd.setCaseInsensitiveEnumValuesAllowed(true);
        cmd.registerConverter(LocalDateTime.class,
                (String value) -> LocalDateTime.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd.HH:mm:ss")));
        int exitCode = cmd.execute(args);
        System.exit(exitCode);
    }
}

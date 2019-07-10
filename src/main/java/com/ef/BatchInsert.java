package com.ef;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author natc <nathanielcamomot@gmail.com>
 */
class BatchInsert implements Callable<BatchResult> {
    private static final Logger log = LoggerFactory.getLogger(BatchInsert.class);
    private final long startLine;
    private final long endLine;
    private final DataSource dataSource;
    private final List<String[]> batch;
    private final int successCode;

    public BatchInsert(long startLine, int endLine, DataSource dataSource, List<String[]> batch, int successCode) {
        this.startLine = startLine;
        this.endLine = endLine;
        this.dataSource = dataSource;
        this.batch = batch;
        this.successCode = successCode;
    }

    @Override
    public BatchResult call() throws Exception {
        // try-resources, auto close
        log.debug("Processing Batch({}-{}) with {} elements", startLine, endLine, batch.size());
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn
                        .prepareStatement("INSERT INTO access_logs(ts, ip, request, status, user_agent) VALUES (?, inet_aton(?), ?, ?, ?)")
        ) {

            for (String[] entry : batch) {
                stmt.setTimestamp(1, Timestamp.valueOf(entry[0]));
                stmt.setString(2, entry[1]);
                stmt.setString(3, entry[2].replaceAll("^\"|\"$", ""));
                stmt.setInt(4, Integer.parseInt(entry[3]));
                stmt.setString(5, entry[4].replaceAll("^\"|\"$", ""));
                stmt.addBatch();
            }

            return new BatchResult(startLine, endLine, stmt.executeBatch(), successCode);
        }catch(Exception e){
            throw new BatchException(startLine, endLine, e);
        }
    }
}

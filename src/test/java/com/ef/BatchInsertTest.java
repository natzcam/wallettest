package com.ef;

import org.junit.Test;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BatchInsertTest {

    private static final DataSource dataSource = TestUtils.createTestDataSource();

    @Test
    public void testHappy() throws Exception {
        List<String[]> batch = new ArrayList<>();
        batch.add(new String[]{"2017-01-01 00:00:11.763", "192.168.234.82", "\"GET / HTTP/1.1\"", "200", "\"swcd (unknown version) CFNetwork/808.2.16 Darwin/15.6.0\""});
        batch.add(new String[]{"2017-01-01 00:00:11.763", "192.168.234.82", "\"GET / HTTP/1.1\"", "200", "\"swcd (unknown version) CFNetwork/808.2.16 Darwin/15.6.0\""});
        BatchInsert insert = new BatchInsert(2, 2, dataSource, batch, 1);
        BatchResult result = insert.call();

        assertTrue(result.isSuccess());
        assertEquals(2, result.getResults().length);
    }

    @Test
    public void testInvalidStatus() throws Exception {
        try {
            List<String[]> batch = new ArrayList<>();
            batch.add(new String[]{"2017-01-01 00:00:11.763", "192.168.234.82", "\"GET / HTTP/1.1\"", "hello world", "\"swcd (unknown version) CFNetwork/808.2.16 Darwin/15.6.0\""});
            batch.add(new String[]{"2017-01-01 00:00:11.763", "192.168.234.82", "\"GET / HTTP/1.1\"", "200", "\"swcd (unknown version) CFNetwork/808.2.16 Darwin/15.6.0\""});
            BatchInsert insert = new BatchInsert(2, 2, dataSource, batch, 1);
            insert.call();
            throw new RuntimeException("Should not pass");
        } catch (BatchException be) {
            assertEquals(NumberFormatException.class, be.getCause().getClass());
            assertEquals("For input string: \"hello world\"", be.getCause().getMessage());
        }
    }

    @Test
    public void testInvalidTimestamp() throws Exception {
        try {
            List<String[]> batch = new ArrayList<>();
            batch.add(new String[]{"2017-01-01 00:00:11.763", "192.168.234.82", "\"GET / HTTP/1.1\"", "200", "\"swcd (unknown version) CFNetwork/808.2.16 Darwin/15.6.0\""});
            batch.add(new String[]{"2017-01-0100:00:11.763", "192.168.234.82", "\"GET / HTTP/1.1\"", "200", "\"swcd (unknown version) CFNetwork/808.2.16 Darwin/15.6.0\""});
            BatchInsert insert = new BatchInsert(2, 2, dataSource, batch, 1);
            insert.call();
            throw new RuntimeException("Should not pass");
        } catch (BatchException be) {
            assertEquals(IllegalArgumentException.class, be.getCause().getClass());
            assertEquals("Timestamp format must be yyyy-mm-dd hh:mm:ss[.fffffffff]", be.getCause().getMessage());
        }
    }
}

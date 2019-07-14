package com.ef;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author natc <nathanielcamomot@gmail.com>
 */
class DBLoader {
    private static final Logger log = LoggerFactory.getLogger(DBLoader.class);

    private final DataSource dataSource;
    private final int threads;
    private final int queueLimit;
    private final ExecutorCompletionService<BatchResult> executor;

    public DBLoader(DataSource dataSource, int threads, int queueLimit) {
        this.dataSource = dataSource;
        this.threads = threads;
        this.queueLimit = queueLimit;

        executor = createExecutor();
    }

    private ExecutorCompletionService<BatchResult> createExecutor() {
        // Create a fixed thread pool with a **bounded** queue. If the queue limit is reached, CallerRunsPolicy kicks in, meaning
        // tasks will be run in the main thread to throttle the memory consumption
        ThreadPoolExecutor tpe = new ThreadPoolExecutor(
                threads,
                threads,
                0, TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(queueLimit),
                new ThreadFactory() {
                    final AtomicInteger threadNumber = new AtomicInteger(0);

                    @Override
                    public Thread newThread(Runnable r) {
                        Thread t = new Thread(r, "dbloader-" + threadNumber.getAndIncrement());
                        t.setDaemon(false);
                        t.setPriority(Thread.MAX_PRIORITY);
                        return t;
                    }
                },
                new ThreadPoolExecutor.CallerRunsPolicy());

        return new ExecutorCompletionService<>(tpe);
    }

    public void load(File logFile, String delimiter, int batchSize) throws IOException {
        log.debug("Using {} threads to load {}", threads, logFile);

        List<String[]> buffer = new ArrayList<>(batchSize);
        long lineNum = 0;
        int batchCount = 0;
        String line;
        try (BufferedReader br = new BufferedReader(new FileReader(logFile))) {
            while ((line = br.readLine()) != null) {
                lineNum++;
                if (!line.isEmpty()) {
                    buffer.add(line.split(delimiter));
                    if (buffer.size() == batchSize) {
                        executor.submit(new BatchInsert(lineNum - batchSize, lineNum, dataSource, new ArrayList<>(buffer), Statement.SUCCESS_NO_INFO));
                        batchCount++;
                        buffer.clear();
                    }
                }
            }

            if (!buffer.isEmpty()) {
                executor.submit(new BatchInsert(lineNum - batchSize, lineNum, dataSource, buffer, Statement.SUCCESS_NO_INFO));
                batchCount++;
            }
        }

        System.out.println("===============================================");
        long done = 0;
        while (batchCount > 0) {
            try {
                // block until a callable completes
                BatchResult result = executor.take().get();
                batchCount--;
                log.debug(result.getMessage());
                System.out.write(("\r" + (done += result.getResults().length) + "/" + lineNum).getBytes(StandardCharsets.UTF_8));
            } catch (ExecutionException e) {
                // BatchException
                log.error("Batch failed", e.getCause());
            } catch (InterruptedException e) {
                log.error("Interrupted while waiting", e);
                return;
            }
        }
        System.out.println(" Done with load using " + threads + " threads!");
        System.out.println("===============================================");
    }
}

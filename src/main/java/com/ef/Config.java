package com.ef;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * @author natc <nathanielcamomot@gmail.com>
 */
class Config {
    private final Properties appProperties;
    private final Properties dataSourceProperties;

    public Config(File appConfig, File dataSourceConfig) throws IOException {
        appProperties = new Properties();
        appProperties.load(new FileInputStream(appConfig));

        dataSourceProperties = new Properties();
        dataSourceProperties.load(new FileInputStream(dataSourceConfig));
    }

    public Config(Properties appProperties, Properties dataSourceProperties) {
        this.appProperties = appProperties;
        this.dataSourceProperties = dataSourceProperties;
    }

    public Properties getDataSourceProperties() {
        return dataSourceProperties;
    }

    public String getDelimiter() {
        return appProperties.getProperty("delimiter", "\\|");
    }

    public int getBatchSize() {
        return Integer.parseInt(appProperties.getProperty("batchSize", "1000"));
    }

    //20 rather than the number of processor cores because of a lot of I/O
    public int getThreads() {
        return Integer.parseInt(appProperties.getProperty("threads", "20"));
    }

    public int getQueueLimit() {
        return Integer.parseInt(appProperties.getProperty("queueLimit", "5000"));
    }
}

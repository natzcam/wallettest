CREATE TABLE IF NOT EXISTS access_logs  (
    ts TIMESTAMP(3) NOT NULL,
    ip INT(11) UNSIGNED NOT NULL,
    request VARCHAR(255) NOT NULL,
    status SMALLINT(3) NOT NULL,
    user_agent VARCHAR(2048) NOT NULL,
    INDEX idx_ts_ip (ts, ip)
);

TRUNCATE TABLE access_logs;

CREATE TABLE IF NOT EXISTS blocked (
     ip INT(11) UNSIGNED NOT NULL,
     reason VARCHAR(255)
);
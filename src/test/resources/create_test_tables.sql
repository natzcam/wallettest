CREATE TABLE access_logs  (
    ts TIMESTAMP(3) NOT NULL,
    ip BIGINT NOT NULL,
    request VARCHAR(255) NOT NULL,
    status SMALLINT(3) NOT NULL,
    user_agent VARCHAR(2048) NOT NULL,
    INDEX idx_ts_ip (ts, ip)
);

CREATE TABLE blocked (
     ip BIGINT NOT NULL,
     reason VARCHAR(255)
);
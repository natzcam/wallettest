#### Setup MySQL
If you have docker, start a mysql docker container running on port 4406 (to not conflict with local msyql) with user wallettest, pw: wallettest
```sh
docker run --name wallettest \
-e MYSQL_ROOT_PASSWORD=wallettest \
-e MYSQL_DATABASE=wallettest \
-e MYSQL_USER=wallettest \
-e MYSQL_PASSWORD=wallettest \
-p 4406:3306 \
-d mysql:5.7.26
```
If not, create a schema, username, password with your MySQL instance and take note of it

#### Download and unzip
https://github.com/natzcam/wallettest/releases/download/v1/wallettest-1.0-SNAPSHOT.zip

#### Fill `datasource.properties` with the correct values
```properties
#jdbcUrl=jdbc:mysql://localhost:{port}/{schema}
#username={user}
#password={password}

# Docker
jdbcUrl=jdbc:mysql://localhost:4406/wallettest
username=wallettest
password=wallettest
```

#### Run the program
```sh
java -cp parser.jar com.ef.Parser --startDate=2017-01-01.13:00:00 --duration=daily --threshold=250 --accesslog=access.log
```

#### Schema
The program will create the necessary tables automatically. Create table SQL is here:
[`src/main/resources/create_tables.sql`](src/main/resources/create_tables.sql)

#### Search SQL
(1)
```sql
SELECT inet_ntoa(ip), count(ip) hits
FROM access_logs
WHERE ts BETWEEN '2017-01-01.13:00:00' AND '2017-01-01.14:00:00'
GROUP BY ip
HAVING hits > 100
```

(2)
```sql
SELECT *, inet_ntoa(ip) ip_str
FROM access_logs
WHERE inet_ntoa(ip) = '192.168.77.101'
```

#### Other Configs
```properties
# the delimiter in the log file
delimiter=\\|

# the batch size to run per thread
batchSize=1000

# the number of threads in the thread pool
threads=20

# the thread pool queue limit
queueLimit=5000
```

#### Clone, run, package
```sh
git clone git@github.com:natzcam/wallettest.git
cd wallettest
./mvnw exec:java -Dexec.mainClass=com.ef.Parser -Dexec.args="--debug --accesslog=files/access.log --startDate=2017-01-01.13:00:00 --duration=daily --threshold=250"
```
```sh
# build unix
./mvnw clean package
```
```cmd
rem build windows
mvnw.cmd clean package
```

#### Notes
1. It seems that the load is slow with 1 thread so created a multi-threaded load.
2. The date condition uses BETWEEN so dates are inclusive
3. IPs are stored as numbers
4. --help to show help:
```sh
Missing required options [--startDate=<startDate>, --duration=<duration>, --threshold=<threshold>]
Usage: <main class> [--debug] [--accesslog=<accessLog>] [--config=<appConfig>]
                    [--datasource=<dataSourceConfig>] --duration=<duration>
                    --startDate=<startDate> --threshold=<threshold>
Checks if an IP made requests over the threshold!
      --accesslog=<accessLog>
                             access log
      --config=<appConfig>   app configuration file (default: app.properties)
      --datasource=<dataSourceConfig>
                             datasource configuration file (default: datasource.
                               properties)
      --debug                show verbose logging (default: false)
      --duration=<duration>  hourly, daily
      --startDate=<startDate>
                             start date-time
      --threshold=<threshold>
                             threshold
```
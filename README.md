#### Setup MySQL
If you have docker, start a mysql docker container running on port 33060 (to not conflict with local msyql) with user wallettest, pw: wallettest
```sh
docker run --name wallettest \
-e MYSQL_ROOT_PASSWORD=wallettest \
-e MYSQL_DATABASE=wallettest \
-e MYSQL_USER=wallettest \
-e MYSQL_PASSWORD=wallettest \
-p 33060:3306 \
-d mysql:5.7.26
```
If not, create a schema, username, password with your MySQL instance and take note of it

#### Download and unzip
https://github.com/natzcam/wallettest/releases/download/v1/wallettest-1.0-SNAPSHOT.zip

#### Fill `datasource.properties` with the correct values
```properties
# Docker
# jdbcUrl=jdbc:mysql://localhost:33060/wallettest
# username=wallettest
# password=wallettest

jdbcUrl=jdbc:mysql://localhost:{port}/{schema}
username={user}
password={password}
```

#### Run the program
```sh
java -cp target/parser.jar com.ef.Parser --startDate=2017-01-01.13:00:00 --duration=daily --threshold=250 --accesslog=access.log
```

#### Create the jar
Clone the repository then run
unix
```sh
./mvnw clean package
```
win
```sh
./mvnw.cmd clean package
```

#### Schema
The program will create the necessary tables automatically. SQL is here:
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
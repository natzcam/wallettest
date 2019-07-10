docker run --name wallethub \
-e MYSQL_ROOT_PASSWORD=wallethub \
-e MYSQL_DATABASE=wallethub \
-e MYSQL_USER=wallethub \
-e MYSQL_PASSWORD=wallethub \
-p 3306:3306 \
-d mysql:5.7.26
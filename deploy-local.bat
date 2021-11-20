rem Secuencia de comandos para despliegue en desarrollo local
rem https://samples-test-spring-develop.herokuapp.com/
heroku login
heroku container:login
mvn clean package -DskipTests=true
copy target\*-deploy.jar target\samples-test-spring-latest-deploy.jar
docker build -t samples-test-spring-develop .
heroku container:push web --app samples-test-spring-develop
heroku container:release web --app samples-test-spring-develop
heroku logs --app samples-test-spring-develop

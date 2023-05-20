#FROM openjdk:8-jre-slim-buster
FROM eclipse-temurin:20-jre-alpine@sha256:fe87d656d46efc37ca335e6102a30c4154cac820a61f5392acefc8f7c6ee9d56
#EXPOSE no se tiene en cuenta en Heroku, el puerto lo pone la aplicacion en la clase principal usando la variavble PORT
EXPOSE 8080
#Para Azure definir la variable WEBSITES_PORT=8080 desde el portal: Settings->Configuration
COPY target/samples-test-spring-latest-deploy.jar .
ENTRYPOINT ["java", "-Xmx128m", "-Xss1m", "-jar", "samples-test-spring-latest-deploy.jar"]
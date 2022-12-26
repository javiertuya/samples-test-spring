#FROM openjdk:8-jre-slim-buster
FROM eclipse-temurin:19-jre-alpine@sha256:9b2cd6fe6cf39e93a76a43b1d80a5666d0a65061289c80d03834bb0a52074e3a
#EXPOSE no se tiene en cuenta en Heroku, el puerto lo pone la aplicacion en la clase principal usando la variavble PORT
EXPOSE 8080
#Para Azure definir la variable WEBSITES_PORT=8080 desde el portal: Settings->Configuration
COPY target/samples-test-spring-latest-deploy.jar .
ENTRYPOINT ["java", "-Xmx128m", "-Xss1m", "-jar", "samples-test-spring-latest-deploy.jar"]
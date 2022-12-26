#FROM openjdk:8-jre-slim-buster
FROM eclipse-temurin:8-jre-alpine@sha256:1d62faf876f3ff84f131c4f9ede0974e4753666c5efc151e1456ea6f6c075e30
#EXPOSE no se tiene en cuenta en Heroku, el puerto lo pone la aplicacion en la clase principal usando la variavble PORT
EXPOSE 8080
#Para Azure definir la variable WEBSITES_PORT=8080 desde el portal: Settings->Configuration
COPY target/samples-test-spring-latest-deploy.jar .
ENTRYPOINT ["java", "-Xmx128m", "-Xss1m", "-jar", "samples-test-spring-latest-deploy.jar"]
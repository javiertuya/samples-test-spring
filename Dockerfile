#FROM openjdk:8-jre-slim-buster
#Dependabot is ignoring updates when sha changes, use latest version
#FROM eclipse-temurin:8-jre-alpine@sha256:ab4064508d2b7427694f19fd82d66f4b381d36d714c955219dbe8d9ba8de0964
FROM eclipse-temurin:8-jre-alpine

#EXPOSE no se tiene en cuenta en Heroku, el puerto lo pone la aplicacion en la clase principal usando la variavble PORT
EXPOSE 8080
#Para Azure definir la variable WEBSITES_PORT=8080 desde el portal: Settings->Configuration
COPY target/samples-test-spring-latest-deploy.jar .
ENTRYPOINT ["java", "-Xmx128m", "-Xss1m", "-jar", "samples-test-spring-latest-deploy.jar"]
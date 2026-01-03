#Use a Docker Hardened Image. Note that this requires a login to pull the base image.
#FROM eclipse-temurin:17.0.17_10-jre-alpine
FROM dhi.io/eclipse-temurin:17-alpine3.22

#EXPOSE no se tiene en cuenta en Heroku, el puerto lo pone la aplicacion en la clase principal usando la variavble PORT
EXPOSE 8080
#Para Azure definir la variable WEBSITES_PORT=8080 desde el portal: Settings->Configuration
COPY target/samples-test-spring-latest-deploy.jar .
ENTRYPOINT ["java", "-Xmx128m", "-Xss1m", "-jar", "samples-test-spring-latest-deploy.jar"]
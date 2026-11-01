#Use a Docker Hardened Image. Note that this requires a login to pull the base image.
#FROM eclipse-temurin:17.0.17_10-jre-alpine
FROM dhi.io/eclipse-temurin:17-alpine3.21@sha256:95fa9354a179886928eab22f1f37394f94fc0f6bdcb41e81676ce96bd97a6986

#EXPOSE no se tiene en cuenta en Heroku, el puerto lo pone la aplicacion en la clase principal usando la variavble PORT
EXPOSE 8080
#Para Azure definir la variable WEBSITES_PORT=8080 desde el portal: Settings->Configuration
COPY target/samples-test-spring-latest-deploy.jar .
ENTRYPOINT ["java", "-Xmx128m", "-Xss1m", "-jar", "samples-test-spring-latest-deploy.jar"]
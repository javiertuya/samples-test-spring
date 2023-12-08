#Note that dependabot does not trigger PR if a previous closed PR exists
#even there is a new image with a different SHA.
#Do not use combined updates for docker dependencies
FROM eclipse-temurin:17-jre-alpine@sha256:aa5fc621cc5eebbf90b815973e95ca800cfcab671fdd7918a3a5c9c770bb804a

#EXPOSE no se tiene en cuenta en Heroku, el puerto lo pone la aplicacion en la clase principal usando la variavble PORT
EXPOSE 8080
#Para Azure definir la variable WEBSITES_PORT=8080 desde el portal: Settings->Configuration
COPY target/samples-test-spring-latest-deploy.jar .
ENTRYPOINT ["java", "-Xmx128m", "-Xss1m", "-jar", "samples-test-spring-latest-deploy.jar"]
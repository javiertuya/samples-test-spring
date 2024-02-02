#Note that dependabot does not trigger PR if a previous closed PR exists
#even there is a new image with a different SHA.
#Do not use combined updates for docker dependencies
FROM eclipse-temurin:17-jre-alpine@sha256:8025a8bed5f80335e763f33af95f36cd64c7ea6127ed9ed4e2ba39c6320f343d

#EXPOSE no se tiene en cuenta en Heroku, el puerto lo pone la aplicacion en la clase principal usando la variavble PORT
EXPOSE 8080
#Para Azure definir la variable WEBSITES_PORT=8080 desde el portal: Settings->Configuration
COPY target/samples-test-spring-latest-deploy.jar .
ENTRYPOINT ["java", "-Xmx128m", "-Xss1m", "-jar", "samples-test-spring-latest-deploy.jar"]
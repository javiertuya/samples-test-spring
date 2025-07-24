#Note that dependabot does not trigger PR if a previous closed PR exists
#even there is a new image with a different SHA.
#Do not use combined updates for docker dependencies
#FROM eclipse-temurin:17-jre-alpine@sha256:fcf70ae7ba37872c7d1da875593321c3e90bd9a02c6b4bfde5a1260b08b8f178
#Fixes version to allow combined updates
FROM eclipse-temurin:17.0.14_7-jre-alpine

#EXPOSE no se tiene en cuenta en Heroku, el puerto lo pone la aplicacion en la clase principal usando la variavble PORT
EXPOSE 8080
#Para Azure definir la variable WEBSITES_PORT=8080 desde el portal: Settings->Configuration
COPY target/samples-test-spring-latest-deploy.jar .
ENTRYPOINT ["java", "-Xmx128m", "-Xss1m", "-jar", "samples-test-spring-latest-deploy.jar"]
[![Build Status](https://github.com/javiertuya/samples-test-spring/actions/workflows/build.yml/badge.svg)](https://github.com/javiertuya/samples-test-spring/actions/workflows/build.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=my%3Asamples-test-spring&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=my%3Asamples-test-spring)
[![Javadoc](https://img.shields.io/badge/%20-javadoc-blue)](https://javiertuya.github.io/samples-test-spring/)

# samples-test-spring

Este proyecto es utilizado a modo de ejemplo para ilustrar algunos aspectos del desarrollo y automatización de pruebas para
las asignaturas relacionadas con ingenieria del software, sistemas de información y pruebas de softare.

[Descargar la última versión publicada](https://github.com/javiertuya/samples-test-spring/releases) - 
[Ver más detalles en el javadoc](https://javiertuya.github.io/samples-test-spring/)


## Contenido

Este proyecto ilustra:
- Diferentes configuraciones para la automatización de pruebas de aplicaciones Spring Boot (v3):
  - Pruebas unitarias con JUnit
  - Pruebas parametrizadas con JUnitParams
  - Utilización de mocks
  - Pruebas de servicios rest y controladores (MockMvc)
  - Pruebas de un interfaz de usuario web con Selenium
  - Uso de lombok para generar automaticamente getters y setters de entidades y DTOs
  - Automatización de pruebas BDD con JBehave (unitarias y de interfaz de usuario)
- Estructura de un proyecto maven y configuración el pom.xml
  - Separación de las pruebas ut e it
  - Generación de reports  estandar (Surefire y Failsafe)
  - Generación de reports de cobertura de código (JaCoCo)
  - Otros reports (resultados de test en formato JUnit html)
- Integración continua con GitHub Actions (proceso completo CI/CD):
  - Estructuración del worflow con varios jobs que se comunican mediante artefactos
  - Publicación de resultados de test que fallan
  - Configuración de selenoid como servicio de navegadores, incluyendo grabación de video de las sesiones
  - Análisis estático de calidad del código 
  (SonarQube alojado en [sonarcloud.io](https://sonarcloud.io/project/overview?id=my:samples-test-spring))
  - Análisis estático de vulnerabilidad de dependencias (OWASP Dependench Check)
  - Despliegue de la aplicación en Azure:
    - En diferentes entornos: [producción](https://samples-test-spring-main.azurewebsites.net/) 
      y [preproducción/integración](https://samples-test-spring-develop.azurewebsites.net/)
    - Post-deploy test
  - Ejemplo de despliegue en Heroku (desactivado)
- Integración continua con Jenkins (para ejecución on-premise):
  - Fichero Jenkinsfile con la configuración de la pipeline
  - Incluye acciones análogas a las usadas en GitHub Actions (excepto despliegue)

## Requisitos e Instalación

- [Descargar la última versión](https://github.com/javiertuya/samples-test-spring/releases) y disponer al menos de Java 17 JDK
- Opción 1: Apache Maven:
	- Asegurarse de que JAVA_HOME apunta a un JDK y no JRE
	- Ejecutar `mvn install`
	- (solo pruebas unitarias con `test`, todas las pruebas con `verify`)
- Opción 2: Eclipse con M2Eclipse instalado (distribuciones como Oxigen IDE for Java EE Developers ya lo incluyen).
Desde la raiz del proyecto:
	- Asegurarse de que se tiene instalado Lombok. Ver: https://projectlombok.org/setup/eclipse)
	- Asegurarse de que se tiene configurado un JDK: Desde build path, editar JRE System Library y en Environment
	comprobar que JavaSE-17 apunta a un JDK en vez de un JRE
	- *Maven->Update Project*
	- *Run As->Maven install*
	- Para ejecutar desde el entorno (run as JUnit Test) 

Última configuración con la que se ha probado: 
Windows 10/Ubuntu 20, Eclipse Temurin 17, Apache Maven 3.9.3 o Eclipse IDE for Java EE Developers (eclipse-jee-2023-06-R),
Base de datos embebida H2. El resto de dependencias son las especificadas en pom.xml.

## Reports
La instalacion anterior compilará, ejecutará pruebas y dispondrá de los reports en `target/site`:

- surefire-report.html: report de las pruebas unitarias (ut)
- failsafe-report.html: report de las pruebas del interfaz de usuario (it)
- junit*: report consolidado de todas las pruebas con el formato que genera junit
- jacoco, jacoco-it: reports de cobertura de código. separado para ut e it
- reports estandar de jbehave en `target/jbehave`
- testapidocs/index.html: javadoc del proyecto (generados en la fase `install`)

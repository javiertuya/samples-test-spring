[![Build Status](https://github.com/javiertuya/samples-test-spring/actions/workflows/build.yml/badge.svg)](https://github.com/javiertuya/samples-test-spring/actions/workflows/build.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=my%3Asamples-test-spring&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=my%3Asamples-test-spring)

# samples-test-spring

Este proyecto es utilizado a modo de ejemplo para ilustrar algunos aspectos del desarrollo y automatización de pruebas para
las asignaturas relacionadas con ingenieria del software, sistemas de información y pruebas de softare.

[Descargar la última versión](https://github.com/javiertuya/samples-test-spring/releases)

## Contenido

Este proyecto ilustra:
- Diferentes configuraciones para la automatización de pruebas de aplicaciones Spring Boot:
  - Pruebas unitarias con JUnit
  - Pruebas parametrizadas con JUnitParams
  - Utilización de mocks
  - Pruebas de servicios rest y controladores (MockMvc)
  - Pruebas de un interfaz de usuario web con Selenium
  - Automatización de pruebas BDD con JBehave (unitarias y de interfaz de usuario)
  - Uso de lombok para generar automaticamente getters y setters de entidades y DTOs
- Estructura de un proyecto maven y configuración el pom.xml
  - Separación de las pruebas ut e it
  - Generación de reports  estandar (Surefire y Failsafe)
  - Generación de reports de cobertura de código (JaCoCo)
  - Otors reports (resultados de test en formato JUnit html)
- Proceso completo de integración continua con GigHub Actions:
  - Estructuración del worflow con varios jobs que se comunican mediante artefactos
  - Configuración de selenoid como servicio de navegadores, incluyendo grabación de video de las sesiones
  - Análisis estático de calidad del código (SonarQube alojado en [sonarcloud.io](https://sonarcloud.io/organizations/giis/projects))
  - Análisis estático de vulnerabilidad de dependencias (OWASP Dependench Check)

## Requisitos e Instalación

- [Descargar la última versión](https://github.com/javiertuya/samples-test-spring/releases) y disponer al menos de Java 8 JDK
- Opción 1: Apache Maven:
	- Asegurarse de que JAVA_HOME apunta a un JDK y no JRE
	- Ejecutar `mvn install`
	- (solo pruebas unitarias con `test`, todas las pruebas con `verify`)
- Opción 2: Eclipse con M2Eclipse instalado (algunas distribuciones como Oxigen IDE for Java EE Developers ya lo incluyen).
Desde la raiz del proyecto:
	- Asegurarse de que esta configurado JDK: Desde build path, editar JRE System Library y en Environment
	comprobar que JavaSE-1.8 apunta a un JDK en vez de un JRE
	- *Maven->Update Project*
	- *Run As->Maven install*
	- Para ejecutar desde el entorno (run as JUnit Test o run as Spring Boot App) 
	se requiere la instalacion de Lombok. Ver: https://projectlombok.org/setup/eclipse

Configuración con la que se ha probado: Windows 10/Ubuntu 18. Apache Maven 3.6.3 o Eclipse IDE for Java EE Developers (jee-2019-03-R)
con plugin Spring Tools 3.
Spring Boot 2.0.4. Base de datos embebida H2. El resto de dependencias son las especificadas en pom.xml.

## Reports
La instalacion anterior compilará, ejecutará pruebas y dispondrá de los reports en `target/site`:

- apidocs/index.html: javadoc del proyecto
- surefire-report.html: report de las pruebas unitarias (ut)
- failsafe-report.html: report de las pruebas del interfaz de usuario (it)
- junit*: report consolidado de todas las pruebas con el formato que genera junit
- jacoco, jacoco-ut, jacoco-it: reports de cobertura de código consolidado, y separado para ut e it
- reports estandar de jbehave en `target`

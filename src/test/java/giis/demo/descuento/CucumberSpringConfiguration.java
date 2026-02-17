package giis.demo.descuento;

import io.cucumber.spring.CucumberContextConfiguration;

/**
 * Usada para evitar problemas con la configuracion al tener dos paquetes independientes de pruebas con
 * Cucumber (UT e IT)
 */
@CucumberContextConfiguration
public class CucumberSpringConfiguration {
}

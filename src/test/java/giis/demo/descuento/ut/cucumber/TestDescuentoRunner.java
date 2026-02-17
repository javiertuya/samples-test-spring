package giis.demo.descuento.ut.cucumber;

import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

/**
 * Ejecutor de los tests cucumber de pruebas unitarias (UT) para DataJpaTest.
 * 
 * Al tener dos paquetes independientes con cucumber (UT e IT) y los archivos .feature dentro de cada paquete,
 * cuando se ejecuta con maven aparece un problema que causa que Cucumber no localice correctamente los steps
 * y el setUp de cada uno. Para evitarlo que se anyade la configuracion de cucumber.clue, que de otra forma no
 * seria necesaria.
 * 
 * Ademas, al ejecutar desde eclipse de forma independiente UT o IT, causa un error que indica que hay varias
 * clases anotadas con CucumberContextConfiguration en dos lugares debido a que escanea el classpath completo.
 * Para evitarlo se anyade una clase anotada con CucumberContextConfiguration por encima de todos los tests.
 */
@Suite // Activa el runner de JUnit
@IncludeEngines("cucumber") // Usando el motor de Cucumber
@SelectClasspathResource("giis/demo/descuento/ut/cucumber") // los .feature estaran dentro de este paquete
// Normalmente no necesitaria especificar los glue porque los buscaria en este paquete, pero
// es necesiario aqui debido a que hay diferentes paquetes independients
@ConfigurationParameter(key = "cucumber.glue", value = "giis.demo.descuento.ut.cucumber")
// Opciones para definicion de reporting
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty, html:target/cucumber/reports-ut.html")
public class TestDescuentoRunner {
}
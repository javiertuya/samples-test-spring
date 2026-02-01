package giis.demo.descuento.ut.cucumber;

import org.junit.runner.RunWith;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;

/**
 * Ejecutor de los tests cucumber de este paquete
 */
@RunWith(Cucumber.class)
@CucumberOptions(plugin = { "pretty" , "html:target/cucumber/reports-ut.html" }) // para mostrar los escenarios en la consola
// no especifica features porque por defecto las localiza donde esta este fichero
// no especifica glue porque por defecto usa todos los steps de este paquete
public class TestDescuentoRunner {
}
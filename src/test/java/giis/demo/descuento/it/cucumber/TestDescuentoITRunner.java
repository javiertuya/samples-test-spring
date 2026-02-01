package giis.demo.descuento.it.cucumber;

import org.junit.runner.RunWith;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;

/**
 * Ejecutor de los tests cucumber de este paquete
 */
@RunWith(Cucumber.class)
// no especifica glue porque por defecto usa todos los steps de este paquete
@CucumberOptions(plugin = { "pretty" , "html:target/reports/cucumber-it.html" })
public class TestDescuentoITRunner {
}
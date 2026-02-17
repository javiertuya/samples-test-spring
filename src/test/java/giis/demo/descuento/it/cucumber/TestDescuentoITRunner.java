package giis.demo.descuento.it.cucumber;

/**
 * Ejecutor de los tests cucumber de pruebas IT con Selenium.
 * 
 * Ver comentarios sobre la configuracion en las pruebas UT.
 */
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("giis/demo/descuento/it/cucumber")
@ConfigurationParameter(key = "cucumber.glue", value = "giis.demo.descuento.it.cucumber")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty, html:target/cucumber/reports-it.html")
public class TestDescuentoITRunner {
}
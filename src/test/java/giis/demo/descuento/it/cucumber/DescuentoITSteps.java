package giis.demo.descuento.it.cucumber;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;

import giis.demo.descuento.DescuentoApplication;
import giis.demo.descuento.it.SeleniumUtil;
import giis.demo.util.Util;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;

/**
 * Define el mapping (glue) como en DescuentoDatabaseSteps pero con un contexto web de Spring.
 * Notar que Before y After son clases de Cucumber, no de JUnit
 */
@CucumberContextConfiguration // para integracion de Cucumber con Spring
@SpringBootTest(classes = { DescuentoApplication.class }, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DescuentoITSteps {
	// Mismas declaraciones que en TestDescuentoSelenium, incluyendo el puerto dinamico
	@Autowired
	private javax.sql.DataSource datasource;
	@LocalServerPort
	int port;
	WebDriver driver;

	@Before
	public void setUp() {
		driver = SeleniumUtil.getNewDriver();
	}

	@After
	public void tearDown() {
		driver.quit();
	}

	@Given("los siguientes clientes en base de datos:")
	public void setClientes(List<Map<String, String>> clientes) {
		// Con WebEnvironment.RANDOM_PORT no hay transaccion con rollback por escenario (ver TestDescuentoSelenium),
		// por lo que es necesario eliminar explicitamente los datos antes de insertar
		JdbcTemplate db = new JdbcTemplate(datasource);
		db.execute("delete from cliente");
		String sql = "insert into cliente(id,edad,nuevo,cupon,tarjeta) values (?,?,?,?,?)";
		List<Object[]> params = new ArrayList<>();
		for (Map<String, String> row : clientes) {
			Object[] paramsarray = new Object[] { row.get("id"), row.get("edad"), row.get("nuevo"), row.get("cupon"), row.get("tarjeta") };
			params.add(paramsarray);
		}
		db.batchUpdate(sql, params);
	}

	@When("Se inicia la ventana")
	public void setApplication() {
		driver.get(SeleniumUtil.getApplicationUrl(port));
		find(By.linkText("Ejecutar descuentos de clientes")).click();
	}

	@When("se cambia la edad a {word}")
	public void setEdad(String edad) {
		doSetEdad(edad);
	}

	@When("se elimina la edad")
	public void setEdad() {
		doSetEdad("");
	}

	private void doSetEdad(String edad) {
		WebElement txtEdad = find(By.id("txtEdad"));
		txtEdad.clear(); // si no se limpia antes, sendKeys concatenara con el texto existente
		txtEdad.sendKeys(edad);
		find(By.id("btnEdad")).click();
	}

	@Then("los descuentos visualizados son:")
	public void getDescuentos(List<Map<String, String>> descuentos) {
		// busca la tabla en el navegador, obtiene el texto de las celdas y la compara como string csv
		WebElement tab = find(By.id("tabDescuentos"));
		String[][] arrays = SeleniumUtil.getTableContent(tab);
		String actual = Util.arraysToCsv(arrays, null, "|", "|", "|");
		String expected = Util.mapsToCsv(descuentos, new String[] { "Id", "% Descuento" }, true, "|", "|", "|", "");
		assertEquals(expected, actual);
	}
	
	private WebElement find(By locator) {
		return SeleniumUtil.findUsingWaitUntilVisible(driver, locator);
	}

}
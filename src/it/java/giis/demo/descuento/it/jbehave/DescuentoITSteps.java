package giis.demo.descuento.it.jbehave;
import static org.junit.Assert.assertEquals;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.jbehave.core.annotations.AfterScenario;
import org.jbehave.core.annotations.BeforeScenario;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.jbehave.core.model.ExamplesTable;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import giis.demo.descuento.it.SeleniumUtil;
import giis.demo.util.*;

/**
 * Define el mapping de los pasos para descuento-database.story para ejecucion con Selenium.
 * Maneja las estructuras tabulares que representan tablas en base de datos o en la salida de la aplicacion
 * representadas con variables de tipo ExamplesTable 
 * (https://jbehave.org/reference/stable/tabular-parameters.html,
 * https://jbehave.org/reference/stable/javadoc/core/org/jbehave/core/model/ExamplesTable.html).
 * Las comparaciones de tablas se realizan comparando la representacion de estas tablas como strings
 * de forma similar a cuando se ejecutan las pruebas unitarias, cambiando solamente la forma en que se cargan los
 * datos de prueba.
 */
public class DescuentoITSteps {
	//para el acceso a la base de datos con JdbcTemplate
	@Autowired private DataSource datasource;

	//Declara el driver de Selenium usado por las pruebas
	WebDriver driver;
    
    @BeforeScenario
    public void beforeEachScenario() {
		//Crea una instancia del driver que abrira el navegador
		driver=SeleniumUtil.getNewDriver();
    }
    @AfterScenario
    public void afterEachScenario() {
		//cierra el navegador y finaliza la sesion del driver (si se quiere cerrar solo el navegador usar close)
		driver.quit(); 
    }
	@Given("los siguientes clientes en base de datos: $clientesbd")
    public void setClientes(ExamplesTable clientes) {
    	//en este caso es necesario limpiar previamente pues no se ejecuta desde el runner de spring
    	JdbcTemplate db=new JdbcTemplate(datasource);
    	db.execute("delete from cliente");
    	String sql="insert into cliente(id,edad,nuevo,cupon,tarjeta) values (?,?,?,?,?)";
    	List<Object[]> params=new ArrayList<>();
    	for (Map<String,String> row : clientes.getRows()) {
    		Object[] paramsarray=new Object[] {row.get("id"), row.get("edad"), row.get("nuevo"), row.get("cupon"), row.get("tarjeta")} ;
    		params.add(paramsarray);
    	}
    	db.batchUpdate(sql,params);
    }
    @When("Se inicia la ventana")
    public void setApplication() {
    	int port=TestDescuentoITRunner.getPort();
		driver.get(SeleniumUtil.getApplicationUrl(port));
		driver.findElement(By.linkText("Ejecutar descuentos de clientes")).click();
    }
    @When("se cambia la edad a $edad")
    public void setEdad(String edad) {
		doSetEdad(edad);				
    }
    @When("se elimina la edad")
    public void setEdad() {
    	doSetEdad("");
    }
    private void doSetEdad(String edad) {
		WebElement txtEdad;
		txtEdad = (new WebDriverWait(driver, Duration.ofSeconds(5))).until(ExpectedConditions.presenceOfElementLocated(By.id("txtEdad")));
		txtEdad.clear(); //si no se limpia antes, sendKeys concatenara con el texto existente
		txtEdad.sendKeys(edad);
		driver.findElement(By.id("btnEdad")).click();					   	
    }
    @Then("los descuentos visualizados son: $descuentos")
    public void getDescuentos(ExamplesTable descuentos) {
		//busca la tabla en el navegador, obtiene el texto de las celdas y la compara como string csv
		WebElement tab=driver.findElement(By.id("tabDescuentos"));
		String[][] arrays=SeleniumUtil.getTableContent(tab);
		assertEquals(descuentos.asString(), Util.arraysToCsv(arrays,null,"|","|","|"));
     }
}
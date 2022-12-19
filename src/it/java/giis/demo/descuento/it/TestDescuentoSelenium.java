package giis.demo.descuento.it;
import org.junit.*;
import org.junit.runner.RunWith;
import static org.junit.Assert.assertEquals;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import giis.demo.descuento.*;
import giis.demo.util.*;

/**
 * Pruebas de la interaccion del usuario con la aplicacion web del ejemplo de descuentos a clientes 
 * (Problema 3d)
 * utilizando Selenium (https://www.seleniumhq.org/):
 * Ilustra la configuracion para pruebas de integracion con el interfaz web de usuario en Spring Boot
 * y el proceso basico de ejecucion y comparacion de resultados con Selenium.
 * <br/>Las situaciones a cubrir son las mismas que en samples-test-java (giis.demo.descuento.it.assertj).
 * <br/>La configuracion es la siguiente:
 * 
 * <br/>SpringBootTest: Al igual que en las pruebas unitarias (TestDescuentoRepository) se reutiliza
 * la configuracion de la aplicacion, pero se anyade la especificacion del entorno (webEnvironment).
 * Esto permite que se ejecute la instancia del servidor tomcat embebido con el que se puede interaccionar.
 * Se especifica RANDOM_PORT para que la prueba no interfiera con otras aplicaciones que puedan estar 
 * ejecutandose (por ejemplo en CI).
 * <br/>TestPropertySource: Igual que en las pruebas unitarias (TestDescuentoReposotory)
 * Especifica un fichero de configuracion diferente (localizado en src/test/resources), 
 * en este caso solo se omite la carga de datos inicial de data.sql para que se arranque con una base de datos limpia
 * (esto se podria hacer utilizando un profile diferente con su propia configuracion)
 * <br/>RunWith: Se ha de especificar el runner especifico para Spring Booth (si no, las anotaciones son ignoradas)
 * <br/>Notar que no se especifica DataJpaTest pues interfiere con la configuracion especificada de webEnvironment
 * y falla al intentar lanzar el servidor. 
 * Si se indica ServletWebServerFactoryAutoConfiguration.class en las clasess de SpringBootTest
 * El servidor se configura pero incorrectamente sin mapear los endpoints (posible bug?)
 */
@SpringBootTest(classes= {DescuentoApplication.class},
	webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations="classpath:application-test.properties")
//@TestPropertySource(properties = "server.port=8080")
@RunWith(SpringRunner.class)
public class TestDescuentoSelenium {
	//datasource para acceso a la base de datos mediante sql con JdbcTemplate
	@Autowired private javax.sql.DataSource datasource;
	//Como se especifico WebEnvironment.RANDOM_PORT, esta variable identifica el puerto utilizado por el servidor
	@LocalServerPort int port;
	//Declara el driver de Selenium usado por las pruebas
	WebDriver driver;

	@Before
	public void setUp() {
		loadCleanDatabase();
		loadMainPage();
	}
	@After
	public void tearDown() {
		driver.quit(); //cierra el navegador y finaliza la sesion del driver (si se quiere cerrar solo el navegador usar close) 
	}
	/**
	 * Inicializa el WebDriver para el navegador indicado y navega a la pagina principal.
	 * Inserta sleeps tras cada accion para que se pueda ver la secuencia de pasos en los videos
	 */
	private void loadMainPage() {
		//Crea una instancia del driver que abrira el navegador
		driver=SeleniumUtil.getNewDriver();
		//se dirige a la pagina principal
		driver.get(SeleniumUtil.getApplicationUrl(port));
		//ilustra como guardar la imagen del navegador en este momento (el nombre identificara los parametros del paso)
		SeleniumUtil.takeScreenshot(driver, "main-menu");
		SeleniumUtil.sleep(600);
		//selecciona el link para ir a la pagina que se va a probar
		driver.findElement(By.linkText("Ejecutar descuentos de clientes")).click();
		SeleniumUtil.takeScreenshot(driver, "main-application");
		SeleniumUtil.sleep(600);
	}
	/**
	 * Datos de prueba que se cargaran en el setup para cubrir las situaciones del disenyo de la prueba.
	 * Notar que no se eliminan los datos antes de ejecutar porque el runner establece una transaccion y hace
	 * rollback al finalizar el test case, asegurando siempre base de datos limpia.
	 */
	public void loadCleanDatabase() {
		//La carga se realiza directqamente sobre la base de datos. 
		//Esto perimite que los sripts de creacion de datos de prueba sean guardados en fichero externos
		//cuando se trate de inicializar muchas filas en muchas tablas
		JdbcTemplate database=new JdbcTemplate(datasource);
		database.execute("delete from cliente");
		String sql="insert into cliente(id,edad,nuevo,cupon,tarjeta) values"
				+"(1,18,'S','N','N'),"
				+"(2,38,'S','S','N'),"
				+"(3,21,'S','N','S'),"
				+"(4,25,'N','N','N'),"
				+"(5,40,'N','S','N'),"
				+"(6,42,'N','N','S'),"
				+"(7,39,'N','S','S')";
		database.execute(sql);
	}

	/**
	 * Escenario de prueba de la pantalla: 
	 * Cuatro pasos cambiando el filtro por edad, comprobando en cada uno el valor de la tabla de descuentos.
	 * Como los pasos son similares, encapsula el proceso de ejecucion y comparacion de resultados en 
	 * un metodo especifico, eliminando codigo duplicado.
	 */
	@Test
	public void testDescuentoScenario() {
		doStep(true,"","Id,% Descuento\n"
				+"1,15\n"
				+"2,20\n"
				+"5,20\n"
				+"6,10\n"
				+"7,30\n");
		doStep(false, "40", "Id,% Descuento\n"
				+"5,20\n"
				+"6,10\n");
		doStep(false, "39", "Id,% Descuento\n"
				+"5,20\n"
				+"6,10\n"
				+"7,30\n");
		doStep(false,"","Id,% Descuento\n"
				+"1,15\n"
				+"2,20\n"
				+"5,20\n"
				+"6,10\n"
				+"7,30\n");
	}
	/**
	 * La ejecucion de un paso ilustra el uso de las acciones basicas de selenium (findElement, click, sendKeys)
	 * y otras utilidades (obtener el contenido de una tabla, tomar imagenes)
	 */
	private void doStep(boolean initialStep, String edad, String expected) {
		WebElement txtEdad;
		//La busqueda de elementos se realiza con findElement, pasando un argumento que indica el criterio.
		//Por ejmplo, para localizar el campo de texto con la edad:
		//  txtEdad=driver.findElement(By.id("txtEdad"));
		//En ocasiones, si no se establecen tiempos de espera entre acciones, es posible que se abra 
		//la pagina deseada del navegador, pero que todavia no esten presentes los diferentes elementos, 
		//causando una excepcion. 
		//Esto es tipico cuando se usa/incluye javascript, y tambien depende del navegador y la velocidad del equipo.
		//Este elemento se obtendra utilizando una espera explicita
		txtEdad = (new WebDriverWait(driver, Duration.ofSeconds(5)))
			.until(ExpectedConditions.presenceOfElementLocated(By.id("txtEdad")));
		//Existen tambien formas de establecer un tiempo implicito para el driver.
		//Ver: https://www.seleniumhq.org/docs/04_webdriver_advanced.jsp#explicit-and-implicit-waits
		
		if (initialStep) {
			assertEquals("",txtEdad.getText()); //asegura que no hay texto
		} else { //pone la edad y envia el formulario
			txtEdad.clear(); //si no se limpia antes, sendKeys concatenara con el texto existente
			txtEdad.sendKeys(edad);
			driver.findElement(By.id("btnEdad")).click();					
		}
		//ilustra como guardar la imagen del navegador en este momento (el nombre identificara los parametros del paso)
		SeleniumUtil.takeScreenshot(driver, initialStep+"-"+edad);
		SeleniumUtil.sleep(600);
		//comprueba el estado del filtro aplicado tras el post
		assertEquals("".equals(edad) ? "n/a" : edad, driver.findElement(By.id("filtro")).getText());
		//busca la tabla en el navegador, obtiene el texto de las celdas y la compara como string csv
		WebElement tab=driver.findElement(By.id("tabDescuentos"));
		String[][] arrays=SeleniumUtil.getTableContent(tab);
		assertEquals(expected, Util.arraysToCsv(arrays));
		SeleniumUtil.sleep(600);
	}

}

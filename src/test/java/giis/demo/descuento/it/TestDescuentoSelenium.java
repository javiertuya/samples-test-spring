package giis.demo.descuento.it;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

import giis.demo.descuento.DescuentoApplication;
import giis.demo.util.Util;

/**
 * Pruebas de la interaccion del usuario con la aplicacion web del ejemplo de descuentos a clientes (Problema
 * 3d) utilizando Selenium (https://www.seleniumhq.org/): Ilustra la configuracion para pruebas de integracion
 * con el interfaz web de usuario en Spring Boot y el proceso basico de ejecucion y comparacion de resultados
 * con Selenium. 
 * 
 * <br/>
 * Las situaciones a cubrir son las mismas que en samples-test-java (giis.demo.descuento.it.assertj). <br/>
 * La configuracion es la siguiente:
 * 
 * <br/>
 * SpringBootTest: Al igual que en las pruebas unitarias (TestDescuentoRepository) se reutiliza la
 * configuracion de la aplicacion, pero se anyade la especificacion del entorno (webEnvironment). Esto permite
 * que se ejecute la instancia del servidor tomcat embebido con el que se puede interaccionar. Se especifica
 * RANDOM_PORT para que la prueba no interfiera con otras aplicaciones que puedan estar ejecutandose (por
 * ejemplo en CI).
 * 
 * <br/>
 * TestPropertySource: Igual que en las pruebas unitarias (TestDescuentoReposotory) Especifica un fichero de
 * configuracion diferente (localizado en src/test/resources), en este caso solo se omite la carga de datos
 * inicial de data.sql para que se arranque con una base de datos limpia (esto se podria hacer utilizando un
 * profile diferente con su propia configuracion)
 * 
 * <br/>
 * Notar que no se especifica DataJpaTest pues interferiría con la configuracion especificada de webEnvironment
 * fallando al intentar lanzar el servidor.
 */
@SpringBootTest(classes = { DescuentoApplication.class }, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-test.properties")
//@TestPropertySource(properties = "server.port=8080")
public class TestDescuentoSelenium {
	// datasource para acceso a la base de datos mediante sql con JdbcTemplate
	@Autowired
	private javax.sql.DataSource datasource;
	// Como se especifico WebEnvironment.RANDOM_PORT, esta variable identifica el puerto utilizado por el servidor
	@LocalServerPort
	int port;
	// Declara el driver de Selenium usado por las pruebas
	WebDriver driver;

	@BeforeEach
	public void setUp() {
		loadCleanDatabase();
		loadMainPage();
	}

	@AfterEach
	public void tearDown() {
		driver.quit(); // cierra el navegador y finaliza la sesion del driver (si se quiere cerrar solo el navegador usar close)
	}

	/**
	 * Inicializa el WebDriver para el navegador indicado y navega a la pagina principal. Inserta sleeps tras cada
	 * accion para que se pueda ver la secuencia de pasos en los videos
	 */
	private void loadMainPage() {
		// Crea una instancia del driver que abrira el navegador
		driver = SeleniumUtil.getNewDriver();
		// se dirige a la pagina principal
		driver.get(WebConfig.getApplicationUrl(port));
		// ilustra como guardar la imagen del navegador en este momento (el nombre identificara los parametros del paso)
		SeleniumUtil.takeScreenshot(driver, "main-menu");
		SeleniumUtil.sleep(600);
		// selecciona el link para ir a la pagina que se va a probar
		driver.findElement(By.linkText("Ejecutar descuentos de clientes")).click();
		SeleniumUtil.takeScreenshot(driver, "main-application");
		SeleniumUtil.sleep(600);
	}

	/**
	 * Carga limpia de la base de datos: A diferencia de otros tests *ut* que usan @Transactional,
	 * aqui NO sirve el rollback de @Transactional: con WebEnvironment.RANDOM_PORT el navegador
	 * llama a un servidor real que atiende la peticion en otro hilo y otra conexion, por lo que los datos
	 * deben estar commiteados para que la aplicacion los vea; un rollback de la transaccion del test no
	 * alcanzaria esa conexion. Por eso hay que eliminar explicitamente los datos antes de insertar (junto
	 * con spring.sql.init.mode=never, que evita la carga de data.sql).
	 * 
	 * El efeccto del rollback se podria simular tambien de forma declarativa usando una anotacion
	 * @Sql con executionPhase = AFTER_TEST_METHOD.
	 * 
	 * Resumen de los tres escenarios de carga de datos:
	 * - DataJpaTest: rollback automatico (+ BD propia).
     * - Transactional: rollback explícito sobre la BD real, posible porque test y petición comparten transaccion.
     * - Navegador: rollback imposible (servidor en otra conexion), requiere borrado manual.
	 */
	public void loadCleanDatabase() {
		// La carga se realiza directamente sobre la base de datos.
		// Esto perimite que los sripts de creacion de datos de prueba puedan ser guardados en fichero externos
		// cuando se trate de inicializar muchas filas en muchas tablas
		JdbcTemplate database = new JdbcTemplate(datasource);
		database.execute("delete from cliente");
		String sql = """
				insert into cliente(id,edad,nuevo,cupon,tarjeta) values
					(1,18,'S','N','N'),
					(2,38,'S','S','N'),
					(3,21,'S','N','S'),
					(4,25,'N','N','N'),
					(5,40,'N','S','N'),
					(6,42,'N','N','S'),
					(7,39,'N','S','S')
					""";
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
		doStep(true, "", """
				Id,% Descuento
				1,15
				2,20
				5,20
				6,10
				7,30
				""");
		doStep(false, "40", """
				Id,% Descuento
				5,20
				6,10
				""");
		doStep(false, "39", """
				Id,% Descuento
				5,20
				6,10
				7,30
				""");
		doStep(false, "", """
				Id,% Descuento
				1,15
				2,20
				5,20
				6,10
				7,30
				""");
	}

	/**
	 * La ejecucion de un paso ilustra el uso de las acciones basicas de selenium (findElement, click, sendKeys) y
	 * otras utilidades (obtener el contenido de una tabla, tomar imagenes)
	 */
	private void doStep(boolean initialStep, String edad, String expected) {
		WebElement txtEdad = driver.findElement(By.id("txtEdad"));
		if (initialStep) {
			assertEquals("", txtEdad.getText()); // asegura que no hay texto
		} else { // pone la edad y envia el formulario
			txtEdad.clear(); // si no se limpia antes, sendKeys concatenara con el texto existente
			txtEdad.sendKeys(edad);
			driver.findElement(By.id("btnEdad")).click();
		}
		
		// Ilustra como guardar la imagen del navegador en este momento
		SeleniumUtil.takeScreenshot(driver, initialStep + "-" + edad);
		SeleniumUtil.sleep(600);

		// Comprueba el estado del filtro aplicado.
		// Si no se hubiera tomado una imagen de pantalla y un sleep, el test seria flaky:
		// Con navegadores recientes (desde 2025), aunque el elemento a buscar esta dentro de un span estatico, 
		// no siempre se localiza el elemento tras un post (causa excepcion StaleElementReferenceException).
		// Para solucionarlo se debería usar un wait hasta que el elemento este visible (no solamente presente).
		// Esto se ilustrara en el test que utiliza PageObjects.
		assertEquals("".equals(edad) ? "n/a" : edad, driver.findElement(By.id("filtro")).getText());
		
		// busca la tabla en el navegador, obtiene el texto de las celdas y la compara como string csv
		WebElement tab = driver.findElement(By.id("tabDescuentos"));
		String[][] arrays = SeleniumUtil.getTableContent(tab);
		assertEquals(expected, Util.arraysToCsv(arrays));

		// Alternativa de comparacion, tabla como string: en lugar de obtener el contenido celda a celda (getTableContent) y
		// convertirlo a csv, se obtiene el contenido completo de la tabla como un unico string con getText(): el
		// navegador separa las filas con saltos de linea y las celdas con espacios. Es mas simple, pero menos
		// robusto: depende del formato exacto del texto renderizado y no permite tratar las celdas por separado.
		String expectedWhitespaceCsv = Util.arraysToCsv(arrays, null, " ", "", "").strip();
		assertEquals(expectedWhitespaceCsv, tab.getText());

		SeleniumUtil.sleep(600);
	}

}

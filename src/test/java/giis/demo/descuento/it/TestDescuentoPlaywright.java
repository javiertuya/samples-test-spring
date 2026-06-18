package giis.demo.descuento.it;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

import giis.demo.descuento.DescuentoApplication;
import giis.demo.util.Util;

/**
 * Misma prueba de interfaz de usuario que TestDescuentoSelenium, pero ejecutada con Playwright
 * (https://playwright.dev/java/) en lugar de Selenium. Se mantiene identico el escenario y las comprobaciones
 * para que se puedan comparar ambas tecnologias; los comentarios resaltan unicamente las diferencias respecto
 * de la version con Selenium.
 *
 * <br/>
 * Al igual que Selenium apoya sus utilidades en SeleniumUtil, esta version las apoya en PlaywrightUtil (lanzar
 * el navegador segun el fichero de propiedades, obtener el contenido de una tabla y tomar capturas).
 *
 * <br/>
 * Principales diferencias respecto de Selenium:
 * <pre>
 * - Gestion de navegadores: Playwright descarga y gestiona sus propios navegadores.
 * - Esperas: Playwright incorpora auto-waiting: cada accion espera de
 *   forma automatica a que el elemento sea accionable, por lo que no hace falta muchos waits.
 * - API: en lugar de driver.findElement(By...) se usan Locators (page.locator("css")).
 * </pre>
 */
@SpringBootTest(classes = { DescuentoApplication.class }, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-test.properties")
public class TestDescuentoPlaywright {
	// datasource para acceso a la base de datos mediante sql con JdbcTemplate (igual que en Selenium)
	@Autowired
	private javax.sql.DataSource datasource;
	// Como se especifico WebEnvironment.RANDOM_PORT, esta variable identifica el puerto utilizado por el servidor
	@LocalServerPort
	int port;
	// A diferencia de Selenium (un unico WebDriver), Playwright tiene tres objetos: el punto de entrada
	// (Playwright), el navegador lanzado (Browser) y la pestanya sobre la que se interactua (Page)
	Playwright playwright;
	Browser browser;
	Page page;

	@BeforeEach
	public void setUp() {
		loadCleanDatabase();
		loadMainPage();
	}

	@AfterEach
	public void tearDown() {
		// Se cierra el navegador y se libera Playwright (equivalente a driver.quit() en Selenium, pero aqui
		// hay que cerrar tambien el objeto Playwright que gestiona el proceso del navegador)
		if (browser != null)
			browser.close();
		if (playwright != null)
			playwright.close();
	}

	/**
	 * Inicializa Playwright, lanza el navegador y navega a la pagina principal. Inserta esperas tras cada accion
	 * para que se pueda ver la secuencia de pasos en los videos (no son necesarias para sincronizar: Playwright
	 * espera automaticamente, cumplen el mismo proposito que los sleep de la version Selenium).
	 */
	private void loadMainPage() {
		// Inicializa Playwright y lanza el navegador segun el fichero de propiedades (local headed/headless o
		// remoto), de forma analoga a SeleniumUtil.getNewDriver (ver PlaywrightUtil.getNewBrowser)
		playwright = Playwright.create();
		browser = PlaywrightUtil.getNewBrowser(playwright);
		page = browser.newPage(new Browser.NewPageOptions().setViewportSize(1024, 768));
		// se dirige a la pagina principal (se reutiliza la utilidad de Selenium para construir la url)
		page.navigate(SeleniumUtil.getApplicationUrl(port));
		PlaywrightUtil.takeScreenshot(page, "main-menu");
		page.waitForTimeout(600);
		// selecciona el link para ir a la pagina que se va a probar (Playwright localiza por texto con getByText)
		page.getByText("Ejecutar descuentos de clientes").click();
		PlaywrightUtil.takeScreenshot(page, "main-application");
		page.waitForTimeout(600);
	}

	/**
	 * Carga limpia de la base de datos: identica a TestDescuentoSelenium. Con WebEnvironment.RANDOM_PORT no hay
	 * transaccion con rollback por test, por lo que es necesario eliminar explicitamente los datos antes de
	 * insertar (junto con spring.sql.init.mode=never, que evita la carga de data.sql).
	 */
	public void loadCleanDatabase() {
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
	 * Escenario de prueba de la pantalla: identico al de TestDescuentoSelenium.
	 * Cuatro pasos cambiando el filtro por edad, comprobando en cada uno el valor de la tabla de descuentos.
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
	 * La ejecucion de un paso ilustra las acciones basicas de Playwright (locator, fill, click) frente a las de
	 * Selenium (findElement, sendKeys, click).
	 */
	private void doStep(boolean initialStep, String edad, String expected) {
		Locator txtEdad = page.locator("#txtEdad");
		if (initialStep) {
			// Para campos de formulario Playwright usa inputValue() (el valor del input), mientras que Selenium
			// usaba getText() (que para inputs devuelve cadena vacia)
			assertEquals("", txtEdad.inputValue());
		} else { // pone la edad y envia el formulario
			txtEdad.fill(edad); // fill borra el contenido previo automaticamente (no hace falta clear() como en Selenium)
			page.locator("#btnEdad").click();
		}

		// Ilustra como guardar la imagen del navegador en este momento
		PlaywrightUtil.takeScreenshot(page, initialStep + "-" + edad);
		page.waitForTimeout(600);

		// Comprueba el estado del filtro aplicado. No se necesita ningun wait ni parche: Playwright reintenta la
		// lectura hasta que el elemento esta visible y estable tras el post.
		assertEquals("".equals(edad) ? "n/a" : edad, page.locator("#filtro").innerText());

		// busca la tabla en el navegador, obtiene el texto de las celdas y la compara como string csv
		Locator tab = page.locator("#tabDescuentos");
		String[][] arrays = PlaywrightUtil.getTableContent(tab);
		assertEquals(expected, Util.arraysToCsv(arrays));
		page.waitForTimeout(600);
	}

}

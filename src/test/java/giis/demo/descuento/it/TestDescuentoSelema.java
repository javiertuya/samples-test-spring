package giis.demo.descuento.it;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import giis.demo.descuento.DescuentoApplication;
import giis.demo.util.Util;
import giis.selema.framework.junit4.LifecycleJunit4Test;
import giis.selema.manager.SeleManager;
import giis.selema.manager.SelemaConfig;
import giis.selema.services.browser.SelenoidBrowserService;
import giis.selema.services.impl.WatermarkService;

/**
 * Las mismas pruebas que TestDescuentoSelenium (quitando los somentarios) pero usando un componente (selema) 
 * que gestiona el driver de selenium, la grabacion de videos con selenoid, imagenes de los test que fallan, etc:
 * https://github.com/javiertuya/selema
 */
@SpringBootTest(classes = { DescuentoApplication.class }, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-test.properties")
@RunWith(SpringRunner.class)
public class TestDescuentoSelema {
	@Autowired
	private javax.sql.DataSource datasource;
	@LocalServerPort
	int port;
	
	// En vez de declarar el driver, instancia y configura el SeleniumManager que gestionara el driver
	private static SeleManager 
		sm = new SeleManager(new SelemaConfig().setReportSubdir("target/selema")) //carpeta especifica para estos reports
			.setBrowser("chrome")
			.setDriverUrl(SeleniumUtil.getRemoteWebDriverUrl()) //leido del archivo properties (si existe), si es "" indicara driver local
			.add(new SelenoidBrowserService().setVideo()) //configura para uso de selenoid con grabacion de video
			.add(new WatermarkService().setDelayOnFailure(3)); //insercion de marcas de agua en la pagina bajo test, si falla espera 3 segundos para poder observar el estado

		@Rule
		public LifecycleJunit4Test tw = new LifecycleJunit4Test(sm);

		@Before
		public void setUp() {
			loadCleanDatabase();
			loadMainPage();
		}

	/**
	 * Navega a la pagina principal (el driver y otras funcionalidades se obtienen a traves del objeto sm)
	 */
	private void loadMainPage() {
		// El driver se instancia automaticamente antes de setUp, y a traves de sm se pueden realizar las acciones
		// correspondientes
		sm.driver().get(SeleniumUtil.getApplicationUrl(port));
		sm.watermark(); // inserta el nombre del test como marca de agua
		sm.screenshot("main-menu");
		SeleniumUtil.sleep(600);
		sm.driver().findElement(By.linkText("Ejecutar descuentos de clientes")).click();
		sm.watermark();
		sm.screenshot("main-application");
		SeleniumUtil.sleep(600);
	}

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

	private void doStep(boolean initialStep, String edad, String expected) {
		WebElement txtEdad = sm.driver().findElement(By.id("txtEdad"));
		if (initialStep) {
			assertEquals("", txtEdad.getText()); // asegura que no hay texto
		} else {
			txtEdad.clear(); // si no se limpia antes, sendKeys concatenara con el texto existente
			txtEdad.sendKeys(edad);
			sm.driver().findElement(By.id("btnEdad")).click();
			sm.watermark();
			SeleniumUtil.sleep(600);
		}
		sm.screenshot(initialStep + "-" + edad);
		assertEquals("".equals(edad) ? "n/a" : edad, sm.driver().findElement(By.id("filtro")).getText());
		WebElement tab = sm.driver().findElement(By.id("tabDescuentos"));
		String[][] arrays = SeleniumUtil.getTableContent(tab);
		assertEquals(expected, Util.arraysToCsv(arrays));
		SeleniumUtil.sleep(600);
	}

}

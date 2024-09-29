package giis.demo.descuento.it;
import org.junit.*;
import org.junit.runner.RunWith;
import static org.junit.Assert.assertEquals;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.openqa.selenium.*;

import giis.demo.descuento.*;
import giis.demo.util.*;

/**
 * Pruebas de la interaccion del usuario con la aplicacion web del ejemplo de descuentos a clientes,
 * misma implementacion que TestDescuentoSelenium pero utilizando Page Objects
 * 
 * NOTA: el codigo duplicado se mantiene intencionadamente para tener ejemplos independientes en un unico fichero
 */
@SpringBootTest(classes= {DescuentoApplication.class},
	webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations="classpath:application-test.properties")
@RunWith(SpringRunner.class)
public class TestDescuentoPageObjects {
	@Autowired private javax.sql.DataSource datasource;
	@LocalServerPort int port;
	WebDriver driver;
	DescuentoPo po; //PageObject de la pantalla de descuentos que se esta probando

	/**
	 * Salvo la ejecucion de cada paso y la localizacion de la pagina bajo test en el setup,
	 * el resto del codigo es igual que en TestDescuentoSelenium.
	 * En el setup:
	 * - crea una instancia de la pagina principal
	 * - y despues invoca el metodo para navegar a la pagina bajo test
	 */
	@Before
	public void setUp() {
		loadCleanDatabase();
		driver=SeleniumUtil.getNewDriver();
		po = new DescuentoMainPo(driver, port).NavigateToDescuentoUsingPo();
	}
	@After
	public void tearDown() {
		driver.quit(); //cierra el navegador y finaliza la sesion del driver (si se quiere cerrar solo el navegador usar close) 
	}
	public void loadCleanDatabase() {
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
	 * La ejecucion de un paso ilustra el uso de las acciones del Page Object.
	 * Se puede apreciar como se simplifica el codigo del test al encapsular todas las
	 * operaciones realizadas por selenium en el Page Object
	 */
	private void doStep(boolean initialStep, String edad, String expected) {
		if (initialStep)
			assertEquals("", po.getEdad()); // asegura que no hay texto
		else // pone la edad, esta accion enviara el formulario
			po.setEdad(edad);

		// comprueba el estado del filtro aplicado tras el post y el contenido de la tabla con los descuentos
		assertEquals("".equals(edad) ? "n/a" : edad, po.getFiltro());
		String[][] descuentos = po.getDescuentos();
		assertEquals(expected, Util.arraysToCsv(descuentos));
	}

}

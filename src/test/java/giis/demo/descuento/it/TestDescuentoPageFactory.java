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
 * misma implementacion que TestDescuentoPageObjects pero utilizando PageFactory
 * 
 * NOTA: el codigo duplicado se mantiene intencionadamente para tener ejemplos independientes en un unico fichero
 */
@SpringBootTest(classes= {DescuentoApplication.class},
	webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations="classpath:application-test.properties")
@RunWith(SpringRunner.class)
public class TestDescuentoPageFactory {
	@Autowired private javax.sql.DataSource datasource;
	@LocalServerPort int port;
	WebDriver driver;
	DescuentoPf pf; //PageObject de la pantalla de descuentos que se esta probando

	/**
	 * El setup es igual que con PageFactory, salvo que se inicializa la pagina
	 * usando el metodo que devuelve PageFactory
	 */
	@Before
	public void setUp() {
		loadCleanDatabase();
		driver=SeleniumUtil.getNewDriver();
		pf = new DescuentoMainPo(driver, port).NavigateToDescuentoUsingPf();
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
	 * El test con Page Factory es igual que con Page Object
	 */
	private void doStep(boolean initialStep, String edad, String expected) {
		if (initialStep)
			assertEquals("", pf.getEdad()); // asegura que no hay texto
		else // pone la edad, esta accion enviara el formulario
			pf.setEdad(edad);

		// comprueba el estado del filtro aplicado tras el post y el contenido de la tabla con los descuentos
		assertEquals("".equals(edad) ? "n/a" : edad, pf.getFiltro());
		String[][] descuentos = pf.getDescuentos();
		assertEquals(expected, Util.arraysToCsv(descuentos));
	}

}

package giis.demo.descuento.ut;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import giis.demo.descuento.Cliente;
import giis.demo.descuento.ClienteRepository;
import giis.demo.descuento.DescuentoDisplayDTO;
import giis.demo.util.Util;

/**
 * Pruebas del ejemplo de informe de descuentos de clientes leidos desde la base de datos (Problemas 3b y 3c):
 * Ilustra la configuracion necesaria para utilizar un runner diferente al de Spring Boot (en este caso se
 * trata de JUnitParamsRunner para ejecucion de pruebas parametrizadas).
 *
 * Como este test esta basado en TestDescuentoRepository, se han eliminado todos los comentarios anyadiendo
 * solamente los especificos para ilustrar el uso de un runner diferente.
 * 
 * En JUnit 5/6 los test parametrizados requieren importar con junit-jupiter-params.
 */
@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
public class TestDescuentoParametrized {
	private static final Logger log = LoggerFactory.getLogger(TestDescuentoParametrized.class);
	
	@Autowired
	private TestEntityManager entityManager;
	@Autowired
	private ClienteRepository cliente;

	private TestInfo testInfo; // usado para obtener el nombre del test ejecutado

	@BeforeEach
	public void setUp(TestInfo testInfo) {
		loadCleanDatabase();
		this.testInfo = testInfo;
	}

	public void loadCleanDatabase() {
		entityManager.persist(new Cliente(1, 18, "S", "N", "N"));
		entityManager.persist(new Cliente(2, 38, "S", "S", "N"));
		entityManager.persist(new Cliente(3, 21, "S", "N", "S"));
		entityManager.persist(new Cliente(4, 25, "N", "N", "N"));
		entityManager.persist(new Cliente(5, 40, "N", "S", "N"));
		entityManager.persist(new Cliente(6, 42, "N", "N", "S"));
		entityManager.persist(new Cliente(7, 39, "N", "S", "S"));
	}

	/**
	 * En este test, como el segundo parametro es una tabla (salida deseada en csv) 
	 * utiliza ; y | para separar columnas y filas, respectivamente, 
	 * que se remplazaran para darle formato csv antes del assert.
	 */
	@ParameterizedTest
	@CsvSource({ "39, 5;20|6;10|7;30", 
				"40, 5;20|6;10" })
	public void testParametrizado(Integer edad, String expected) {
		log.info("Run test with parameters: {}", testInfo.getDisplayName());
		List<DescuentoDisplayDTO> descuentos = cliente.getListaDescuentos(edad);
		assertEquals(expected.replace(";", ",").replace("|", "\n"),
				Util.pojosToCsv(descuentos, new String[] { "id", "descuento" }).trim());
	}

}

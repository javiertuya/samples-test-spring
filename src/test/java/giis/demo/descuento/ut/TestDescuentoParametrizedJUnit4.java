package giis.demo.descuento.ut;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import giis.demo.descuento.Cliente;
import giis.demo.descuento.ClienteRepository;
import giis.demo.descuento.DescuentoDisplayDTO;
import giis.demo.util.Util;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

/**
 * Ilustra las principales al usar JUnit 4 en vez de JUnit 5/6. Para ello usa el mismo
 * test que TestDescuentoParametrized.
 * 
 * Como los test parametrizados nativos no tienen mucha flexibilidad, utiliza otro componente JUnitParams
 */
@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
// En lugar de especificar la extension ExtendWith (cuando proceda),
// con JUnit 4 se indica el ejecutor especifico para Spring (SpringRunner). 
// Pero en este caso, se necesita el ejecutor especifico de JUnitParams.
@RunWith(JUnitParamsRunner.class)
public class TestDescuentoParametrizedJUnit4 {
	private static final Logger log = LoggerFactory.getLogger(TestDescuentoParametrizedJUnit4.class);

	@Autowired
	private TestEntityManager entityManager;
	@Autowired
	private ClienteRepository cliente;

	// Como no se pudo utilizar SpringRunner se han de definir estas dos reglas 
	@SuppressWarnings("deprecation")
	@ClassRule
	public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();
	@SuppressWarnings("deprecation")
	@Rule
	public final SpringMethodRule springMethodRule = new SpringMethodRule();

	@Rule // usado para obtener el nombre del test ejecutado
	public TestName name = new TestName();

	@Before
	public void setUp() {
		loadCleanDatabase();
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
	 * El test parametrizado ejecuta el proceso de obtencion de los descuentos para dos edades que causan
	 * diferentes resultados
	 */
	@Test
	@Parameters({ "39, 5;20\n6;10\n7;30\n", "40, 5;20\n6;10\n" })
	public void testParametrizado(Integer edad, String expected) {
		log.info("Run test with parameters: {}", name.getMethodName());
		List<DescuentoDisplayDTO> descuentos = cliente.getListaDescuentos(edad);
		assertEquals(expected.replace(";", ","),
				Util.pojosToCsv(descuentos, new String[] { "id", "descuento" }).trim());
	}

}

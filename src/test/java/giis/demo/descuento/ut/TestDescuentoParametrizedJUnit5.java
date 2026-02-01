package giis.demo.descuento.ut;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import giis.demo.descuento.Cliente;
import giis.demo.descuento.ClienteRepository;
import giis.demo.descuento.DescuentoDisplayDTO;
import giis.demo.util.Util;

/**
 * Ilustra los principales cambios al usar JUnit5 en vez de JUnit4 con Spring Boot 3.5. Para ello usa el mismo
 * test que TestDescuentoParametrized. Junit6 todavia no viene incluida con spring boot v3.
 */
@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
//En lugar de cambiar el runner usando RunWith, con JUnit5 se usa otro mecanismo de extensiones
//con ExtendWith, aunque tambien se puede seguir usando RunWith
@ExtendWith(SpringExtension.class)
public class TestDescuentoParametrizedJUnit5 {
	@Autowired
	private TestEntityManager entityManager;
	@Autowired
	private ClienteRepository cliente;

	// En el test con JUnit se usaba Rule y ClassRule porque en los test parametrizados se cambiaba el runner.
	// Ahora ya no se cambia el runner.
	// Se usaba otra Rule para obtener el nombre del test. Como ya no existen en JUnit5,
	// se hace pasando un parametro en el setup (o en los metodos)
	private TestInfo testInfo;

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

	// En JUnit5 existen test parametrizados nativos con junit-jupiter-params, con otras anotaciones diferentes.
	// En este ejemplo, como el segundo parametro es una tabla (salida deseada en csv) utiliza ; y | para separar
	// columnas y filas, respectivamente, que se remplazaran para darle formato csv antes del assert.
	@ParameterizedTest
	@CsvSource({ "39, 5;20|6;10|7;30", 
				"40, 5;20|6;10" })
	public void testParametrizado(Integer edad, String expected) {
		System.out.println("Run test with parameters: " + testInfo.getDisplayName());
		List<DescuentoDisplayDTO> descuentos = cliente.getListaDescuentos(edad);
		assertEquals(expected.replace(";", ",").replace("|", "\n"),
				Util.pojosToCsv(descuentos, new String[] { "id", "descuento" }).trim());
	}

}

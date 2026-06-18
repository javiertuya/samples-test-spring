package giis.demo.descuento.ut;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

import giis.demo.descuento.Cliente;
import giis.demo.descuento.ClienteRepository;
import giis.demo.descuento.DescuentoDisplayDTO;
import giis.demo.util.Util;

/**
 * Pruebas del ejemplo de informe de descuentos de clientes leidos desde la base de datos (Problemas 3b y 3c):
 * Ilustra la configuracion para pruebas unitarias de repositorios en Spring Boot. 
 * 
 * <br/>
 * Las situaciones a cubrir son las mismas que en samples-test-java
 * (giis.demo.descuento.ut.TestDescuentoDatabase.java) pero aqui realiza las comparaciones directamente de
 * estructuras bidimensionales (lista de atributos de objetos). 
 * 
 * <br/>
 * La configuracion es la siguiente:
 * 
 * <br/>
 * - DataJpaTest permite acceder a las entidades de la base de datos con una configuracion especifica para
 * pruebas (ver detalles:
 * https://docs.spring.io/spring-boot/docs/current/api/org/springframework/boot/test/autoconfigure/orm/jpa/DataJpaTest.html)
 * <br/>
 * - TestPropertySource: Especifica un fichero de configuracion diferente (localizado en src/test/resources),
 * en este caso solo se omite la carga de datos inicial de data.sql para que se arranque con una base de datos
 * limpia (esto se podria hacer utilizando un profile diferente con su propia configuracion) <br/>
 * - ExtendWith: Para integracion de Spring con JUnit. Cuando se usa JUnit 6 + SpringBoot 4 como en este caso
 * no suele ser necesario porque las anotaciones *Test (aqui DataJpaTest) y contienen esa anotacion <br/>
 * - RunWith: Se ha de especificar el runner especifico para Spring Boot si se usa JInit 4)
 */
@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
//@ExtendWith(SpringExtension.class)
//@RunWith(SpringRunner.class)
public class TestDescuentoRepository {
	// para cargar datos de prueba
	@Autowired
	private TestEntityManager entityManager;
	// el repositorio bajo prueba
	@Autowired
	private ClienteRepository cliente;
	// datasource para acceso a la base de datos mediante sql con JdbcTemplate
	@Autowired
	private javax.sql.DataSource datasource;

	@BeforeEach
	public void setUp() {
		loadCleanDatabase();
	}

	/**
	 * Datos de prueba que se cargaran en el setup para cubrir las situaciones del disenyo de la prueba. 
	 * Notar que no se eliminan los datos antes de ejecutar porque DataJpaTest se comporta de la siguiente forma:
	 * - Envuelve cada test en una transaccion que revierte (rollback) al finalizar, de modo que
     *   cada test arranca con la BD limpia (esto es lo que evita el borrado manual).
     * - Sustituye el datasource por una BD H2 embebida propia con nombre unico (replace=ANY),
     *   aislada de la que usan los demas tests.
	 * - Si se quiere evitar lo anterior y usar la BD real, se puede usar @AutoConfigureTestDatabase(replace = NONE).
	 * 
     * Ademas, con spring.sql.init.mode=never no se han cargado los datos iniciales de data.sql.
     * Solamente a modo de ilustracion, los datos se cargan de tres formas diferentes.
	 */
	public void loadCleanDatabase() {
		// datos cargados a traves del TestEntityManager
		entityManager.persist(new Cliente(1, 18, "S", "N", "N"));
		entityManager.persist(new Cliente(2, 38, "S", "S", "N"));
		entityManager.persist(new Cliente(3, 21, "S", "N", "S"));
		// datos cargados directamente a traves del repositorio
		cliente.save(new Cliente(4, 25, "N", "N", "N"));
		cliente.save(new Cliente(5, 40, "N", "S", "N"));
		// datos cargados directamente en la base de datos utilizando sql
		JdbcTemplate database = new JdbcTemplate(datasource);
		database.execute("""
				insert into cliente(id,edad,nuevo,cupon,tarjeta) values 
					(6,42,'N','N','S'),
					(7,39,'N','S','S')
				""");
	}

	/**
	 * Para la consulta sin parametros simplemente invoca el metodo del modelo que obtiene una lista de objetos
	 * (DTO) y la comparacion se realiza transformando estos con un metodo de utilidad que convierte la lista
	 * anterior a formato CSV.
	 */
	@Test
	public void testConsultaSinParametro() {
		List<DescuentoDisplayDTO> descuentos = cliente.getListaDescuentos(null);
		assertEquals("""
				1,15
				2,20
				5,20
				6,10
				7,30
				""", Util.pojosToCsv(descuentos, new String[] { "id", "descuento" }));
	}

	/**
	 * La misma forma de probar cuando hay parametros.
	 */
	@Test
	public void testConsultaConParametro() {
		List<DescuentoDisplayDTO> descuentos = cliente.getListaDescuentos(40);
		assertEquals("""
				5,20
				6,10
				""", Util.pojosToCsv(descuentos, new String[] { "id", "descuento" }));
	}

}

package giis.demo.descuento.it;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import giis.demo.descuento.Cliente;
import giis.demo.descuento.ClienteRepository;
import giis.demo.descuento.DescuentoApplication;
import giis.demo.descuento.DescuentoDisplayDTO;
import giis.demo.util.Util;

/**
 * Mismas pruebas del repositorio que TestDescuentoRepository (mismo escenario y misma comparacion CSV), pero
 * ejecutadas contra una base de datos PostgreSQL REAL levantada en un contenedor Docker mediante Testcontainers,
 * en lugar de la H2 en memoria. Se mantiene identico el escenario y las comprobaciones para que se puedan
 * comparar ambos enfoques; los comentarios resaltan unicamente las diferencias respecto de la version con H2.
 *
 * <br/>
 * Por que un ejemplo asi: todos los demas tests usan H2 en memoria, que no es el motor de produccion. H2 puede
 * comportarse distinto que el motor real (dialecto SQL, tipos, SQL nativo, orden de filas), por lo que para
 * pruebas de integracion de la capa de persistencia conviene probar contra el mismo motor que se usara en
 * produccion. Testcontainers arranca ese motor real en un contenedor desechable, sin necesidad de instalarlo.
 * De hecho, este ejemplo destapa un fallo latente que H2 ocultaba: la query del repositorio no tiene ORDER BY
 * (ver el comentario de testConsultaSinParametro).
 *
 * <br/>
 * Configuracion especifica de este enfoque (lo que cambia respecto de H2):
 * <pre>
 * - @Testcontainers + @Container: gestionan el ciclo de vida del contenedor. Al declarar el contenedor como
 *   static se arranca UNA vez para toda la clase (y se detiene al terminar), en lugar de uno por test.
 * - @ServiceConnection: conecta automaticamente el datasource de Spring a la url/usuario/password del
 *   contenedor. Evita tener que fijar spring.datasource.* a mano (o usar @DynamicPropertySource).
 * - spring.jpa.hibernate.ddl-auto=create: con una BD embebida (H2) Spring Boot ya crea las tablas solo; con una
 *   BD NO embebida el valor por defecto es 'none', por lo que hay que forzarlo para que Hibernate cree el esquema
 *   a partir de las entidades al arrancar el contexto. Se usa 'create' y NO 'create-drop' a proposito: con
 *   'create-drop' Hibernate intenta ejecutar el DROP del esquema al cerrar el contexto, pero para entonces la
 *   extension @Testcontainers ya ha parado el contenedor (lo hace en afterAll, antes del shutdown hook del JVM),
 *   por lo que Hikari se queda bloqueado pidiendo una conexion a una BD que ya no existe hasta agotar su
 *   connection-timeout (30s), lo que se traduce en el aviso "Surefire is going to kill self fork JVM ... 30
 *   seconds after System.exit(0)" y una espera de 30s al final. Con 'create' no hay DROP al cerrar (el
 *   contenedor es desechable, no tiene sentido dropear), el contexto cierra sin pedir conexion y el JVM termina
 *   de inmediato.
 * </pre>
 *
 * <br/>
 * Limpieza de datos: igual que TestDescuentoRestService, se usa @Transactional para que cada test haga rollback
 * al finalizar (junto con spring.sql.init.mode=never, heredado de application-test.properties). Funciona porque
 * el acceso a la BD ocurre en el mismo hilo y transaccion que el test. Si este fuese un test con servidor real
 * (WebEnvironment.RANDOM_PORT + navegador, como TestDescuentoPlaywright), el rollback no serviria y habria que
 * borrar los datos manualmente, con independencia de que la BD sea H2 o un contenedor.
 *
 * <br/>
 * Nota: requiere tener Docker en ejecucion. La primera vez Testcontainers descargara la imagen postgres.
 */
@SpringBootTest(classes = { DescuentoApplication.class })
@Testcontainers
@TestPropertySource(locations = "classpath:application-test.properties",
		properties = "spring.jpa.hibernate.ddl-auto=create")
@Transactional
public class TestDescuentoTestcontainers {
	// Contenedor con la BD real. static => uno por clase (se reutiliza en todos los tests). @ServiceConnection
	// hace que Spring tome de aqui la configuracion del datasource, asi que no hay que configurar nada mas.
	@Container
	@ServiceConnection
	static PostgreSQLContainer postgres = new PostgreSQLContainer(DockerImageName.parse("postgres:16-alpine"));

	// el repositorio bajo prueba
	@Autowired
	private ClienteRepository cliente;
	// datasource para acceso a la base de datos mediante sql con JdbcTemplate (apunta al contenedor)
	@Autowired
	private javax.sql.DataSource datasource;

	@BeforeEach
	public void setUp() {
		loadCleanDatabase();
	}

	/**
	 * Carga de datos de prueba. No se eliminan los datos previos: @Transactional revierte los inserts de cada
	 * test al finalizar y spring.sql.init.mode=never evita la carga de data.sql, por lo que cada test arranca con
	 * la BD limpia. A modo de ilustracion los datos se cargan de dos formas (no se usa TestEntityManager porque
	 * ese es un componente especifico de @DataJpaTest, que aqui no se utiliza).
	 */
	public void loadCleanDatabase() {
		// datos cargados directamente a traves del repositorio
		cliente.save(new Cliente(1, 18, "S", "N", "N"));
		cliente.save(new Cliente(2, 38, "S", "S", "N"));
		cliente.save(new Cliente(3, 21, "S", "N", "S"));
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
	 * Consulta sin parametros. Mismo escenario que TestDescuentoRepository, con una diferencia REVELADORA: la
	 * query del repositorio no tiene ORDER BY, por lo que el orden de las filas no esta garantizado y depende del
	 * motor. Con H2 (TestDescuentoRepository) las filas salian casualmente ordenadas por id y el test pasaba;
	 * PostgreSQL las devuelve en otro orden y, sin ordenar, este test fallaria. Es justo el tipo de fallo latente
	 * que una BD en memoria oculta y un motor real (el de produccion) saca a la luz: el valor de Testcontainers.
	 * Se ordena por id antes de comparar (la solucion de fondo seria anyadir ORDER BY a la query del repositorio).
	 */
	@Test
	public void testConsultaSinParametro() {
		List<DescuentoDisplayDTO> descuentos = cliente.getListaDescuentos(null);
		descuentos.sort(Comparator.comparing(DescuentoDisplayDTO::getId));
		assertEquals("""
				1,15
				2,20
				5,20
				6,10
				7,30
				""", Util.pojosToCsv(descuentos, new String[] { "id", "descuento" }));
	}

	/**
	 * Consulta con parametro: igual que TestDescuentoRepository pero, por el mismo motivo que el test anterior
	 * (la query no garantiza el orden), se ordena por id antes de comparar.
	 */
	@Test
	public void testConsultaConParametro() {
		List<DescuentoDisplayDTO> descuentos = cliente.getListaDescuentos(40);
		descuentos.sort(Comparator.comparing(DescuentoDisplayDTO::getId));
		assertEquals("""
				5,20
				6,10
				""", Util.pojosToCsv(descuentos, new String[] { "id", "descuento" }));
	}

}

package giis.demo.descuento.ut;
import org.junit.*;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import static org.junit.Assert.assertEquals;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import java.util.List;

import giis.demo.descuento.*;
import giis.demo.util.*;
import junitparams.Parameters;
import junitparams.JUnitParamsRunner;

/**
 * Pruebas del ejemplo de informe de descuentos de clientes leidos desde la base de datos (Problemas 3b y 3c):
 * Ilustra la configuracion necesaria para utilizar un runner diferente al de Spring Boot
 * (en este caso se trata de JUnitParamsRunner para ejecucion de pruebas parametrizadas).
 * <br/>Como este test esta basado en TestDescuentoRepository, se han eliminado todos los comentarios
 * anyadiendo solamente los especificos para ilustrar el uso de un runner diferente.
 */
@DataJpaTest
@TestPropertySource(locations="classpath:application-test.properties")
//En este caso no se utiliza SpringRunner
@RunWith(JUnitParamsRunner.class)
public class TestDescuentoParametrized {
	@Autowired private TestEntityManager entityManager;
	@Autowired private ClienteRepository cliente;

	//Para sustituir SpringRunner basta con definir estas dos reglas 
	//(solo disponibles en ultimas versiones de spring)
	@ClassRule
	public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();
	@Rule
	public final SpringMethodRule springMethodRule = new SpringMethodRule();

	@Rule public TestName name = new TestName();
	
	@Before
	public void setUp() {
		loadCleanDatabase();
	}
	public void loadCleanDatabase() {
		entityManager.persist(new Cliente(1,18,"S","N","N"));
		entityManager.persist(new Cliente(2,38,"S","S","N"));
		entityManager.persist(new Cliente(3,21,"S","N","S"));
		entityManager.persist(new Cliente(4,25,"N","N","N"));
		entityManager.persist(new Cliente(5,40,"N","S","N"));
		entityManager.persist(new Cliente(6,42,"N","N","S"));
		entityManager.persist(new Cliente(7,39,"N","S","S"));
	}

	/**
	 * El test parametrizado ejecuta el proceso de obtencion de los descuentos para dos edades
	 * que causan diferentes resultados
	 */
	@Test
	@Parameters({"39, 5;20\n6;10\n7;30\n", 
				 "40, 5;20\n6;10\n"})  
	public void testParametrizado(Integer edad, String expected) {
		System.out.println("Run test with parameters: "+name.getMethodName());
		List<DescuentoDisplayDTO> descuentos=cliente.getListaDescuentos(edad);
		assertEquals(expected.replace(";", ","), 
				Util.pojosToCsv(descuentos,new String[] {"id","descuento"}).trim());
	}

}

package giis.demo.descuento.ut;
import org.junit.*;
import org.junit.runner.RunWith;
import static org.junit.Assert.assertEquals;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.junit4.SpringRunner;
import org.mockito.Mockito;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import giis.demo.descuento.*;
import giis.demo.util.*;

/**
 * Prueba de un servicio con una implementacion incompleta: Ilustra el uso de Mocks y la carga 
 * datos de prueba mediante ficheros externos.
 * <br/>El servicio bajo prueba utiliza un api para acceder al servicio de marketing, 
 * que esta desplegado como un microservicio independiente (no comparte base de datos),
 * pero cuya implementacion todavia no esta disponible (solo el interfaz).
 * <br/>Se anyade la anotacion SpringBootTest: Como Spring Boot realiza automaticamente la mayor parte de las configuraciones, 
 * y el resto se especifican en la clase que arranca la aplicacion, indica que se reutilizara esta clase para la configuracion.
 * <br/>Este test define un mock para el api utilizando Mockito. Las situaciones a cubrir son:
 * <pre>
 * Codigos de promocion del cliente segun pais
 * -tiene codigo de promocion
 * -no tiene codigo de promocion
 * -hay codigos de promocion diferentes
 * </pre>
 */
@SpringBootTest(classes = DescuentoApplication.class)
@TestPropertySource(locations="classpath:application-test.properties")
@RunWith(SpringRunner.class)
public class TestPromocionMock {
	//El servicio bajo prueba
	@Autowired private ClienteService cliente;
	//el mock que sustituira los metodos que acceden al microservicio de marketing
	//que utiliza ClienteService (no implementados todavia)
	@MockBean private MarketingApi marketing;

	/**
	 * Define el mock que devuelve una lista de pares clave-valor de la forma codigo de pais-codigo de promocion
	 * tal como los devolveria la invocacion al microservicio de marketing,
	 */
	@SuppressWarnings("serial")
	@Before
	public void setUp() {
		Map<String, String> codes = new HashMap<String, String>() {{ put("ES", "P01ES"); put("UK", "P03UK"); }};
		Mockito.when(marketing.getPromotions()).thenReturn(codes);
	}

	/**
	 * El caso de prueba utiliza el servicio del cliente como si toda la implementacion de la api
	 * estuviera disponible, pero en este caso es simplemente un mock.
	 * Para cubrir las tres situaciones disenyadas se tienen tres clientes, en dos de ellos el codigo
	 * de pais se corresponde con un codigo de promocion existetne (codigos diferentes), en el otro no.
	 * Ilustra tambien como se puede inicializar la base de datos mediante sql en un archivo externo:
	 * https://docs.spring.io/spring/docs/current/spring-framework-reference/testing.html#testcontext-executing-sql.
	 */
	@Sql(scripts = "classpath:/sql-test-mock.sql", 
			config = @SqlConfig(commentPrefix = "--", separator = ";")) //config se podria quitar pues son los valores por defecto
	@Test
	public void testPromocionMock() {
		List<PromocionDisplayDTO> promos=cliente.getListaPromociones();
		assertEquals("1,P01ES\n2,\n3,P03UK\n",Util.pojosToCsv(promos,new String[] {"id","promo"}));
	}
}

package giis.demo.descuento.ut;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.transaction.annotation.Transactional;

import giis.demo.descuento.ClienteService;
import giis.demo.descuento.DescuentoApplication;
import giis.demo.descuento.MarketingApi;
import giis.demo.descuento.PromocionDisplayDTO;
import giis.demo.util.Util;

/**
 * Prueba de un servicio con una implementacion incompleta: Ilustra el uso de Mocks y la carga datos de prueba
 * mediante ficheros externos. <br/>
 * El servicio bajo prueba utiliza un api para acceder al servicio de marketing, que esta desplegado como un
 * microservicio independiente (no comparte base de datos), pero cuya implementacion todavia no esta
 * disponible (solo el interfaz). <br/>
 * 
 * Este test define un mock para el api utilizando Mockito. Las situaciones a cubrir son:
 * 
 * <pre>
 * Codigos de promocion del cliente segun pais
 * -tiene codigo de promocion
 * -no tiene codigo de promocion
 * -hay codigos de promocion diferentes
 * </pre>
 */
@SpringBootTest(classes = DescuentoApplication.class)
@TestPropertySource(locations = "classpath:application-test.properties")
// A diferencia de @DataJpaTest, @SpringBootTest no establece una transaccion con rollback por defecto, por lo que
// los datos cargados por @Sql persistirian para el siguiente test. Con @Transactional cada test hace rollback al
// finalizar, de modo que (junto con spring.sql.init.mode=never) la base de datos siempre arranca limpia.
@Transactional
public class TestPromocionMock {
	// El servicio bajo prueba
	@Autowired
	private ClienteService cliente;
	// el mock que sustituira los metodos que acceden al microservicio de marketing
	// que utiliza ClienteService (no implementados todavia)
	@MockitoBean
	private MarketingApi marketing;

	/**
	 * Define el mock que devuelve una lista de pares clave-valor de la forma codigo de pais-codigo de promocion
	 * tal como los devolveria la invocacion al microservicio de marketing,
	 */
	@BeforeEach
	public void setUp() {
		Map<String, String> codes = new HashMap<String, String>() { { put("ES", "P01ES"); put("UK", "P03UK"); } };
		Mockito.when(marketing.getPromotions()).thenReturn(codes);
	}

	/**
	 * El caso de prueba utiliza el servicio del cliente como si toda la implementacion de la api estuviera
	 * disponible, pero en este caso es simplemente un mock. Para cubrir las tres situaciones disenyadas se tienen
	 * tres clientes, en dos de ellos el codigo de pais se corresponde con un codigo de promocion existetne
	 * (codigos diferentes), en el otro no. Ilustra tambien como se puede inicializar la base de datos mediante
	 * sql en un archivo externo:
	 * https://docs.spring.io/spring/docs/current/spring-framework-reference/testing.html#testcontext-executing-sql.
	 */
	@Sql(scripts = "classpath:/sql-test-mock.sql", 
			config = @SqlConfig(commentPrefix = "--", separator = ";")) 
	// config se podria quitar pues son los valores por defecto
	@Test
	public void testPromocionMock() {
		List<PromocionDisplayDTO> promos = cliente.getListaPromociones();
		assertEquals("""
				1,P01ES
				2,
				3,P03UK
				""", Util.pojosToCsv(promos, new String[] { "id", "promo" }));
	}
}

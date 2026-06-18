package giis.demo.descuento.ut;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import giis.demo.descuento.DescuentoApplication;

/**
 * Pruebas del ejemplo de informe de descuentos de clientes leidos desde la base de datos (Problemas 3b y 3c):
 * Ilustra la configuracion para pruebas de un servicio rest en Spring Boot (implementado en
 * DescuentoRestController.java). 
 * 
 * <br/>
 * Realiza los mismos tests que TestDescuentoRepository, del cual han eliminado todos los comentarios
 * anyadiendo solamente los especificos para ilustrar la prueba del servicio rest. 
 * 
 * <br/>
 * La configuracion es la siguiente:
 * 
 * <br/>
 * - SpringBootTest: Carga la configuracion de la aplicacion, pero en este caso se indica que utilice un
 * entorno web sin desplegar un servidor. <br/>
 * - AutoConfigureMockMvc: configura MockMvc que permite acceder a los endpoints del servicio. <br/>
 * - TestPropertySource, RunWith: Igual que TestDescuentoRepository.java <br/>
 */
@SpringBootTest(classes = { DescuentoApplication.class }, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")

// A diferencia de @DataJpaTest, @SpringBootTest usa el datasource REAL de la aplicacion (la misma BD
// que los tests de navegador en *it*) y NO abre transaccion con rollback por defecto: los inserts de un test
// persistirian para el siguiente. Por eso se añade @Transactional explicitamente. Funciona porque MockMvc
// ejecuta la peticion en el mismo hilo y transaccion que el test, asi cada test revierte sus inserts al
// finalizar y (junto con spring.sql.init.mode=never) la BD arranca siempre limpia, sin borrado manual.
@Transactional
public class TestDescuentoRestService {
	// datasource para acceso a la base de datos mediante sql con JdbcTemplate
	@Autowired
	private javax.sql.DataSource datasource;
	// Objeto usado para acceder al servicio rest
	@Autowired
	private MockMvc mvc;

	@BeforeEach
	public void setUp() {
		loadCleanDatabase();
	}

	public void loadCleanDatabase() {
		// No es necesario borrar los datos previos: la bd arranca vacia (spring.sql.init.mode=never)
		// y @Transactional revierte los inserts de cada test al finalizar
		JdbcTemplate database = new JdbcTemplate(datasource);
		database.execute("""
				insert into cliente(id,edad,nuevo,cupon,tarjeta) values 
					(1,18,'S','N','N'), 
					(2,38,'S','S','N'), 
					(3,21,'S','N','S'), 
					(4,25,'N','N','N'), 
					(5,40,'N','S','N'), 
					(6,42,'N','N','S'), 
					(7,39,'N','S','S')
				""");
	}

	@Test
	public void testConsultaSinParametro() throws Exception {
		// el objeto mvc ejecuta la llamada al api y contiene una serie de matchers para comprobar
		// el resultado (se muestran diferentes formas de comprobar)
		mvc.perform(get("/api/descuentos").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(5)))
				.andExpect(jsonPath("$[0].id", is(1)))
				.andExpect(jsonPath("$[0].descuento", is(15)))
				.andExpect(jsonPath("$[1].*", contains(2, 20)))
				.andExpect(jsonPath("$[2].*", contains(5, 20)))
				.andExpect(jsonPath("$[3].*", contains(6, 10)))
				.andExpect(jsonPath("$[4].*", contains(7, 30)));
	}

	@Test
	public void testConsultaConParametro() throws Exception {
		// El objeto mvc devuelve un ResultActions que puede ser utilizado para otras comparaciones
		ResultActions res = mvc.perform(get("/api/descuentos?edad=40").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
		// en este caso se comparara el contenido completo del json obtenido
		String json = res.andReturn().getResponse().getContentAsString();
		assertEquals("[{\"id\":5,\"descuento\":20},{\"id\":6,\"descuento\":10}]", json);
		// ahora eliminando comillas para que sea mas facil indicar la salida deseada
		assertEquals("[{id:5,descuento:20},{id:6,descuento:10}]", json.replaceAll("\"", ""));
	}

}

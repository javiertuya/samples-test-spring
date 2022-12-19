package giis.demo.descuento.ut;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import giis.demo.descuento.*;

/**
 * Ilustra la configuracion para pruebas unitarias de un controlador web utilizando mocks para el resto del sistema.
 * Es similar a TestDescuentoRestService, con los siguientes cambios:
 * <br/>- Como utiliza solo el controlador (el servicio que accede a esta se implementa con un mock), 
 * SpringBootTest no precisa en este caso indicar la clase que definira el contexto de la aplicacion
 * <br/>- Como no se usara la base de datos no hace falta indicar TestPropertySource
 * 
 * <br/>Este ejemplo ilustra solamente la forma de interactuar con el controlador de forma aislada.
 * Estas pruebas para el controlador se realizaran normalmente si el controlador incluye una logica compleja que no 
 * sera probada cuando se realicen pruebas integrando el interfaz de usuario, de lo contrario
 * se estaria probando lo mismo dos veces.
 */
@SpringBootTest
@TestPropertySource(locations="classpath:application-test.properties")
@AutoConfigureMockMvc
@RunWith(SpringRunner.class)
public class TestDescuentoWebController {
	//Objeto usado para acceder al controlador web
	@Autowired private MockMvc mvc;
	//el mock del servicio que utiliza este controlador
	@MockBean private ClienteService cliente;
	
	/**
	 * Configura el mock en el setup: En este ejemplo el mock devolvera los datos de ClienteService (id,descuento)
	 * simulando el comportamiento con una base de datos con dos filas (1,18,'S','N','N'), (2,38,'S','S','N')
	 * para dos situaciones a cubrir:
	 * <br>(1) acceso sin indicar edad (debera mostrar todas las filas con los descuentos 15, 20 respectivamente)
	 * <br>(2) acceso indicando edad 19 (debera mostrar solo la segunda fila con descuento 20)
	 */
	@SuppressWarnings("serial")
	@Before
	public void setUp() {
		DescuentoDisplayDTO descuento0=new DescuentoDisplayDTO(1,15);
		DescuentoDisplayDTO descuento1=new DescuentoDisplayDTO(2,20);
		Mockito.when(cliente.getListaDescuentos(0))
			.thenReturn(new ArrayList<DescuentoDisplayDTO>() {{ add(descuento0); add(descuento1); }});
		Mockito.when(cliente.getListaDescuentos(19))
			.thenReturn(new ArrayList<DescuentoDisplayDTO>() {{ add(descuento1); }});
	}

	/**
	 * Situación (1): Get para obtener la pagina inicial, no se especifica edad, devolvera dos filas (de acuerdo con el mock).
	 * Ilustra la forma habitual de comprobar los valores que devuelve el controlador utilizando matchers para:
	 * <br>- "command" contiene el campo donde el usuario introduce la edad (es nulo al ser un get)
	 * <br>- "descuento" contiene la lista de descuentos (id,descuento) que devuelve el controlador
	 */
	@Test
	public void testGetRequest() throws Exception {
		ResultActions res=mvc.perform(get("/descuentos"))
				.andExpect(status().isOk());
		//la comprobacion se realiza accediendo los DTOs que el controlador recibe/devuelve en el request (Model)
		res.andExpect(model().attribute("command", hasProperty("edad", is(nullValue()))));
		res.andExpect(model().attribute("descuentos", hasSize(2)));
		res.andExpect(model().attribute("descuentos", hasItem(
				allOf(hasProperty("id", is(1)), hasProperty("descuento", is(15)) ) )));
		res.andExpect(model().attribute("descuentos", hasItem(
				allOf(hasProperty("id", is(2)), hasProperty("descuento", is(20)) ) )));
	}
	/**
	 * Situacion (2): Post indicando edad 19, devolvera una fila (de acuerdo con el mock)
	 * Ilustra el paso de parametros hacia el controlador y diferntes formas 
	 * de comprobar los valores que devuelve el este:
	 * <br>- "command" es el campo donde el usuario introduce la edad (en este caso, 19)
	 * <br>- "descuento" contiene la lista de descuentos (id,descuento) que devuelve el controlador
	 * <br>Ademas de usar matchers, ilustra como se determinan los objetos que el controlador envia a la vista
	 * para hacer comparaciones, y la obtencion del contenido completo html de la respuesta
	 */
	@Test
	public void testPostRequest() throws Exception {
		//en el post debe especificar el parametro del form (campos DescuentoFormDTO)
		ResultActions res=mvc.perform(post("/descuentos").param("edad", "19"))
				.andExpect(status().isOk());
		res.andExpect(model().attribute("command", hasProperty("edad", is(19))));
		res.andExpect(model().attribute("descuentos", hasSize(1)));
		res.andExpect(model().attribute("descuentos", hasItem(
				allOf(hasProperty("id", is(2)), hasProperty("descuento", is(20)) ) )));
		//Las comparaciones se pueden realizar tambien usando los objetos del Model
		@SuppressWarnings("unchecked")
		List<DescuentoDisplayDTO> dto=(List<DescuentoDisplayDTO>) res.andReturn().getModelAndView().getModel().get("descuentos");
		assertEquals(1,dto.size());
		assertEquals(2,dto.get(0).getId().intValue());
		assertEquals(20,dto.get(0).getDescuento().intValue());
		//Con getResponse se obtiene el contenido de la respuesta, donde se puede buscar p.e. la presencia de strings
		assertThat(res.andReturn().getResponse().getContentAsString(), 
				containsString("<title>Descuentos de clientes</title>"));
	}

}

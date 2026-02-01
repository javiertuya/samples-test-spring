package giis.demo.descuento.ut.cucumber;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;

import giis.demo.descuento.ClienteRepository;
import giis.demo.descuento.DescuentoDisplayDTO;
import giis.demo.util.Util;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;

/**
 * Define el mapping (glue) de los pasos para el test descrito en Gherkin (descuento-database.story). Maneja
 * las estructuras tabulares que representan tablas en base de datos o en la salida de la aplicacion Las
 * comparaciones de tablas se realizan comparando la representacion de estas tablas como strings csv
 */
@CucumberContextConfiguration // para integracion de Cucumber con Spring
@DataJpaTest
public class DescuentoDatabaseSteps {
	@Autowired
	private ClienteRepository cliente;
	@Autowired
	private DataSource datasource;
	// puesto que un mismo test ejecutara varios metodos se deben guardar valores generados entre ejecuciones
	private int edad;

	@Given("los clientes en base de datos:")
	public void setClientes(List<Map<String, String>> clientes) {
		JdbcTemplate db = new JdbcTemplate(datasource);
		db.execute("delete from cliente");
		String sql = "insert into cliente(id,edad,nuevo,cupon,tarjeta) values (?,?,?,?,?)";
		// La carga de datos es similar a cuando no se utiliza Spring, pero en este caso del metodo
		// a utilizar es batchUpdate que requiere los parametros como una lista de arrays, por lo que 
		// se crea primero esta estructura y luego se ejecuta una solo sentencia que cargara todas las filas
		List<Object[]> params = new ArrayList<>();
		for (Map<String, String> row : clientes) {
			Object[] paramsarray = new Object[] { row.get("id"), row.get("edad"), row.get("nuevo"), row.get("cupon"), row.get("tarjeta") };
			params.add(paramsarray);
		}
		db.batchUpdate(sql, params);
	}

	@When("ver informe descuentos clientes de edad {word}")
	public void setEdad(String edadOrCualquiera) {
		if ("cualquiera".equals(edadOrCualquiera))
			edad = 0; // edad minima a partir de la que se muestran resultados (asume edad no puede ser negativa)
		else
			edad = Integer.parseInt(edadOrCualquiera);
	}

	@Then("los descuentos son:")
	public void getDescuentos(List<Map<String, String>> descuentos) {
		List<DescuentoDisplayDTO> descuentosDTO = cliente.getListaDescuentos(edad);
		// convierte la lista de DTOs utilizando una utilidad para convertir a csv, indicando los separadores de Gherkin
		String actual = Util.pojosToCsv(descuentosDTO, new String[] { "id", "descuento" }, true, "|", "|", "|", "");
		String expected = Util.mapsToCsv(descuentos, new String[] { "id", "descuento" }, true, "|", "|", "|", "");
		assertEquals(expected, actual);
	}
}

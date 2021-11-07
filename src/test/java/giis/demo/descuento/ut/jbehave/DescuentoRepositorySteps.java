package giis.demo.descuento.ut.jbehave;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.jbehave.core.annotations.BeforeScenario;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.jbehave.core.model.ExamplesTable;

import giis.demo.descuento.*;
import giis.demo.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
/**
 * Define el mapping de los pasos para el test descrito en Gherkin (descuento-database.story).
 * Maneja las estructuras tabulares que representan tablas en base de datos o en la salida de la aplicacion
 * representadas con variables de tipo ExamplesTable 
 * (https://jbehave.org/reference/stable/tabular-parameters.html,
 * https://jbehave.org/reference/stable/javadoc/core/org/jbehave/core/model/ExamplesTable.html).
 * Las comparaciones de tablas se realizan comparando la representacion de estas tablas como strings
 * de forma similar a cuando no se ejecuta en Spring, cambiando solamente la forma en que se cargan los
 * datos de prueba.
 */
@Component
public class DescuentoRepositorySteps {
    //puesto que un mismo test ejecutara varios pasos se deben guardar valores generados en un paso para uso en el siguiente
    private int edad;
	@Autowired private ClienteRepository cliente;
	@Autowired private DataSource datasource;
    private JdbcTemplate db;

    @BeforeScenario
    public void beforeEachScenario() {
    }
    @Given("los clientes en base de datos: $clientesbd")
    public void setClientes(ExamplesTable clientes) {
    	db=new JdbcTemplate(datasource);
    	//en este caso es necesario limpiar previamente pues no se ejecuta desde el runner de spring
    	db.execute("delete from cliente");
    	String sql="insert into cliente(id,edad,nuevo,cupon,tarjeta) values (?,?,?,?,?)";
    	//La carga de datos es similar a cuando no se utiliza Spring, pero en este caso del metodo
    	//a utilizar es batchUpdate que requiere los parametros como una lista de arrays,
    	//por lo que se crea primero esta estructura y luego se ejecuta una solo sentencia que cargara todas las filas
    	List<Object[]> params=new ArrayList<>();
    	for (Map<String,String> row : clientes.getRows()) {
    		Object[] paramsarray=new Object[] {row.get("id"), row.get("edad"), row.get("nuevo"), row.get("cupon"), row.get("tarjeta")} ;
    		params.add(paramsarray);
    	}
    	db.batchUpdate(sql,params);
    }
    @When("ver informe descuentos clientes de edad $edad")
    public void setEdad(String edadOrCualquiera) {
    	if ("cualquiera".equals(edadOrCualquiera))
    		edad=0; //edad minima a partir de la que se muestran resultados (asume edad no puede ser negativa)
    	else
    		edad=Integer.parseInt(edadOrCualquiera);
    }
    @Then("los descuentos son: $descuentos")
    public void getDescuentos(ExamplesTable descuentos) {
    	List<DescuentoDisplayDTO> descuentosDTO=cliente.getListaDescuentos(edad);
    	//convierte la lista de DTOs utilizando una utilidad para convertir a csv, indicando los separadores de JBehave
    	String actual=Util.pojosToCsv(descuentosDTO, new String[] {"id","descuento"},true,"|","|","|","");
    	String expected=descuentos.asString();
    	assertEquals(expected,actual);
     }
}
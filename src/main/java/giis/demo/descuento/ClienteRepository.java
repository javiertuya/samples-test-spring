package giis.demo.descuento;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

/**
 * Logica de negocio: Spring autogenera todos los DAOs y otros metodos del repositorio, aqui solo se
 * implementan los metodos adicionales que se necesiten.
 */
public interface ClienteRepository extends CrudRepository<Cliente, String> {
	/**
	 * Obtencion de la lista de clientes y descuentos a partir de una cierta edad.
	 * 
	 * Podria implementarse en un servicio o con codigo java en el repositorio, pero se muestra aqui una de las
	 * formas de definir de forma declarativa la query mediante JQL. Notar que la sintaxis es como SQL, pero en
	 * este caso, la estructura devuelta no es un objeto Cliente, sino un DTO. Ver mas informacion en:
	 * https://www.petrikainulainen.net/programming/spring-framework/spring-data-jpa-tutorial-creating-database-queries-with-the-query-annotation/
	 * y en:
	 * https://stackoverflow.com/questions/36328063/how-to-return-a-custom-object-from-a-spring-data-jpa-group-by-query
	 */
	@Query(value = """
			SELECT new giis.demo.descuento.DescuentoDisplayDTO(
				id,
				CASE WHEN nuevo='S' and cupon='N' and tarjeta='N' then 15
					WHEN nuevo='S' and cupon='S' and tarjeta='N' then 20
					WHEN nuevo='N' and cupon='S' and tarjeta='N' then 20
					WHEN nuevo='N' and cupon='N' and tarjeta='S' then 10
					WHEN nuevo='N' and cupon='S' and tarjeta='S' then 30 
					ELSE 0 END)
			FROM Cliente
			WHERE NOT (nuevo='S' AND tarjeta='S')
				AND NOT (nuevo='N' AND tarjeta='N' AND cupon='N')
				AND (edad >= :edad OR :edad IS NULL)
			""")
	List<DescuentoDisplayDTO> getListaDescuentos(@Param("edad") Integer edad);

	// este podria suprimirse pues sera autogenerado, se muestra aqui solo como ejemplo
	List<Cliente> findAll();
}

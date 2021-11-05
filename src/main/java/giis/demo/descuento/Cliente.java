// tag::sample[]
package giis.demo.descuento;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import lombok.*;

/**
 * Clientes de la entidad bancaria, mapeados directamente a la tabla Cliente.
 * A usar Spring Data define los atributos que seran usados para autogenerar la base de datos.
 * En esta entidad y los DTOs los getters y setters se generan automaticamente con Lombok
 * (http://www.baeldung.com/intro-to-project-lombok).
 * Para ejecutar desde eclipse hay que instalarlo en el entorno. 
 * Ver https://projectlombok.org/setup/eclipse
 */
@Entity
@Getter @Setter @NoArgsConstructor @RequiredArgsConstructor
public class Cliente {
	//el atributo NonNull es para que lombok genere constructor con los campos obligatorios
    @Id
    @Column(columnDefinition="int", nullable=false) private @NonNull Integer id;
    @Column(columnDefinition="int", nullable=false) private @NonNull Integer edad;
    @Column(columnDefinition="char(1)", nullable=false) private @NonNull String nuevo; // S o N (nuevo o habitual)
    @Column(columnDefinition="char(1)", nullable=false) private @NonNull String cupon; // S o N (tiene cupon descuento)
    @Column(columnDefinition="char(1)", nullable=false) private @NonNull String tarjeta; // S o N (tiene tarjeta fidelizacion)
    @Column(columnDefinition="char(2)", nullable=true) private String pais; //anyadido para las pruebas de Mock
}


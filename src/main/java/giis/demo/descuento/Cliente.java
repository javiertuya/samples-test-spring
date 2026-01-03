package giis.demo.descuento;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * Clientes de la entidad bancaria, que seran mapeados a la tabla Cliente.
 */
@Entity
@Getter @Setter @NoArgsConstructor @RequiredArgsConstructor
public class Cliente {
	// el atributo NonNull permite que lombok genere constructor con los campos obligatorios
    @Id
    @Column(columnDefinition="int", nullable = false) private @NonNull Integer id;
    @Column(columnDefinition="int", nullable = false) private @NonNull Integer edad;
    @Column(columnDefinition="char(1)", nullable = false) private @NonNull String nuevo; // S o N (nuevo o habitual)
    @Column(columnDefinition="char(1)", nullable = false) private @NonNull String cupon; // S o N (tiene cupon descuento)
    @Column(columnDefinition="char(1)", nullable = false) private @NonNull String tarjeta; // S o N (tiene tarjeta fidelizacion)
    @Column(columnDefinition="char(2)", nullable = true) private String pais; // anyadido para las pruebas de Mock
}

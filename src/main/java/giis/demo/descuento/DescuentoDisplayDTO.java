package giis.demo.descuento;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

/**
 * Cada una de las filas que muestran al usuario la lista de clientes y descuentos
 */
@Getter @AllArgsConstructor
public class DescuentoDisplayDTO {
    private @NonNull Integer id;
    private @NonNull Integer descuento;
}

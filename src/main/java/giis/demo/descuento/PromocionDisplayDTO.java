package giis.demo.descuento;
import lombok.*;
/**
 * Cada una de las filas que muestran al usuario la lista de clientes y descuentos
 */
@Getter @AllArgsConstructor
public class PromocionDisplayDTO {
    private Integer id;
    private String promo;
}

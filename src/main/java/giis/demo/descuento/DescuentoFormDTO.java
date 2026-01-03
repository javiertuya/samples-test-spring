package giis.demo.descuento;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Parametros del formulario que son enviados al controlador desde la vista de Descuentos
 */
@Getter @Setter @NoArgsConstructor
public class DescuentoFormDTO {
	@Min(0)
	private Integer edad;
}

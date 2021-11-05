package giis.demo.descuento;

import javax.validation.constraints.Min;

import lombok.*;

/**
 * Parametros del formulario que son enviados al controlador desde la vista de Descuentos
 */
@Getter @Setter @NoArgsConstructor
public class DescuentoFormDTO {
	@Min(0)
	private Integer edad;
}

package giis.demo.descuento;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para obtener los descuentos
 * bajo una url /api/decuentos?edad=xxx (edad es opcional, si no se indica, se obtienen todos).
 * No realiza validaciones de los parametros.
 */
//@EnableJpaRepositories("giis.demo.descuento") 
//@EntityScan("giis.demo.descuento")
@RestController
public class DescuentoRestController {
	//el servicio utilizado para obtener los datos
	@Autowired private ClienteService clienteService;

	@GetMapping("/api/descuentos")
    public List<DescuentoDisplayDTO>  descuentos(@RequestParam(name="edad", defaultValue="") Integer edad) {
		return clienteService.getListaDescuentos(edad); //edad sera null si no se ha especificado
	}
}

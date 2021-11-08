package giis.demo.descuento;

import java.util.List;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * Controlador web para la funcionalidad de visualizacion descuentos de clientes.
 * Define los endpoints que invoca la vista y ejecuta la logica de negocio correspondiente.
 * La comunicacion con la vista se realiza mediante un objeto Model que contiene una serie de entradas 
 * con los objetos que se comunican hacia la vista (descuentos y clientes) y con los que se reciben 
 * desde la vista en el post (command)
 */
@Controller
public class DescuentoWebController {
	private static final String DESCUENTOS_TEMPLATE = "descuentos"; //html de la vista
	private static final String DESCUENTOS_MODEL = "descuentos"; //componente del modelo con datos enviados a la vista
	private static final String FILTRO_MODEL = "filtro"; //otros valores enviados a la vista (ultimo filtro aplicado)
	private static final String CLIENTES_MODEL = "clientes";
	private static final String DESCUENTOS_FORM = "command"; //datos recibidos desde el formulario html de la vista
	private static final Logger log = LoggerFactory.getLogger(DescuentoApplication.class);

	//el servicio utilizado para obtener los datos
	@Autowired private ClienteService clienteService;
	//este acceso directo al repositorio solo se hace para mostrar tambien la lista de clientes (para depuracion)
	@Autowired private ClienteRepository clienteRepo;
	//para obtener el numero de version  https://www.vojtechruzicka.com/spring-boot-version/
	@Autowired private BuildProperties buildProperties;
	/**
	 * Pagina inicial, se encarga de poner los datos iniciales en el objeto model
	 */
	@GetMapping("/"+DESCUENTOS_TEMPLATE)
    public String descuentos(Model model){
		log.info("GET Descuentos");
		model.addAttribute(DESCUENTOS_FORM, new DescuentoFormDTO());
		fillMasterData(model);
		fillDescuentos(0,model);
        return DESCUENTOS_TEMPLATE;
	}
	/**
	 * Pagina resultado de un postback, recibe command con la edad del cliente a usar como filtro
	 * y pone los valores correspondientes a los objetos que se veran en la vista.
	 * Ver https://medium.com/@grokwich/spring-boot-thymeleaf-html-form-handling-762ef0d51327 
	 * para las difrentes formas en que se mapean los campos de un form a objetos
	 */
	@PostMapping("/"+DESCUENTOS_TEMPLATE)
	public String descuentosSubmit(@Valid @ModelAttribute(DESCUENTOS_FORM) DescuentoFormDTO command, BindingResult bindingResult, Model model) {
		log.info("POST Descuentos");
		fillMasterData(model);
		if (!bindingResult.hasErrors()) //validacion: https://spring.io/guides/gs/validating-form-input/
			fillDescuentos(command.getEdad(),model);
		return DESCUENTOS_TEMPLATE;
	}
	private void fillMasterData(Model model) {
		model.addAttribute("version", buildProperties.getVersion());
		Iterable<Cliente> clientes=clienteRepo.findAll();
		model.addAttribute(CLIENTES_MODEL,clientes);
	}
	private void fillDescuentos(Integer edad, Model model) {
		List<DescuentoDisplayDTO> descuentos=clienteService.getListaDescuentos(edad);
		model.addAttribute(DESCUENTOS_MODEL,descuentos);
		model.addAttribute(FILTRO_MODEL, edad==null || edad==0 ? "n/a" : edad);
	}

}

package giis.demo.descuento;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Obgiene datos, descuentos y promociones de clientes
 */
@Service
public class ClienteService {
	@Autowired ClienteRepository cliente;
	@Autowired MarketingApi marketing;
	
	/**
	 * La obtencion de lista de descuentos se limita a obtener los datos del repositorio
	 */
	public List<DescuentoDisplayDTO> getListaDescuentos(Integer edad) {
		return cliente.getListaDescuentos(edad);
	}
	/**
	 * La obtencion de la lista de promociones consulta los clientes del repositorio y 
	 * loas promociones del api de marketing, devolviendo
	 * para cada cliente el codigo de promocion correspondiente a su pais.
	 * Si todos los datos estuvieran en la misma base de datos, esto se implementaria directamente
	 * como una query en el repositorio, pero en este caso la unica forma de acceder a los datos de
	 * marketing es a traves de su api.
	 */
	public List<PromocionDisplayDTO> getListaPromociones() {
		//datos que seran fusionados (uno procede del repositorio y otro del api de marketing)
		Map<String,String> promosPorPais=marketing.getPromotions();
		List<Cliente> clientes=cliente.findAll();
		//forma la lista de promociones
		List <PromocionDisplayDTO> clientesConPromo=new ArrayList<>();
		for (Cliente item : clientes) {
			String promo=promosPorPais.get(item.getPais());
			clientesConPromo.add(new PromocionDisplayDTO(item.getId(),promo));
		}
		return clientesConPromo;
	}



	

}

package giis.demo.descuento;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Punto de entrada que arranca la aplicacion en el puerto 8080:
 * Solamente personaliza la configuracion del nombre de los paquetes donde se tienen los repositorios y las entidades 
 * (para simplificar el ejemplo, en este caso se ponen bajo el mismo paquete)
 */
@EnableJpaRepositories("giis.demo.descuento") 
@EntityScan("giis.demo.descuento")
@SpringBootApplication
public class DescuentoApplication {

	public static void main(String[] args) {
        SpringApplication.run(DescuentoApplication.class, args);
    }

}

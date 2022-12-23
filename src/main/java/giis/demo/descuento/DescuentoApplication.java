package giis.demo.descuento;

import java.util.Collections;

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
    	//Para el despliegue en Heroku se requiere establecer el puerto mediante la variable de entorno PORT
		//Para Azure se utiliza la configuracion por defecto de la aplicacion 
		//pero hay que definir WEBSITES_PORT=8080 desde el portal: Settings->Configuration
      	String herokuPort=System.getenv("PORT");
    	if (herokuPort==null || "".equals(herokuPort)) { //despliegue normal, puerto por defecto (8080)
    		SpringApplication.run(DescuentoApplication.class, args);
    	} else { //despliegue para Heroku
            SpringApplication app = new SpringApplication(DescuentoApplication.class);
            app.setDefaultProperties(Collections.singletonMap("server.port", herokuPort));
            app.run(args);
    	}
    }

}

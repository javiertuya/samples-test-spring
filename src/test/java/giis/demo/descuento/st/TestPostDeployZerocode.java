package giis.demo.descuento.st;

import org.jsmart.zerocode.core.domain.TargetEnv;
import org.jsmart.zerocode.core.domain.TestPackageRoot;
import org.jsmart.zerocode.core.runner.ZeroCodePackageRunner;
import org.junit.runner.RunWith;

/**
 * Ejemplo de prueba del API fuera del entorno de Spring Boot con Zerocode
 * (https://github.com/authorjapps/zerocode) que permite especificar escenarios
 * de prueba en Json y ejecutarlos.
 * 
 * Se ejecutara sobre la aplicacion desplegada en Azure desde el entorno CI
 * (GitHub Actions). Para probar en local, lanzar el servidor desde
 * src/main/java
 */
@TargetEnv("zerocode.properties") // configura direccion del host
@TestPackageRoot("zerocode") // ejecuta todos los tests (.json o .yaml) en esta carpeta
@RunWith(ZeroCodePackageRunner.class) // este runner buscara los test y los ejecutara
public class TestPostDeployZerocode {
}

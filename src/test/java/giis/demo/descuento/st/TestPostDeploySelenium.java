package giis.demo.descuento.st;
import org.junit.*;

import static org.junit.Assert.assertEquals;

import java.time.Duration;

import org.openqa.selenium.*;

import giis.demo.descuento.it.SeleniumUtil;

/**
 * Prueba web de Selenium fuera del entorno de Spring Boot.
 * 
 * Solamente comprueba el acceso a los elementos basicos de la aplicacion,
 * ya que esta pensado para ejecutarse
 * sobre la aplicacion desplegada en Azure desde el entorno CI (GitHub Actions).
 * Para probar en local, lanzar el servidor desde src/main/java
 */
public class TestPostDeploySelenium {
	private WebDriver driver;

	@Test
	public void testPostDeploySmoke() {
		String url=SeleniumUtil.getApplicationUrl(0); // usa el puerto especificado en selenium.properties
		driver=SeleniumUtil.getNewDriver();
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(15));

		//se dirige a la pagina principal y selecciona el link para ir a la pagina que se va a probar
		driver.get(url);
		SeleniumUtil.takeScreenshot(driver, "postdeploy-main-menu");
		driver.findElement(By.linkText("Ejecutar descuentos de clientes")).click();
		SeleniumUtil.takeScreenshot(driver, "postdeploy-main-application-after-get");
		
		//pone una edad y click
		WebElement txtEdad=driver.findElement(By.id("txtEdad"));
		assertEquals("",txtEdad.getText()); //asegura que no hay filtro previo
		assertEquals("n/a",driver.findElement(By.id("filtro")).getText());
		txtEdad.sendKeys("20");
		driver.findElement(By.id("btnEdad")).click();				
		SeleniumUtil.takeScreenshot(driver, "postdeploy-main-application-after-post");
		
		//comprueba que se ha realizado un post examinando el filtro
		assertEquals("20",driver.findElement(By.id("filtro")).getText());
		driver.quit();
	}

}

package giis.demo.descuento.it;
import org.junit.*;

import static org.junit.Assert.assertEquals;

import java.time.Duration;

import org.openqa.selenium.*;

/**
 * Prueba de humo que solamente comprueba el acceso a los elementos basicos de la aplicacion
 * fuera del entorno de Spring Boot.
 * Solo se ejecutara tras el despliegue de la aplicacion y en el entorno CI (GitHub Actions).
 */
public class TestPostDeploy {
	private WebDriver driver;

	@Test
	public void testPostDeploySmoke() {
		//Si la url (configurada en el .properties si existe) es de una aplicacion desplegada en Heroku o Azure
		//se trata del test post deploy, si no, salta este test
		String url=SeleniumUtil.getApplicationUrl(0);
		if (!url.contains("herokuapp.com") && !url.contains("azurewebsites.net"))
			return;
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

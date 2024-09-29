package giis.demo.descuento.it;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Esta clase representa el Page Object inicial, que habitualmente seria la
 * pagina de login, en este caso es una pagina estatica. Los pageObjects reciben
 * normalmente una instancia del driver, en este caso se anyade el puerto porque
 * ha sido generado dinamicamente por Spring.
 * 
 * Notar que el metodo para navegar hacia una pagina ejecuta las acciones de
 * selenium necesarias y devuelve el page object de la pagina destino. Esta es
 * la forma habitual de navegacion. En el caso de que una accion pueda tener
 * diferentes destinos (p.e. un login correcto e incorrecto), habria dos metodos
 * para realizar la accion que llevara a diferentes destinos.
 */
public class DescuentoMainPo {
	private WebDriver driver;

	public DescuentoMainPo(WebDriver driver, int port) {
		this.driver = driver;
		driver.get(SeleniumUtil.getApplicationUrl(port));
	}

	public DescuentoPo NavigateToDescuentoUsingPo() {
		driver.findElement(By.linkText("Ejecutar descuentos de clientes")).click();
		return new DescuentoPo(driver);
	}

	// Como el anterior pero devolviendo un objeto Page Factory
	public DescuentoPf NavigateToDescuentoUsingPf() {
		driver.findElement(By.linkText("Ejecutar descuentos de clientes")).click();
		return new DescuentoPf(driver);
	}

}

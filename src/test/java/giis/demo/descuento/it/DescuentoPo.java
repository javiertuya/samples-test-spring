package giis.demo.descuento.it;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Ejemplo de Page Object del formulario principal. Define los locators de los
 * diferentes elementos de la pagina como variables y luego metodos para
 * realizar las interacciones con estos elementos y consultar sus valores
 */
public class DescuentoPo {
	private WebDriver driver;
	private By edad = By.id("txtEdad");
	private By edadUpdate = By.id("btnEdad");
	private By filtro = By.id("filtro");
	private By tabDescuentos = By.id("tabDescuentos");

	public DescuentoPo(WebDriver driver) {
		this.driver = driver;
	}

	public String getEdad() {
		return findElementUsingWait(edad).getText();
	}

	public void setEdad(String value) {
		// Aunque en este caso no serian necesarios los waits,
		// si lo fueran se incluira tambien en el page object como se hace en este caso
		WebElement edadElem = findElementUsingWait(edad);
		edadElem.clear();
		edadElem.sendKeys(value);
		driver.findElement(edadUpdate).click();
	}

	private WebElement findElementUsingWait(By locator) {
		return (new WebDriverWait(driver, Duration.ofSeconds(5)))
				.until(ExpectedConditions.presenceOfElementLocated(locator));
	}

	public String getFiltro() {
		return driver.findElement(filtro).getText();
	}

	public String[][] getDescuentos() {
		WebElement tab = driver.findElement(tabDescuentos);
		return SeleniumUtil.getTableContent(tab);
	}
	
}

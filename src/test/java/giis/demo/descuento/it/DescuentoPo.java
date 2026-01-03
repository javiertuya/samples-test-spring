package giis.demo.descuento.it;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Ejemplo de Page Object del formulario principal. Define los locators de los diferentes elementos de la
 * pagina como variables y luego metodos para realizar las interacciones con estos elementos y consultar sus
 * valores
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
		return find(edad).getText();
	}

	public void setEdad(String value) {
		WebElement edadElem = find(edad);
		edadElem.clear();
		edadElem.sendKeys(value);
		driver.findElement(edadUpdate).click();
	}

	public String getFiltro() {
		return find(filtro).getText();
	}

	public String[][] getDescuentos() {
		WebElement tab = find(tabDescuentos);
		return SeleniumUtil.getTableContent(tab);
	}
	
	private WebElement find(By locator) {
		return SeleniumUtil.findUsingWaitUntilVisible(driver, locator);
	}

}

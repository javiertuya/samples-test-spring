package giis.demo.descuento.it;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

/**
 * Ejemplo de Page Object del formulario principal usando la implementacion Page Factory.
 * En este caso las variables no definen los locators, sino que definen los WebElements,
 * por lo que el codigo correspondiente de los metodos que utilizan selenium se simplifica.
 */
public class DescuentoPf {
	// Los WebElement se declaran como variables, la anotacion @FindBy (en selenium.support)
	// incorpora lo necesiario para obtener el elemento
	@FindBy(id="txtEdad") private WebElement edad;
	@FindBy(id="btnEdad") private WebElement edadUpdate;
	@FindBy(id="filtro") private WebElement filtro;
	@FindBy(id="tabDescuentos") private WebElement tabDescuentos;
	WebDriver driver; // necesario para incluir un wait en getFiltro

	public DescuentoPf(WebDriver driver) {
		// Cuando se usa Page Factory, para que funcionen las anotaciones FindBy
		// hay que inicializar estos elementos inyectando el driver
		PageFactory.initElements(driver, this);
		this.driver = driver;
		// Nota: @FindBy no conseguiria obtener los elementos si requieren waits.
		// Para facilitar esto, selenium.support permite utilizar una forma alternativa
		// de inicializacion que permite que la localizacion de elementos se realice
		// de forma similar a cuando se usan waits hasta que los elementos esten visibles.
		// La inicializacion se realizaria con la siguiente linea (timeout de 10 segundos):
		// NOSONAR PageFactory.initElements(new org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory(driver, 10), this);
	}

	// Notar como el codigo de todos los metodos se simplifica ligeramente respecto de DescuentoPo
	// pero todavia puede ser necesario usar waits aunque se use AjaxElementLocatorFactory

	public String getEdad() {
		return edad.getText();
	}

	public void setEdad(String value) {
		edad.clear();
		edad.sendKeys(value);
		edadUpdate.click();
	}

	public String getFiltro() {
		// El estado del filtro aplicado tras un post requiere un wait hasta que sea visible, 
		// (ver comentario en TestDescuentoSelenium).
		// Aunque se usase AjaxElementLocatorFactory, los waits que implementa esperan a que los elementos 
		// esten presentes (no visibles). Se implementa aqui el wait
		SeleniumUtil.findUsingWaitUntilVisible(driver, By.id("filtro"));
		return filtro.getText();
	}

	public String[][] getDescuentos() {
		return SeleniumUtil.getTableContent(tabDescuentos);
	}

}

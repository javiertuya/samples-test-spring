package giis.demo.descuento.it;

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

	public DescuentoPf(WebDriver driver) {
		// Cuando se usa Page Factory, para que funcionen las anotaciones FindBy 
		// hay que inicializar estos elementos inyectando el driver
		PageFactory.initElements(driver, this);
		
		// Nota: @FindBy no conseguiria obtener los elementos si requieren waits.
		// Para facilitar esto, selenium.support permite utilizar una forma alternativa
		// de inicializacion que permite que la localizacion de elementos se realice 
		// de forma similar a cuando se usan waits con un timeout.
		// La inicializacion se realizaria con la siguiente linea (timeout de 10 segundos):
		// NOSONAR PageFactory.initElements(new org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory(driver, 10), this);
	}

	// Notar como el codigo de todos los metodos se simplifica respecto de DescuentoPo
	
	public String getEdad() {
		return edad.getText();
	}

	public void setEdad(String value) {
		edad.clear();
		edad.sendKeys(value);
		edadUpdate.click();
	}

	public String getFiltro() {
		return filtro.getText();
	}

	public String[][] getDescuentos() {
		return SeleniumUtil.getTableContent(tabDescuentos);
	}
	
}

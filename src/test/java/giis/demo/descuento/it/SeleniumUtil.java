package giis.demo.descuento.it;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Files;

import giis.demo.util.ApplicationException;
import io.github.bonigarcia.wdm.WebDriverManager;

/**
 * Utilidades varias para uso en los tests con Selenium
 */
public class SeleniumUtil {
	private static final Logger log = LoggerFactory.getLogger("giis.demo.descuento.it.SeleniumUtil");

	/**
	 * Instancia un WebDriver para el navegador usado en estos tests (Chrome). WebDriver es un interface que debe
	 * instanciarse con el driver correspondiente al navegador a utilizar, pero no existe una factoria que lo
	 * haga, por lo que se utiliza este metodo.
	 * 
	 * <br/>
	 * Notar la diferencia entre driver, que es el fichero externo que realiza el interfaz con el navegador y los
	 * bindings (Selenium Client and WebDriver Language Bindings) que son las dependencias que definen los metodos
	 * con los que se interactua con WebDriver. Tambien existe un remote web driver que permite ejecutar el
	 * navegador en otro equipo diferente.
	 * 
	 * <br/>
	 * Estos drivers son archivos ejecutables que se guardaran en las carpetas de recursos pero que no se pueden
	 * obtener directamente mediante maven. Para automatizar la obtencion de los drivers locales se utiliza el
	 * componente webdrivermanager que se encarga de descargar y poner accesibles los binarios en el path.
	 * 
	 * @return el WebDriver instanciado.
	 */
	public static WebDriver getNewDriver() {
		WebDriver driver;
		if (WebConfig.isLocal()) { // driver local
			// La descarga de binarios del driver se hace con WebDriverManager.
			// Notar que desde 2022, Selenium incorpora de forma nativa el SeleniumManager, por lo que 
			// se podria eliminar el uso de WebDriverManager. No se hace porque aunque es bastante estable, 
			// todavia es beta hasta la v5 de selenium
			WebDriverManager.chromedriver().setup();
			log.info("Using local driver");
			ChromeOptions options = new ChromeOptions();
			if (WebConfig.isHeadless()) {
				log.info("Using headless driver");
				options.addArguments("--headless", "--remote-allow-origins=*");
			}
			options.addArguments("--window-size=1024,768");
			driver = new ChromeDriver(options);
		} else { // assume a well formed url
			String remoteUrl = WebConfig.getRemoteUrl();
			log.info("Using remote driver: " + remoteUrl);
			ChromeOptions options = new ChromeOptions();
			driver = new RemoteWebDriver(getNativeUrl(remoteUrl), options);
		}
		return driver;
	}

	private static java.net.URL getNativeUrl(String url) {
		try {
			return new java.net.URL(url);
		} catch (MalformedURLException e) {
			throw new ApplicationException("Can't create url " + url);
		}
	}

	/**
	 * Utilidad para obtencion de todos los elementos de una tabla a partir del WebElement que apunta a esta.
	 * Obtiene como un string y por orden todos los elementos encerrados entre th o tr. Como La tabla puede no
	 * tener un numero igual de celdas en cada fila, el numero de columnas devuelto sera el de la fila mas larga.
	 * 
	 * @param tableElement WebElement que apunta a un elemento table.
	 * @return Matriz de strings con el contenido de cada celda (incluyendo headers).
	 */
	public static String[][] getTableContent(WebElement tableElement) {
		List<List<String>> orows = new ArrayList<>();
		int maxcol = 0;
		List<WebElement> irows = tableElement.findElements(By.tagName("tr"));
		for (WebElement irow : irows) {
			List<String> ocols = new ArrayList<>();
			// busca en la fila: elementos td, si no existen busca th
			List<WebElement> icols = irow.findElements(By.tagName("td"));
			if (icols.size() == 0)
				icols = irow.findElements(By.tagName("th"));
			// acumula las celdas de esta fila, calculando siempre el maximo total de columnas
			int numcol = 0;
			for (WebElement icol : icols) {
				ocols.add(icol.getText());
				numcol++;
				maxcol = maxcol < numcol ? numcol : maxcol;
			}
			orows.add(ocols);
		}
		// convierte a arrays rellenando con espacios en blanco cuando sea necesario
		return listToStringMatrix(orows, maxcol);
	}

	private static String[][] listToStringMatrix(List<List<String>> irows, int maxcol) {
		String[][] orows = new String[irows.size()][maxcol];
		for (int i = 0; i < irows.size(); i++) {
			for (int j = 0; j < irows.get(i).size(); j++) // copia columnas de la fila
				orows[i][j] = irows.get(i).get(j);
			for (int j = irows.get(i).size(); j < maxcol; j++) // rellena con blancos hasta maxcol
				orows[i][j] = "";
		}
		return orows;
	}

	/**
	 * Toma una imagen de la vista actual del navegador y lo guarda en target/screenshots.
	 * 
	 * @param name nombre que se dara a la imagen (le anyade un timestamp para diferenciar imagenes guardadas en
	 *             la misma sesion)
	 */
	public static void takeScreenshot(WebDriver driver, String name) {
		String imageFolderPath = "target/site/screenshot/";
		String timestamp = String.valueOf(System.currentTimeMillis()); // diferencia archivos en varias ejecuciones
		String fileName = imageFolderPath + timestamp + "-" + name + ".png";
		// toma la imagen y la guarda en el archivo
		File imageFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
		try {
			log.info("Take screenshot: " + fileName);
			File newFile = new File(fileName);
			Files.createParentDirs(newFile);
			Files.copy(imageFile, newFile);
		} catch (IOException e) {
			throw new ApplicationException(e);
		}
	}

	/**
	 * Localiza un elemento utilizando un wait que espera a que se encuentre presente y visible en el markup
	 * 
	 * Nota 2025: Ejecutando en local (comprobado con Chrome 143) en algunas ocasiones falla el wait cuando
	 * intenta localizar un span, produciendo una excepcion del driver que indica "Node with given id does not
	 * belong to the document"
	 * 
	 * Parece que es un bug del ChromeDriver que se arrastra desde Chrome 130 por una race condition: Cuando
	 * Chrome actualiza el DOM y el driver intenta leer el nodo justo en este microinstante, ocurre la excepcion.
	 * Se parcheara comprobando esta excepcion y reejecutando el wait
	 */
	public static WebElement findUsingWaitUntilVisible(WebDriver driver, By locator) {
		try {
			return (new WebDriverWait(driver, Duration.ofSeconds(5)))
				.until(ExpectedConditions.visibilityOfElementLocated(locator));
		} catch (WebDriverException e) {
			if (e.getMessage().contains("Node with given id does not belong")) {
				sleep(500);
				return (new WebDriverWait(driver, Duration.ofSeconds(5)))
						.until(ExpectedConditions.visibilityOfElementLocated(locator));
			} else {
				throw e;
			}
		}
	}

	public static void sleep(long millis) {
		try {
			Thread.sleep(millis); // NOSONAR
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

}

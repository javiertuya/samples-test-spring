package giis.demo.descuento.it;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

/**
 * Utilidades varias para uso en los tests con Playwright, equivalente a SeleniumUtil pero usando la API de
 * Playwright (https://playwright.dev/java/). Mantiene la misma estructura que SeleniumUtil para que se puedan
 * comparar ambas tecnologias; los comentarios resaltan unicamente las diferencias respecto de la version Selenium.
 */
public class PlaywrightUtil {
	private static final Logger log = LoggerFactory.getLogger("giis.demo.descuento.it.PlaywrightUtil");

	private PlaywrightUtil() {
		// clase de utilidades, no se instancia
	}

	/**
	 * Lanza el navegador (Chromium) usado en estos tests teniendo en cuenta el fichero de propiedades, de forma
	 * analoga a SeleniumUtil.getNewDriver.
	 *
	 * <br/>
	 * Playwright siempre descarga y gestiona sus propios navegadores como cuando se usa SeleniumManager.
	 * El objeto Playwright recibido lanza directamente el navegador (la primera ejecucion descarga los binarios.
	 * Se puede forzar con "mvn exec:java -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args=install").
	 *
	 * <br/>
	 * El comportamiento se controla con la propiedad remote.web.driver.url (se reutiliza la misma configuracion
	 * que en Selenium, selenium.properties, para no duplicar ficheros de propiedades):
	 * <pre>
	 * - En ejecucion local no se definen propiedades y se lanza un navegador local en modo headed (visible).
	 * - El valor "headless" lanza un navegador local en modo headless (compatible con CI).
	 * - Cualquier otro valor se interpreta como una url a la que conectarse via CDP (connectOverCDP).
	 * </pre>
	 *
	 * @param playwright punto de entrada de Playwright ya inicializado (Playwright.create()).
	 * @return el Browser lanzado (local) o al que se ha conectado (remoto).
	 */
	public static Browser getNewBrowser(Playwright playwright) {
		// En ejecucion local no se definen propiedades y toma los valores por defecto
		// En integracion continua se puede usar un navegador headless o un grid remoto compatible con CDP
		String remoteUrl = getRemoteBrowserProperty();
		if ("".equals(remoteUrl) || "headless".equals(remoteUrl)) { // navegador local
			boolean headless = "headless".equals(remoteUrl);
			log.info("Using local browser (headless={})", headless);
			return playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(headless));
		} else { // se asume una url bien formada (para usar con infraestructura compatible con CDP)
			log.info("Using remote browser override: {}. Switch to headless", remoteUrl);
			// Cuando se indica una url remota, tambien lanza navegador headless porque falla con selenium grid.
			// NOSONAR return playwright.chromium().connectOverCDP(remoteUrl);
			return playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
		}
	}

	public static String getRemoteBrowserProperty() {
		// se reutiliza la configuracion de Selenium (selenium.properties) para no duplicar ficheros de propiedades
		return SeleniumUtil.getRemoteWebDriverProperty();
	}

	/**
	 * Equivalente con Playwright a SeleniumUtil.getTableContent: obtiene el contenido de una tabla como matriz de
	 * strings. La logica es la misma (recorrer tr y, dentro, td o th), pero usando Locators de Playwright en vez de
	 * WebElement. Como la tabla puede no tener un numero igual de celdas en cada fila, el numero de columnas
	 * devuelto sera el de la fila mas larga, rellenando con blancos.
	 *
	 * @param table Locator que apunta a un elemento table.
	 * @return Matriz de strings con el contenido de cada celda (incluyendo headers).
	 */
	public static String[][] getTableContent(Locator table) {
		List<List<String>> orows = new ArrayList<>();
		int maxcol = 0;
		Locator irows = table.locator("tr");
		int nrows = irows.count();
		for (int i = 0; i < nrows; i++) {
			Locator irow = irows.nth(i);
			// busca en la fila: elementos td, si no existen busca th
			Locator icols = irow.locator("td");
			if (icols.count() == 0)
				icols = irow.locator("th");
			// acumula las celdas de esta fila, calculando siempre el maximo total de columnas
			int ncols = icols.count();
			List<String> ocols = new ArrayList<>();
			for (int j = 0; j < ncols; j++)
				ocols.add(icols.nth(j).innerText());
			maxcol = maxcol < ncols ? ncols : maxcol;
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
	 * Toma una imagen de la vista actual del navegador y la guarda en target/site/screenshot. A diferencia de
	 * Selenium (SeleniumUtil.takeScreenshot, que necesita castear a TakesScreenshot y crear los directorios),
	 * Playwright captura directamente con page.screenshot y crea las carpetas necesarias.
	 *
	 * @param page pagina de Playwright sobre la que se toma la imagen.
	 * @param name nombre que se dara a la imagen (le anyade un timestamp para diferenciar imagenes guardadas en la
	 *             misma sesion).
	 */
	public static void takeScreenshot(Page page, String name) {
		String imageFolderPath = "target/site/screenshot/";
		String timestamp = String.valueOf(System.currentTimeMillis()); // diferencia archivos en varias ejecuciones
		String fileName = imageFolderPath + timestamp + "-" + name + ".png";
		log.info("Take screenshot: " + fileName);
		page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get(fileName)));
	}

}

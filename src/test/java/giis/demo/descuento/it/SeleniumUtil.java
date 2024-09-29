package giis.demo.descuento.it;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.bonigarcia.wdm.WebDriverManager;

import com.google.common.io.Files;

import giis.demo.util.ApplicationException;

/**
 * Utilidades varias para uso en los tests con Selenium
 */
public class SeleniumUtil {
	private static final Logger log = LoggerFactory.getLogger("giis.demo.descuento.it.SeleniumUtil");
	//Archivo de configuracion para las pruebas web, si no existe, las propiedades tomaran valores por defecto
	//(driver chrome local y aplicacion en localhost, el puerto lo establece spring)
	private static final String SELENIUM_PROPERTIES = "samples-test-spring.properties";
	/**
	 * Instancia un WebDriver para el navegador usado en estos tests (Chrome).
	 * WebDriver es un interface que debe instanciarse con el driver correspondiente al navegador a utilizar,
	 * pero no existe una factoria que lo haga, por lo que se utiliza este metodo.
	 * 
	 * <br/>Notar la diferencia entre driver, que es el fichero externo que realiza el interfaz con el navegador
	 * y los bindings (Selenium Client and WebDriver Language Bindings) que son las dependencias que definen
	 * los metodos con los que se interactua con WebDriver.
	 * Tambien existe un remote web driver que permite ejecutar el navegador en otro equipo diferente.
	 * 
	 * <br/>Estos drivers son archivos ejecutables que se guardaran en las carpetas de recursos pero que no
	 * se pueden obtener directamente mediante maven. Para automatizar la obtencion de los drivers locales
	 * se utiliza el componente webdrivermanager que se encarga de descargar y poner accesibles los binarios en el path.
	 * 
	 * @return el WebDriver instanciado.
	 */
	public static WebDriver getNewDriver() {
		//Utiliza un archivo de propiedades para definir si el driver es local o remoto
		//En ejecucion local no se definen propiedades y toma los valores por defecto
		//En integracion continua con GitHub o Jenkins usara un chrome ejecutado en selenoid
		//En integracion continua post deploy usara un chrome headless que viene instalado en los ejecutores
		WebDriver driver;
		String remoteUrl=getRemoteWebDriverProperty();
		if ("".equals(remoteUrl) || "headless".equals(remoteUrl)) { //driver local
			//La descarga de binarios con WebDriverManager se suele hacer solamente una vez o una por clase
			//pero en este caso de ejemplo solo se prueba un escenario, se hace aqui
			WebDriverManager.chromedriver().setup();
			log.info("Using local driver");
			ChromeOptions options = new ChromeOptions();
			if ("headless".equals(remoteUrl)) {
				log.info("Using headless driver");
				options.addArguments("--headless", "--remote-allow-origins=*");
			}
			options.addArguments("--window-size=1024,768");
			driver=new ChromeDriver(options);		
		} else { //assume a well formed url (to use with selenoid)
			log.info("Using remote driver: " + remoteUrl);
			ChromeOptions options=new ChromeOptions();
			//Para grabar videos (debe existir un container selenoid/video-recorder
			//Desde Selenium 4.9.0 las capabilities de selenoid se deben pasar bajo una clave con el vendor extension
			Map<String, Object> selenoidOptions=new HashMap<>();
			selenoidOptions.put("enableVideo", true);
			options.setCapability("selenoid:options", selenoidOptions);
			//Para poder ver en vivo la ejecucion con selenoid-ui anyadir "enableVNC" a true
			driver=new RemoteWebDriver(getNativeUrl(remoteUrl), options);
		}
		return driver;
	}
	/**
	 * Obtiene la url del remote web driver de selenium, si no existe el fichero de configuracion, develve "" (driver local)
	 */
	public static String getRemoteWebDriverUrl() {
		String remoteUrl=getRemoteWebDriverProperty();
		if ("".equals(remoteUrl) || "headless".equals(remoteUrl))
			return ""; //headless means that driver is local
		return remoteUrl;
	}
	public static String getRemoteWebDriverProperty() {
		return getProperty(SELENIUM_PROPERTIES, "remote.web.driver.url", "");
	}
	/**
	 * Obtiene la url a probar a partir de la especificada en la configuracion y el puerto indicado como parametro,
	 * si no existe el fichero de propiedades, utiliza localhost como valor por defecto
	 * Anyade el valor del puerto si este es mayor que cero
	 */
	public static String getApplicationUrl(int port) {
		String url=getProperty(SELENIUM_PROPERTIES, "application.url", "http://localhost") + (port>0 ? ":" + port : "");
		log.info("Application url: " + url);
		return url;
	}
    private static java.net.URL getNativeUrl(String url) {
    	try {
			return new java.net.URL(url);
		} catch (MalformedURLException e) {
			throw new ApplicationException("Can't create url "+url);
		}
    }
	public static String getProperty(String propFileName, String propName, String defaultValue) {
		java.util.Properties prop=new java.util.Properties();
		File propFile=new File(propFileName);
		if (!propFile.exists()) {
			return defaultValue;
		}
		try {
			prop.load(new FileInputStream(propFileName));
		} catch (IOException e) {
			throw new ApplicationException("Can't load properties file "+propFileName);
		}
		String propValue=prop.getProperty(propName.trim()).trim();
		if (propValue==null)
			throw new ApplicationException("Can't read property "+propName);
		return propValue.trim();
	}

	/**
	 * Utilidad para obtencion de todos los elementos de una tabla a partir del WebElement que apunta a esta.
	 * Obtiene como un string y por orden todos los elementos encerrados entre th o tr.
	 * Como La tabla puede no tener un numero igual de celdas en cada fila, el numero de columnas devuelto sera
	 * el de la fila mas larga.
	 * @param tableElement WebElement que apunta a un elemento table.
	 * @return Matriz de strings con el contenido de cada celda (incluyendo headers).
	 */
	public static String[][] getTableContent(WebElement tableElement) {
		List<List<String>> orows=new ArrayList<>();
		int maxcol=0;
		List<WebElement> irows=tableElement.findElements(By.tagName("tr"));
		for (WebElement irow : irows) {
			List<String> ocols=new ArrayList<>();
			//busca en la fila: elementos td, si no existen busca th
			List<WebElement> icols=irow.findElements(By.tagName("td"));
			if (icols.size()==0)
				icols=irow.findElements(By.tagName("th"));
			//acumula las celdas de esta fila, calculando siempre el maximo total de columnas
			int numcol=0;
			for (WebElement icol : icols) {
				ocols.add(icol.getText());
				numcol++;
				maxcol = maxcol<numcol ? numcol : maxcol;
			}
			orows.add(ocols);
		}
		//convierte a arrays rellenando con espacios en blanco cuando sea necesario
		return listToStringMatrix(orows,maxcol);
	}
	private static String[][] listToStringMatrix(List<List<String>> irows, int maxcol) {
		String[][] orows=new String[irows.size()][maxcol];
		for (int i=0; i<irows.size(); i++) {
			for (int j=0; j<irows.get(i).size(); j++) //copia columnas de la fila
				orows[i][j]=irows.get(i).get(j);
			for (int j=irows.get(i).size(); j<maxcol; j++) //rellena con blancos hasta maxcol
				orows[i][j]="";
		}
		return orows;
	}
	/**
	 * Toma una imagen de la vista actual del navegador y lo guarda en target/screenshots.
	 * @param name nombre que se dara a la imagen 
	 * (le anyade un timestamp para diferenciar imagenes guardadas en la misma sesion)
	 */
	public static void takeScreenshot(WebDriver driver, String name) {
		String imageFolderPath = "target/site/screenshot/";
		String timestamp = String.valueOf(System.currentTimeMillis()); //diferencia archivos en varias ejecuciones
		String fileName=imageFolderPath + timestamp + "-" + name + ".png";
		//toma la imagen y la guarda en el archivo
		File imageFile = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
		try {
			log.info("Take screenshot: " + fileName);
			File newFile=new File(fileName);
			Files.createParentDirs(newFile);
			Files.copy(imageFile, newFile);
		} catch (IOException e) {
			throw new ApplicationException(e);
		}
	}
	public static void sleep(long millis) {
		try {
			Thread.sleep(millis); //NOSONAR
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

}

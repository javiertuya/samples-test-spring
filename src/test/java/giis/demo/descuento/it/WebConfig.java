package giis.demo.descuento.it;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import giis.demo.util.ApplicationException;

/**
 * Configuracion para los tests web: gestiona la lectura del fichero de propiedades con 
 * la configuracion del web driver y las urls de la aplicacion bajo prueba.
 */
public class WebConfig {
	private static final Logger log = LoggerFactory.getLogger("giis.demo.descuento.it.WebConfig");

	// Archivo de configuracion para las pruebas web, si no existe, las propiedades tomaran valores por defecto
	// (driver chrome local y aplicacion en localhost, el puerto lo establece spring)
	private static final String SELENIUM_PROPERTIES = "src/test/resources/selenium.properties";

	// Valores especiales de la propiedad remote.web.driver.url: vacio indica navegador local (headed),
	// "headless" indica navegador local en modo headless; cualquier otro valor se interpreta como url remota
	private static final String LOCAL = "";
	private static final String HEADLESS = "headless";

	private WebConfig() {
		// clase de utilidades, no se instancia
	}

	/**
	 * Devuelve la url del navegador remoto a usar, leida del fichero de propiedades.
	 * Usar solamente si se ha verificado el uso de remote driver con isLocal().
	 */
	public static String getRemoteUrl() {
		return getProperty(SELENIUM_PROPERTIES, "remote.web.driver.url", LOCAL);
	}

	/**
	 * Indica si la configuracion corresponde a un navegador local (sea headed o headless), en contraposicion a
	 * un navegador/grid remoto identificado por una url.
	 */
	public static boolean isLocal() {
		String remoteUrl = getRemoteUrl();
		return LOCAL.equals(remoteUrl) || HEADLESS.equals(remoteUrl);
	}

	/**
	 * Indica si la configuracion corresponde a un navegador local en modo headless.
	 */
	public static boolean isHeadless() {
		return HEADLESS.equals(getRemoteUrl());
	}

	/**
	 * Obtiene la url a probar a partir de la especificada en la configuracion y el puerto indicado como parametro:
	 * - Si no existe el fichero de propiedades, utiliza localhost como valor por defecto.
	 * - Si el valor del puerto indicado en el parametro mayor que cero, usa este valor independientemente del que se haya
	 * configurado en el fichero de propiedades (para permitir establecer random port en los tests)
	 */
	public static String getApplicationUrl(int port) {
		String host = getProperty(SELENIUM_PROPERTIES, "application.url", "http://localhost");
		String portstr = getProperty(SELENIUM_PROPERTIES, "application.port", "");
		portstr = port > 0 ? String.valueOf(port) : portstr; // override env specified set by parameter
		String url = host + ("".equals(portstr) ? "" : ":" + portstr);
		log.info("Application url: " + url);
		return url;
	}

	public static String getProperty(String propFileName, String propName, String defaultValue) {
		java.util.Properties prop = new java.util.Properties();
		File propFile = new File(propFileName);
		if (!propFile.exists())
			return defaultValue;

		try {
			prop.load(new FileInputStream(propFileName));
		} catch (IOException e) {
			throw new ApplicationException("Can't load properties file " + propFileName);
		}

		String propValue = prop.getProperty(propName.trim()).trim();
		if (propValue == null)
			throw new ApplicationException("Can't read property " + propName);
		return propValue.trim();
	}

}

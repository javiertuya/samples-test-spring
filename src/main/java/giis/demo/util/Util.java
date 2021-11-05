package giis.demo.util;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;

/**
 * Utilidades varias con metodos generales de serializacion, conversion a csv y conversion de fechas
 */
public class Util {
	private Util() {
	    throw new IllegalStateException("Utility class");
	}

	/**
	 * Convierte una lista de objetos a formato csv
	 * @param pojoList Lista de objetos a serializar
	 * @param fields campos de cada objeto a incluir en el csv
	 */
	public static String pojosToCsv(List<?> pojoList, String[] fields) {
		return pojosToCsv(pojoList,fields,false,",","","","");
	}
	/**
	 * Convierte una lista de objetos a formato csv con varios parametros para personalizar el aspecto
	 * @param pojoList Lista de objetos a serializar
	 * @param fields campos de cada objeto a incluir en el csv
	 * @param headers si es true incluye una primera fila con las cabeceras
	 * @param separator caracter que separa cada columna
	 * @param begin caracter a incluir al principio de cada linea
	 * @param end caracter a incluir al final de cada linea
	 * @param nullAs Texto que se incluira cuando el valor es null
	 * @return el string que representa la lista serializada en csv
	 */
	public static String pojosToCsv(List<?> pojoList, String[] fields, boolean headers, String separator, String begin, String end, String nullAs) {
		StringBuilder sb=new StringBuilder();
		if (headers) 
			addPojoLineToCsv(sb,null,fields,separator,begin,end,nullAs);
		for (int i=0; i<pojoList.size(); i++) {
			try {
				//utiliza Apache commons BeanUtils para obtener los atributos del objeto en un map
				Map<String, String> objectAsMap = BeanUtils.describe(pojoList.get(i));
				addPojoLineToCsv(sb,objectAsMap,fields,separator,begin,end,nullAs);
			} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
				throw new ApplicationException(e);
			}
		}
		return sb.toString();
	}
	private static void addPojoLineToCsv(StringBuilder sb, Map<String, String> objectAsMap, String[] fields, String separator, String begin, String end, String nullAs) {
		sb.append(begin);
		for (int j=0; j<fields.length; j++) {
			String value;
			if (objectAsMap==null) //nombre del campo si no hay map
				value = fields[j];
			else //valor del campo o el especificado para null
				value = objectAsMap.get(fields[j])==null ? nullAs : objectAsMap.get(fields[j]);
			sb.append((j==0 ? "" : separator) + value);
		}
		sb.append(end + "\n");
	}

	/**
	 * Convierte un array bidimensional de strings a csv (usado para comparaciones del ui con AssertJ)
	 */
	public static String arraysToCsv(String[][] arrays) {
		return arraysToCsv(arrays,null,",","","");
	}
	/**
	 * Convierte un array bidimensional de strings a csv permitiendo parametrizacion
	 * (usado para comparaciones del ui con AssertJ y JBehave)
	 */
	public static String arraysToCsv(String[][] arrays, String[] fields, String separator, String begin, String end) {
		StringBuilder sb=new StringBuilder();
		if (fields!=null)
			addArrayLineToCsv(sb,fields,separator,begin,end);
		for (int i=0; i<arrays.length; i++) 
			addArrayLineToCsv(sb,arrays[i],separator,begin,end);
		return sb.toString();
	}
	private static void addArrayLineToCsv(StringBuilder sb, String[] array, String separator, String begin, String end) {
		sb.append(begin);
		for (int j=0; j<array.length; j++)
			sb.append((j==0 ? "" : separator) + array[j]);
		sb.append(end);
		sb.append("\n");
	}
	
}

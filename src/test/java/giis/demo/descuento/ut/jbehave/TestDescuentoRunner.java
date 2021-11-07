package giis.demo.descuento.ut.jbehave;

import java.util.List;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.embedder.EmbedderControls;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.spring.SpringStepsFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import giis.demo.descuento.DescuentoApplication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

/**
 * Configuracion para ejecucion con jbehave de los escenarios escritos en gherkin determinando:
 * <br/>-la configuracion general a utilizar
 * <br/>-los archivos que contienen los escenarios y pasos en gherkin
 * <br/>-las clases java que implementan el mapeo de los pasos.
 * 
 * <br/>La ejecucion de los pasos se realiza en el entorno de Spring, por lo que a diferencia de
 * las configuraciones realizadas en el proyecto samples-test-java aqui se tienen que tener en cuenta
 * las siguientes consideraciones:
 * 
 * <br/>- Para que los pasos se puedan ejecutar en el contexto de Spring, se debe definir esta clase
 * con los mismos atributos que en el caso de tests unitarios ejecutados directamente desde junit.
 * En SpringBootTest se deben anyadir las clases que contienen el mapeo de pasos.
 * <br/>- El metodo InjectableStepsFactory que en pruebas sin spring instanciaba las clases que contienen
 * el mapeo de los pasos, debe cambiar puesto que estas clases deben instanciarse en el contexto de Spring. 
 * Para ello se utiliza SpringStepsFactory en vez de InstanceStepsFactory (no hace falta indicar las clases
 * porque busca automaticamente todas las que tengan alguna anotacion de jbehave).
 * <br/>- Con esto, se podrian ejecutar las historias desde Eclipse, 
 * pero no desde maven utilizando jbehave-maven-plugin.
 * La causa es que al ejecutarse run-stories-as-embeddables, esta clase obtiene un contexto de Spring nulo.
 * Para solucionarlo, se hereda de Embedder en vez de JUnitStories, y se implementa el test case run().
 */
@SpringBootTest(classes = {DescuentoApplication.class,DescuentoRepositorySteps.class})
@TestPropertySource(locations="classpath:application-test.properties")
@RunWith(SpringRunner.class)
public class TestDescuentoRunner extends Embedder{
	@Autowired 
	private ApplicationContext springContext; //para instanciar los beans en el entorno de Spring con InjectableStepsFactory
	//clase de utilidad con metodos generales para la configuracion
	private JBehaveConfig config=new JBehaveConfig(this.getClass(),true); 

	/**
	 * Ejecuta el test de todas las historias,
	 * se pueden crear test diferentes, revisando las historias que se ejecutaran en cada uno
	 * (obtenidas con storyPaths())
	 */
	@Test
	public void run() {
		List<String> stories = storyPaths();
		this.runStoriesAsPaths(stories);
	}
	/**
	 * Establece los parametros que irian el plugin jbehave-maven-plugin
	 * (permite continuar ejecutando historias cuando alguna falla)
	 */
	@Override
	public EmbedderControls embedderControls() {
		return config.getEmbedderControls();
	}
	/**
	 * Configuracion general por defecto, anyadiendo reports de surefire
	 */
	@Override
	public Configuration configuration() {
		return config.getConfiguration();
	}
	/**
	 * Asocia las clases java que implementan cada uno de los pasos de los escenarios
	 */
	@Override
	public InjectableStepsFactory stepsFactory() {
		//a traves del contexto se localizaran todos los mapeos de pasos que tengan anotaciones jbehave
		return new SpringStepsFactory(configuration(),springContext);
	}
	/**
	 * Asocia (localiza) los archivos con escenarios (*.story) que se encuentran en la misma carpeta que esta clase
	 * (antes de ejecutar los tests se copian desde la carpeta de fuentes a la carpeta de clases)
	 */
	protected List<String> storyPaths() {
		return config.getStoryPaths();
	}
}

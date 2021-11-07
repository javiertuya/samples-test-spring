package giis.demo.descuento.ut.jbehave;

import static org.jbehave.core.io.CodeLocations.codeLocationFromClass;
import static org.jbehave.core.reporters.Format.CONSOLE;
import static org.jbehave.core.reporters.Format.HTML;

import java.io.File;
import java.util.List;
import java.util.Locale;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.embedder.EmbedderControls;
import org.jbehave.core.io.CodeLocations;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.io.StoryFinder;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.reporters.SurefireReporter;

/**
 * Metodos comunes para la configuracion de JBehave (evita duplicar codigo de test ut/it)
 */
public class JBehaveConfig {
	private boolean unitTesting;
	private Class<?> embeddableClass;
	public JBehaveConfig(Class<?> thisClass, boolean isUnitTesting) {
		embeddableClass=thisClass;
		unitTesting=isUnitTesting;
	}
	/**
	 * Configuracion general por defecto, anyadiendo reports de surefire
	 */
	public Configuration getConfiguration() {
		//Define las opciones para incluir los resultados en los reports de surefire o failsafe
		Locale.setDefault(Locale.UK); //si no se pone causa excepcion con los valores decimales en los reports
		SurefireReporter.Options opt=new SurefireReporter.Options();
		if (unitTesting) {
			new File("target/surefire-reports/").mkdir(); //asegura que existe la carpeta si se ejecuta desde eclipse
			opt.useReportName("target/surefire-reports/TEST-jbehave-ut");
		} else {
			new File("target/failsafe-reports/").mkdir(); //asegura que existe la carpeta si se ejecuta desde eclipse
			opt.useReportName("target/failsafe-reports/TEST-jbehave-it");
		}
		SurefireReporter surefireReporter = new SurefireReporter(embeddableClass,opt);
		//devuelve la configuracion
		return new MostUsefulConfiguration()
				.useStoryLoader(new LoadFromClasspath(embeddableClass))
				.useStoryReporterBuilder(new StoryReporterBuilder()
						.withCodeLocation(CodeLocations.codeLocationFromClass(embeddableClass))
						.withSurefireReporter(surefireReporter)
						.withDefaultFormats()
						.withFormats(CONSOLE, HTML)
						.withFailureTrace(true)
						.withFailureTraceCompression(true));
	}
	/**
	 * Establece los parametros que irian el plugin jbehave-maven-plugin
	 * (permite continuar ejecutando historias cuando alguna falla)
	 */
	public EmbedderControls getEmbedderControls() {
		//los parametros que irian el plugin jbehave-maven-plugin
		return new EmbedderControls().doIgnoreFailureInStories(true).doIgnoreFailureInView(false);
	}
	/**
	 * Asocia (localiza) los archivos con escenarios (*.story) que se encuentran en la misma carpeta que esta clase
	 * (antes de ejecutar los tests se copian desde la carpeta de fuentes a la carpeta de clases)
	 */
	public List<String> getStoryPaths() {
		//las historias se buscan en los paquetes ut o it
		String paths = unitTesting ? "**/ut/**/*.story" : "**/it/**/*.story";
		return new StoryFinder().findPaths(codeLocationFromClass(embeddableClass), paths, "");
	}

}

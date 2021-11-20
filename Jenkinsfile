node ('slave-x1') {
	stage('Init') { 
		deleteDir() //comenzar con workspace limpio
		git branch: "develop", 
			credentialsId: "************************************", 
			url: "https://github.com/javiertuya/samples-test-spring.git"
	}
	stage('test') {
		echo "****** Build and test" 
		//Para selenoid:
		// la url del driver es usa el nombre del contaienr porque comparte la red con este slave
		// la url de la aplicacion debe ser la ip de este ejecutor
		//No configura browsers.json porque el servicio de selenoid esta siempre disponible
		sh "echo 'remote.web.driver.url=http://selenoid:4444/wd/hub' > samples-test-spring.properties"
        sh "echo \"application.url=http://`(hostname -i)`\" >> samples-test-spring.properties"
        
        //Ejecuta maven, evitando que falle el build si fallan los tests, 
        //luego los reports junit estableceran el estado inestable si procede
		sh "mvn clean verify -Dmaven.test.failure.ignore=true -U --no-transfer-progress"
	}
	stage('report') {
		echo "****** Publishing reports" 
        //reports junit y jacoco para jenkins
        try {
        	junit '**/target/surefire-reports/TEST-*.xml,**/target/failsafe-reports/TEST-*.xml'
        } catch (Exception) { }
        //publica estos reports tambien en su version html
        if (fileExists('target/site/surefire-report.html')) { //omite report si no existe para no genear un link invalido en jenkins
          publishHTML([allowMissing: true, alwaysLinkToLastBuild: true, keepAll: false, 
        	reportDir: "target/site", reportFiles: 'surefire-report.html', 
        	reportName: 'Surefire Report'])
        }
        if (fileExists('target/site/failsafe-report.html')) { //omite report si no existe para no genear un link invalido en jenkins
          publishHTML([allowMissing: true, alwaysLinkToLastBuild: true, keepAll: false, 
        	reportDir: "target/site", reportFiles: 'failsafe-report.html', 
        	reportName: 'Failsafe Report'])
		}
        if (fileExists('target/jbehave/view/reports.html')) { //omite report si no existe para no genear un link invalido en jenkins
          publishHTML([allowMissing: true, alwaysLinkToLastBuild: true, keepAll: false, 
        	reportDir: "target/jbehave/view", reportFiles: 'reports.html', 
        	reportName: 'JBehave Report'])
		}
		//report cobertura jacoco
        publishHTML([allowMissing: true, alwaysLinkToLastBuild: true, keepAll: false, 
        	reportDir: "target/site/jacoco", reportFiles: 'index.html', 
        	reportName: 'JaCoCo Report'])
       //reports estilo junit
        publishHTML([allowMissing: true, alwaysLinkToLastBuild: true, keepAll: false, 
        	reportDir: "target/site/junit-frames", reportFiles: 'index.html', 
        	reportName: 'JUnit Report (frames)'])
        publishHTML([allowMissing: true, alwaysLinkToLastBuild: true, keepAll: false, 
        	reportDir: "target/site/junit-noframes", reportFiles: 'junit-noframes.html', 
        	reportName: 'JUnit Report (noframes)'])

        //envia otros ficheros al archivo del job
        archiveArtifacts artifacts:'**/target/*.jar', allowEmptyArchive:true
        archiveArtifacts artifacts:'**/target/*.log', allowEmptyArchive:true
        archiveArtifacts artifacts:'**/target/*.html', allowEmptyArchive:true
        archiveArtifacts artifacts:'**/target/site/screenshot/*.png', allowEmptyArchive:true
    }
    stage ('dependency-check') {
		//No ejecuta en paralelo con sonar porque este report se incluira en los resultados de sonar
		lock("dependency-check") {
        	echo "****** run OWASP dependency check" 
        	def depCheckProg="/usr/share/dependency-check/bin/dependency-check.sh"
        	def depCheckParam="target/*-deploy.jar"
			sh "${depCheckProg} --scan ${depCheckParam} --out ./target --format HTML --format JSON"
        	publishHTML([allowMissing: true, alwaysLinkToLastBuild: true, keepAll: false, 
        		reportDir: "target", reportFiles: 'dependency-check-report.html', 
        		reportName: 'Dependency Check'])
        	archiveArtifacts artifacts:'target/dependency-check-report.html', allowEmptyArchive:true
		  }
	}
    stage ('sonarqube') {
		echo "****** SonarQube analysis" 
        def scannerHome = tool 'SonarQube Scanner Linux';
        def sonarParams="-Dsonar.projectKey=my:samples-test-spring"
        withSonarQubeEnv('songis') {
        	sh "${scannerHome}/bin/sonar-scanner ${sonarParams}"
        }
	}
	
}


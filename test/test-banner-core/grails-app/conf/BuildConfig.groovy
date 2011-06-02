/** *****************************************************************************
 � 2010 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */

grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir	= "target/test-reports"
//grails.project.war.file = "target/${appName}-${appVersion}.war"

grails.plugin.location.'banner-core' = "../.."

// When deploying a war it is important to exclude the Oracle database drivers.  Not doing so will
// result in the all-too-familiar exception:
// "Cannot cast object 'oracle.jdbc.driver.T4CConnection@6469adc7'... to class 'oracle.jdbc.OracleConnection'
grails.war.resources = { stagingDir ->
  delete( file:"${stagingDir}/WEB-INF/lib/ojdbc6.jar" )
}

grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits( "global" ) {
        // uncomment to disable ehcache
        // excludes 'ehcache'
    }

    log "error" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    repositories {

        mavenRepo "http://m038083.sungardhe.com:8081/nexus/content/repositories/releases/"
        mavenRepo "http://m038083.sungardhe.com:8081/nexus/content/repositories/snapshots/"
        mavenRepo "http://m038083.sungardhe.com:8081/nexus/content/repositories/thirdparty/"

        grailsPlugins()
        grailsHome()
        grailsCentral()
        mavenCentral()
        mavenRepo "http://repository.jboss.org/maven2/"
        mavenRepo "http://repository.codehaus.org"
    }

	plugins {
		compile 'com.sungardhe:banner-codenarc:0.1.3'
        compile 'com.sungardhe:spring-security-cas:1.0.2'
	}


    dependencies {
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.

        // Note: elvyx-1.0.24_beta.jar remains in the lib/ directory of the project as it is not available in a public repo due to licensing issues.
        build 'org.codehaus.groovy:http-builder:0.5.0',  // needed for FooClient, a script that interacts with the Foo resource
              'org.antlr:antlr:3.2',
              'com.thoughtworks.xstream:xstream:1.2.1',
              'javassist:javassist:3.8.0.GA',
              'com.oracle:ojdbc6:11.1.0.6'
              
        runtime "javax.servlet:jstl:1.1.2"
    }

}

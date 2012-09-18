/*******************************************************************************
Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
*******************************************************************************/

grails.project.class.dir        = "target/classes"
grails.project.test.class.dir   = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"

grails.plugin.location.'spring-security-cas' = "../spring_security_cas.git"
grails.plugin.location.'banner-codenarc'     = "../banner_codenarc.git"
grails.plugin.location.'i18n-core'="../i18n_core.git"

grails.project.dependency.resolution = {

    inherits( "global" ) {
    }

    log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'

    plugins {
    }

    distribution = {
    }

    repositories {
    if (System.properties['PROXY_SERVER_NAME']) {
        mavenRepo "${System.properties['PROXY_SERVER_NAME']}"
    } else
    {
        grailsPlugins()
        grailsHome()
        grailsCentral()

        mavenCentral()
        mavenRepo "http://repository.jboss.org/maven2/"
        mavenRepo "http://repository.codehaus.org"
	}
    }

    dependencies {
    }

}

/* ****************************************************************************
Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
*******************************************************************************/

grails.servlet.version = "2.5"
grails.project.class.dir        = "target/classes"
grails.project.test.class.dir   = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"

//grails.plugin.location.'spring-security-saml' = "../spring_security_saml.git"
grails.plugin.location.'banner-codenarc'      = "../banner_codenarc.git"
grails.plugin.location.'i18n-core'            = "../i18n_core.git"
grails.plugin.location.'grails-constraints'   = "../grails_constraints.git"


grails.project.dependency.resolver="maven"

grails.project.dependency.resolution = {

    inherits( "global" ) {

    }

    log "error" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'

    plugins {
        compile ":spring-security-core:2.0-RC5"
        compile ':resources:1.2.8'
        compile ':markdown:1.0.0.RC1'
		runtime ":webxml:1.4.1"
        compile ":functional-test:2.0.0"
    }

    distribution = {
    }

    repositories {
        if (System.properties['PROXY_SERVER_NAME']) {
            mavenRepo "${System.properties['PROXY_SERVER_NAME']}"
        }
        grailsCentral()
        mavenCentral()
        mavenRepo "https://code.lds.org/nexus/content/groups/main-repo"
        mavenRepo "http://repository.jboss.org/maven2/"
    }

    dependencies {
        compile( 'net.sourceforge.nekohtml:nekohtml:1.9.18') {
            excludes 'xml-apis', 'xerces'
        }
		compile "commons-dbcp:commons-dbcp:1.4"
        runtime "com.oracle:xdb6:11.2.0.4"
        test "com.oracle:ojdbc6:11.2.0.1.0"
        test 'org.easymock:easymock:3.2'

    }

}

// CodeNarc rulesets
codenarc.ruleSetFiles="rulesets/banner.groovy"
codenarc.reportName="target/CodeNarcReport.html"
codenarc.propertiesFile="grails-app/conf/codenarc.properties"

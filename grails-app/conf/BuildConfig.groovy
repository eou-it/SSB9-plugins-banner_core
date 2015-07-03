/* ****************************************************************************
Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
*******************************************************************************/

grails.servlet.version = "2.5"
grails.project.class.dir        = "target/classes"
grails.project.test.class.dir   = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"

grails.plugin.location.'spring-security-cas' = "../spring_security_cas.git"
grails.plugin.location.'spring-security-saml' = "../spring_security_saml.git"
grails.plugin.location.'banner-codenarc'     = "../banner_codenarc.git"
grails.plugin.location.'i18n-core'           = "../i18n_core.git"

grails.project.dependency.resolution = {

    inherits( "global" ) {
    }

    log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'

    plugins {
        runtime  ":hibernate:3.6.10.10"
        compile ":spring-security-core:1.2.7.3"
        compile ':resources:1.2.7'
        compile ':markdown:1.0.0.RC1'
		runtime ":webxml:1.4.1"
        compile ":functional-test:2.0.0"
        test ':code-coverage:2.0.3-2',
        {
            excludes 'xercesImpl'
        }
    }

    distribution = {
    }

    repositories {
        if (System.properties['PROXY_SERVER_NAME']) {
            mavenRepo "${System.properties['PROXY_SERVER_NAME']}"
        }

        flatDir name:'banner_core_repo', dirs:'../banner_core.git/lib'

        ebr()
            grailsPlugins()
            grailsHome()
            grailsCentral()
            mavenCentral()
            mavenRepo "http://repository.jboss.org/maven2/"
            mavenRepo "http://repository.codehaus.org"

    }

    dependencies {
		compile "commons-dbcp:commons-dbcp:1.4"
        test ":ojdbc6:11.2.0.1.0"
        test 'org.easymock:easymock:3.2'

    }

}

// CodeNarc rulesets
codenarc.ruleSetFiles="rulesets/banner.groovy"
codenarc.reportName="target/CodeNarcReport.html"
codenarc.propertiesFile="grails-app/conf/codenarc.properties"

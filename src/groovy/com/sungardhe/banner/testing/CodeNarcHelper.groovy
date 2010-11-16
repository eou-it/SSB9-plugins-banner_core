/** *****************************************************************************
 © 2010 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
package com.sungardhe.banner.testing

import grails.util.GrailsUtil

/**
 * This is a helper class to be invoked by projects who want to run code narc.  It is in a helper class and not a
 * GroovyTestCase because some of our projects run unit: (a good thing) and others don't.
 */
class CodeNarcHelper {


    private static final GROOVY_FILES = '**/*.groovy'


    void runCodeNarc() {

        def config = loadConfig()

        def ant = new AntBuilder()

        ant.taskdef(name: 'codenarc', classname: 'org.codenarc.ant.CodeNarcTask')

        ant.codenarc(ruleSetFiles: config.ruleSetFiles, maxPriority1Violations: 0) {

            configureIncludes( config ).each {
                fileset( dir: it ) {
                    include( name: GROOVY_FILES )
                }
            }

            report(type: 'text') {
                option(name: 'writeToStandardOut', value: true)
            }
        }
    }


    private ConfigObject loadConfig() {

        def classesDirPath = "grails-app/conf"

        def classLoader = Thread.currentThread().contextClassLoader
        classLoader.addURL(new File(classesDirPath).toURL())
        try {
            def className = "Config"
            return new ConfigSlurper(GrailsUtil.environment).parse(classLoader.loadClass(className)).codenarc
        }
        catch (ClassNotFoundException e) {
            return new ConfigObject()
        }
    }


    private boolean getConfigBoolean(config, String name) {
        def value = config[name]
        return value instanceof Boolean ? value : true
    }


    private List configureIncludes(config) {
        List includes = []

        if (getConfigBoolean(config, 'processSrcGroovy')) {
            includes << 'src/groovy'
        }

        if (getConfigBoolean(config, 'processControllers')) {
            includes << 'grails-app/controllers'
        }

        if (getConfigBoolean(config, 'processDomain')) {
            includes << 'grails-app/domain'
        }

        if (getConfigBoolean(config, 'processServices')) {
            includes << 'grails-app/services'
        }

        if (getConfigBoolean(config, 'processTaglib')) {
            includes << 'grails-app/taglib'
        }

        if (getConfigBoolean(config, 'processUtils')) {
            includes << 'grails-app/utils'
        }

        if (getConfigBoolean(config, 'processTestUnit')) {
            includes << 'test/unit'
        }

        if (getConfigBoolean(config, 'processTestIntegration')) {
            includes << 'test/integration'
        }

        for (includeDir in config.extraIncludeDirs) {
            includes << "$includeDir"
        }

        return includes
    }

}

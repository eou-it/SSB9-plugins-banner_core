/*********************************************************************************
 Copyright 2009-2011 SunGard Higher Education. All Rights Reserved.
 This copyrighted software contains confidential and proprietary information of 
 SunGard Higher Education and its subsidiaries. Any use of this software is limited 
 solely to SunGard Higher Education licensees, and is further subject to the terms 
 and conditions of one or more written license agreements between SunGard Higher 
 Education and the licensee in question. SunGard is either a registered trademark or
 trademark of SunGard Data Systems in the U.S.A. and/or other regions and/or countries.
 Banner and Luminis are either registered trademarks or trademarks of SunGard Higher 
 Education in the U.S.A. and/or other regions and/or countries.
 **********************************************************************************/
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

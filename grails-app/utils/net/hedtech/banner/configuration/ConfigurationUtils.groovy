/*******************************************************************************
Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
*******************************************************************************/ 
package net.hedtech.banner.configuration

import grails.util.Holders  as CH


/**
 * Common configuration utilities.
 */
abstract
class ConfigurationUtils {

    
    /**
     * Returns the Grails configuration object.
     */
    public static def getConfiguration() {
        if (CH && CH.config) {
            CH?.config
        } else {
            // Grails bug GRAILS-4687, and http://n4.nabble.com/Grails-Unit-Integration-Testing-apparent-Random-Failures-td1315936.html#a1315936
            // result in the configuration holder being null when running all, or all integration, tests. The holder is available
            // when running tests individually. To workaround this, we'll use the ConfigSlurper to read the configuration.
            new ConfigSlurper().parse( new File( 'grails-app/conf/Config.groovy' ).toURL() )
        }
    }
        
}

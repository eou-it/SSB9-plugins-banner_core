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
package net.hedtech.banner.configuration

import org.codehaus.groovy.grails.commons.ConfigurationHolder as CH


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

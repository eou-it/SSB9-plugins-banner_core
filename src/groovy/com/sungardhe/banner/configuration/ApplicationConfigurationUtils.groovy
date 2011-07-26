/** *****************************************************************************
 © 2011 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
package com.sungardhe.banner.configuration

import grails.util.GrailsUtil

import org.apache.log4j.Logger

import org.codehaus.groovy.grails.commons.ApplicationHolder as AH
import org.codehaus.groovy.grails.commons.ConfigurationHolder as CH
import org.codehaus.groovy.grails.commons.GrailsApplication


/**
 * Utilities for application configuration.
 */
abstract
class ApplicationConfigurationUtils {


    private static final Logger log = Logger.getLogger( "com.sungardhe.banner.configuration.ApplicationConfigurationUtils" )


    /**
     * Returns the release number that may be displayed to the user. 
     * The 'release number' is the grails metadata 'app.version' + buildNumber, 
     * where the buildNumber is a one-up number provided by a build number web service. 
     * Please see the 'scripts/BuildRelease.groovy' for details concerning assigning 
     * a build number.
     **/
    public static String getReleaseNumber() {
        def releaseNum = CH.config.application.build.version
        if (!(releaseNum instanceof String)) {
            releaseNum = AH.application.metadata[ 'app.version' ] + "-DEVELOPMENT"
        }
        releaseNum
    }


    /** 
     * Loads a configuration file, using the following search order.
     * 1. Load the configuration file if its location was specified on the command line using -DmyEnvName=myConfigLocation
     * 2. (If NOT Grails production env) Load the configuration file if it exists within the user's .grails directory (i.e., convenient for developers)
     * 3. Load the configuration file if its location was specified as a system environment variable
     * 4. Load from the classpath (e.g., load file from /WEB-INF/classes within the war file). The installer is used to copy configurations
     *    to this location, so that war files 'may' be self contained (yet can still be overriden using external configuration files)
     **/
    public static void addLocation( List locations, String propertyName, String fileName ) {
        try {
            def filePathName = getFilePath( System.getProperty( propertyName ) ) 

            if (GrailsUtil.environment != GrailsApplication.ENV_PRODUCTION) {
                filePathName = filePathName ?: getFilePath( "${System.getProperty( 'user.home' )}/.grails/${fileName}" )
                filePathName = filePathName ?: getFilePath( "${fileName}" ) 
                filePathName = filePathName ?: getFilePath( "grails-app/conf/${fileName}" ) 
            }
            
            filePathName = filePathName ?: getFilePath( System.getenv( propertyName ) )

            if (filePathName) {
                locations << "file:${filePathName}"        
            } 
            else {
                log.warn "Could not find external configuration file $fileName"
                def fileInClassPath = Thread.currentThread().getContextClassLoader().getResource( "$fileName" )?.toURI() 
                if (fileInClassPath) {
                    log.warn "...but found ($fileName) on the classpath (e.g., within the war)"
                    locations << "classpath:$fileName"
                }
            }
        } 
        catch (e) {
            log.warn "Caught exception while loading configuration files (depending on current grails target, this may be ok): ${e.message}"
        }
    }
    

    private static String getFilePath( filePath ) {
        if (filePath && new File( filePath ).exists()) {
            log.info "Including external configuration file: $filePath"
            "${filePath}"
        }
    }

}

/*******************************************************************************
Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
*******************************************************************************/ 
package net.hedtech.banner.configuration

import grails.util.GrailsUtil

import org.apache.log4j.Logger
import org.apache.commons.logging.LogFactory

import org.codehaus.groovy.grails.commons.ApplicationHolder as AH
import org.codehaus.groovy.grails.commons.ConfigurationHolder as CH
import org.codehaus.groovy.grails.commons.GrailsApplication


/**
 * Utilities for application configuration.
 */
class ApplicationConfigurationUtils {


    private static String releaseNum
    

    /**
     * Returns the release number that may be displayed to the user. 
     * The 'release number' is the grails metadata 'app.version' + buildNumber, 
     * where the buildNumber is a one-up number provided by a build number web service. 
     * Please see the 'scripts/BuildRelease.groovy' for details concerning assigning 
     * a build number.
     **/
    public static String getReleaseNumber() {
        if (!releaseNum) {
            def buildNum = CH.config.application.build.number
            if (!(buildNum instanceof String)) {
                buildNum = "DEVELOPMENT"
            }
            releaseNum = "${AH.application.metadata['app.version']}-${buildNum}"
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
            if (filePathName) LogFactory.getLog(this).info "Using configuration file specified by system property '$propertyName'"

            if (GrailsUtil.environment != GrailsApplication.ENV_PRODUCTION) {
                if (!filePathName) {
                    filePathName = getFilePath( "${System.getProperty( 'user.home' )}/.grails/${fileName}" )
                    if (filePathName) LogFactory.getLog(this).info "Using configuration file '\$HOME/.grails/$fileName'"
                }
                if (!filePathName) {
                    filePathName = getFilePath( "${fileName}" ) 
                    if (filePathName) LogFactory.getLog(this).info "Using configuration file '$fileName'"
                }
                if (!filePathName) {
                    filePathName = getFilePath( "grails-app/conf/$fileName" ) 
                    if (filePathName) LogFactory.getLog(this).info "Using configuration file 'grails-app/conf/$fileName'"
                }
            }
            
            if (!filePathName) {
                filePathName = getFilePath( System.getenv( propertyName ) )
                if (filePathName) LogFactory.getLog(this).info "Using configuration file specified by environment variable '$propertyName'"
            }

            if (filePathName) {
                locations << "file:${filePathName}"        
            } 
            else {
                def fileInClassPath = Thread.currentThread().getContextClassLoader().getResource( "$fileName" )?.toURI() 
                if (fileInClassPath) {
                    LogFactory.getLog(this).info "Using configuration file $fileName from the classpath (e.g., from within the war file)"
                    locations << "classpath:$fileName"
                }
            }
        } 
        catch (e) {
            LogFactory.getLog(this).warn "NOTICE: Caught exception while loading configuration files (depending on current grails target, this may be ok): ${e.message}"
        }
    }
    

    private static String getFilePath( filePath ) {
        if (filePath && new File( filePath ).exists()) {
            "${filePath}"
        }
    }

}

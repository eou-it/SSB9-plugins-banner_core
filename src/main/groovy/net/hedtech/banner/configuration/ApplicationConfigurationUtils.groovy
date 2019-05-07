/*******************************************************************************
Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
*******************************************************************************/ 
package net.hedtech.banner.configuration

import grails.util.Environment
import grails.util.Holders
import org.apache.commons.logging.LogFactory

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
            def buildNum = Holders.config.application.build.number
            if (!(buildNum instanceof String)) {
                buildNum = "DEVELOPMENT"
            }
            releaseNum = "${Holders.grailsApplication.metadata['app.version']}-${buildNum}"
        }
        releaseNum
    }

}

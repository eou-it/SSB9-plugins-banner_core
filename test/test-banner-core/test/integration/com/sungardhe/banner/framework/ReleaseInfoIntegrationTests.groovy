/** *****************************************************************************
 © 2010 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
package com.sungardhe.banner.framework

import com.sungardhe.banner.configuration.ApplicationConfigurationUtils as ReleaseVersionHolder

import grails.test.GrailsUnitTestCase

import org.codehaus.groovy.grails.commons.ApplicationHolder as AH
import org.codehaus.groovy.grails.commons.ConfigurationHolder as CH


/**
 * Tests that the release_info.groovy file, when it exists, can be easily accessed 
 * through the configuraiton holder.
 */
public class ReleaseInfoIntegrationTests extends GrailsUnitTestCase {


    void testBuildNumber() {
        def appVersion = AH.application.metadata[ 'app.version' ] 
        def releaseInfo = new File( "release_info.groovy" )
        if (releaseInfo.exists()) {
            // the release_info.groovy file exists, so we should expect to have a 'real' build number  
            assertTrue CH.config.application.build.version.contains( appVersion )
            assertNotNull CH.config.build.number.url
            assertTrue ReleaseVersionHolder.getReleaseNumber()?.contains( appVersion )
            assertFalse ReleaseVersionHolder.getReleaseNumber()?.contains( "-DEVELOPMENT" )
        } else {
            assertTrue ReleaseVersionHolder.getReleaseNumber() == "$appVersion-DEVELOPMENT"  
            // default behavior is an empty map if a config property does not exist
            assertTrue CH.config.application.build.version instanceof Map 
        }
    }

}
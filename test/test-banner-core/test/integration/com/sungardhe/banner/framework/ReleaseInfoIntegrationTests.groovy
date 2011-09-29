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
        def releasePropertiesFile = new File( "target/classes/release.properties" )
        if (releasePropertiesFile.exists()) {
            // the release.properties file exists, so we should expect to have a 'real' build number  
            assertTrue CH.config.application.version.contains( appVersion )
            assertTrue CH.config.application.build.number.isInteger()
            assertNotNull CH.config.build.number.url
            assertTrue ReleaseVersionHolder.getReleaseNumber()?.contains( appVersion )
            assertTrue ReleaseVersionHolder.getReleaseNumber()?.contains( "-${CH.config.application.build.number}" )
            assertFalse ReleaseVersionHolder.getReleaseNumber()?.contains( "-DEVELOPMENT" )
        } else {
            assertTrue ReleaseVersionHolder.getReleaseNumber() == "$appVersion-DEVELOPMENT"  
            // default behavior is an empty map if a config property does not exist
            assertTrue CH.config.application.build.version instanceof Map 
        }
    }

}
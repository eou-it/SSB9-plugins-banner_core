/*******************************************************************************
Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
*******************************************************************************/ 
package net.hedtech.banner.framework

import net.hedtech.banner.configuration.ApplicationConfigurationUtils as ReleaseVersionHolder

import grails.test.GrailsUnitTestCase

import org.codehaus.groovy.grails.commons.ApplicationHolder as AH
import org.codehaus.groovy.grails.commons.ConfigurationHolder as CH
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

/**
 * Tests that the release_info.groovy file, when it exists, can be easily accessed 
 * through the configuraiton holder.
 */
public class ReleaseInfoIntegrationTests extends BaseIntegrationTestCase {

    @Before
    public void setUp(){
        //  conn = dataSource.getSsbConnection()
        //  db = new Sql( conn )
        //  dataSetup()
        //super.setUp()
    }

    @After
    public void tearDown() {
        //super.tearDown()
    }



    @Test
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

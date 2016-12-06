/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.configuration

import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test


class ApplicationConfigurationUtilsIntegrationTests extends BaseIntegrationTestCase {

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
    }

    @After
    public void tearDown() {
        super.tearDown()
    }

    @Test
    public void getReleaseNumberSuccess() {
        def releaseNum = ApplicationConfigurationUtils.getReleaseNumber()
        println(releaseNum)
        assertNotNull(releaseNum)

    }

    @Test
    public void getFilePathFromSysProps() {
        System.properties.setProperty("BANNER_APP_CONFIG_LOCATION", "${System.getProperty('user.home')}/.grails/banner_configuration.groovy")
        assertNotNull(ApplicationConfigurationUtils.getFilePath(System.getProperty("BANNER_APP_CONFIG_LOCATION")))
    }

    @Test
    public void getFilePathFromUserDotGrailsDirectory() {
        def filePath = "${System.getProperty('user.home')}/.grails/banner_configuration.groovy"
        assertNotNull(ApplicationConfigurationUtils.getFilePath(filePath))
    }

    @Ignore
    @Test
    public void getFilePathFromEnvVariable() {
        def filePath = System.getenv("BANNER_APP_CONFIG")
        assertNotNull(ApplicationConfigurationUtils.getFilePath(filePath))
    }
}


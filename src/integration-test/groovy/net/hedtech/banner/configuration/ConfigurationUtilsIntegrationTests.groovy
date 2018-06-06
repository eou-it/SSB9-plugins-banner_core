/**
 * Copyright 2016 Ellucian Company L.P. and its affiliates.
 */
package net.hedtech.banner.configuration

import net.hedtech.banner.testing.BaseIntegrationTestCase
import grails.util.Holders  as CH
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * To Test ConfiguationUtils class
 */
class ConfigurationUtilsIntegrationTests extends BaseIntegrationTestCase {


    @Before
    public void setUp() {
        formContext = ['GUAGMNU'] // Since we are not testing a controller, we need to explicitly set this
        super.setUp()
    }


    @After
    public void tearDown() {
        super.tearDown()
    }



    @Test
    void configurationExists(){
        def outcome = ConfigurationUtils.getConfiguration()
        assertNotNull(outcome);
    }

    @Test
    void configurationIsNull(){
        def oldConfigCH = CH.config.clone()
        CH.config = null
        try{
            def outcome = ConfigurationUtils.getConfiguration()
            assertNotNull(outcome)
        }
        finally {
            CH.config = oldConfigCH
        }
    }
}

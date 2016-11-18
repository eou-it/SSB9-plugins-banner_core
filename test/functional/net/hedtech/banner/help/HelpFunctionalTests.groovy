/*******************************************************************************
Copyright 2009-2015 Ellucian Company L.P. and its affiliates.
*******************************************************************************/
package net.hedtech.banner.help

import grails.util.Holders
import net.hedtech.banner.security.FormContext
import net.hedtech.banner.testing.BaseFunctionalTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Functional tests verifying the On-Line Help Controller.
 */
class HelpFunctionalTests extends BaseFunctionalTestCase {

    @Before
    public void setUp(){
        formContext = ['STVCOLL']
        super.setUp()
        grailsApplication.config.banner.sso.authenticationProvider = "default"
    }

    @After
    public void tearDown() {
        super.tearDown()
    }
    // -------------------------------- Test JSON Representations ---------------------------------

    @Test
    void testList_JSON() {

        login()

        def pageSize = 5
        get("/help/url")

        assertStatus 200
        assertEquals 'application/json', page?.webResponse?.contentType

        def data = new groovy.json.JsonSlurper().parseText(page.webResponse.contentAsString)

        assertNotNull "On-Line url is not configured", data.help.url

    }
}

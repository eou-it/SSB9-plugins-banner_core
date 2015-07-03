/*******************************************************************************
Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
*******************************************************************************/
package net.hedtech.banner.help

import net.hedtech.banner.SpringContextUtils
import net.hedtech.banner.security.FormContext
import net.hedtech.banner.testing.BaseFunctionalTestCase

import grails.converters.JSON
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Functional tests verifying the On-Line Help Controller.
 */
class HelpFunctionalTests extends BaseFunctionalTestCase {

    @Before
    public void setUp(){
        SpringContextUtils.grailsApplication.config.banner.sso.authenticationProvider = "default"
        formContext = ['STVCOLL']
        super.setUp()
    }

    @After
    public void tearDown() {
        FormContext.clear()
    }
    // -------------------------------- Test JSON Representations ---------------------------------

    @Test
    void testList_JSON() {

        login()

        def pageSize = 5
        get("/help/url")

        assertStatus 200
        assertEquals 'application/json', page?.webResponse?.contentType

        def stringContent = page?.webResponse?.contentAsString
        def data = JSON.parse(stringContent)

        assertNotNull "On-Line url is not configured", data.help.url

    }
}

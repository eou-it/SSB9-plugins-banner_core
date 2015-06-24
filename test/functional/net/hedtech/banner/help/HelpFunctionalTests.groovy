/*******************************************************************************
Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
*******************************************************************************/
package net.hedtech.banner.help

import net.hedtech.banner.testing.BaseFunctionalTestCase

import grails.converters.JSON

/**
 * Functional tests verifying the On-Line Help Controller.
 */
class HelpFunctionalTests extends BaseFunctionalTestCase {


    protected void setUp() {
        formContext = ['STVCOLL']
        super.setUp()
    }

    // -------------------------------- Test JSON Representations ---------------------------------

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

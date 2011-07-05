/** *****************************************************************************

 © 2011 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
package com.sungardhe.banner.help

import com.sungardhe.banner.testing.BaseFunctionalTestCase

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
        assertEquals 'text/json', page?.webResponse?.contentType

        def stringContent = page?.webResponse?.contentAsString
        def data = JSON.parse(stringContent)

        assertNotNull "On-Line url is not configured", data.help.url

    }
}

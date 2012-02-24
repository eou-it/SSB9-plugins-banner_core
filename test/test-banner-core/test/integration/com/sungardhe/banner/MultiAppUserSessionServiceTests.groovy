package com.sungardhe.banner

import grails.test.*
import com.sungardhe.banner.testing.BaseIntegrationTestCase

class CrossAppSharedInfoServiceTests extends BaseIntegrationTestCase {

    def crossAppSharedInfoService


    protected void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testFind() {
        def userInfo = crossAppSharedInfoService.findByUserName ('test')
//        assertNotNull "User info does not exist", userInfo


    }
}

/*******************************************************************************
 Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */

package net.hedtech.banner.web

import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin

@TestMixin(GrailsUnitTestMixin)
class SsbURLRequestTests {

    private static final String PATH_ENDS_WITH_CONTROLLER_NAME = "/ssb/registration"
    private static final String PATH_ENDS_WITH_ACTION_NAME = "/ssb/registration/save"
    private static final String PATH_WITHOUT_SSB_URL = "/securityQA/save"
    private static final String EMPTY_PATH = ""
    private static final String CONTROLLER_NAME = "registration"

    void testControllerNameFromPathEndsWithControllerName() {
        SsbURLRequest ssbURLRequest = new SsbURLRequest()
        String controllerName = ssbURLRequest.getControllerNameFromPath(PATH_ENDS_WITH_CONTROLLER_NAME)
        assertEquals(CONTROLLER_NAME, controllerName)
    }

    void testControllerNameFromPathEndsWithActionName() {
        SsbURLRequest ssbURLRequest = new SsbURLRequest()
        String controllerName = ssbURLRequest.getControllerNameFromPath(PATH_ENDS_WITH_ACTION_NAME)
        assertEquals(CONTROLLER_NAME, controllerName)
    }

    void testControllerNameFromPathWithoutSsbUrl() {
        SsbURLRequest ssbURLRequest = new SsbURLRequest()
        String controllerName = ssbURLRequest.getControllerNameFromPath(PATH_WITHOUT_SSB_URL)
        assertEquals(PATH_WITHOUT_SSB_URL, controllerName)
    }

    void testControllerNameWithEmptyPath() {
        SsbURLRequest ssbURLRequest = new SsbURLRequest()
        String controllerName = ssbURLRequest.getControllerNameFromPath(EMPTY_PATH)
        assertEquals(EMPTY_PATH, controllerName)
    }
}

/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

import grails.util.Holders
import net.hedtech.banner.controllers.ControllerUtils
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Test cases for LogoutController
 */
class LogoutControllerIntegrationTests extends BaseIntegrationTestCase {

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        controller = new LogoutController()
    }

    @After
    public void tearDown() {
        super.tearDown()
    }

    @Test
    public void testIndex() {
        controller.index()
        assertEquals 302, controller.response.status
        assertEquals(controller.response.redirectedUrl, ControllerUtils.buildLogoutRedirectURI())
    }

    @Test
    public void testTimeout() {
        controller.request.addHeader(controller.HTTP_REQUEST_REFERER_STRING, '/test/' + controller.LOGIN_AUTH_ACTION_URI)
        controller.timeout()
        assertEquals 200, controller.response.status
        assertEquals(controller.response.forwardedUrl, '/login')
    }

    @Test
    public void testTimoutWithoutReferer() {
        def sessionBackup
        try {
            controller.request.addHeader(controller.HTTP_REQUEST_REFERER_STRING, '/test/')
            sessionBackup = controller.session
            controller.timeout()
            assertEquals 302, controller.response.status
            assertEquals(controller.response.redirectedUrl, controller.createLink([action: controller.ACTION_TIMEOUT_PAGE, absolute: true]))
        } finally {
            controller.request.setSession(sessionBackup)
        }
    }

    @Test
    public void testTimoutPage() {
        controller.timeoutPage()
        assertEquals 200, controller.response.status
        controller.response.setContentType('text/html')
        assertEquals(controller.modelAndView.viewName, '/logout/' + controller.VIEW_TIMEOUT)
        assertEquals(controller.modelAndView.model.uri, ControllerUtils.buildLogoutRedirectURI())
    }

    @Test
    public void testLogoutPage() {
        controller.logoutPage()
        assertEquals 200, controller.response.status
        assertEquals(controller.modelAndView.viewName, '/logout/' + controller.VIEW_LOGOUT_PAGE)
    }

    @Test
    public void testIndexWithSaml() {
        def samlBackup = Holders?.config.banner.sso.authenticationProvider
        try {
            Holders?.config.banner.sso.authenticationProvider = 'saml'
            controller.index()
            assertTrue(controller.request.isRequestedSessionIdValid())
            assertEquals(controller.response.redirectedUrl, ControllerUtils.buildLogoutRedirectURI())
        } finally {
            Holders?.config.banner.sso.authenticationProvider = samlBackup
        }
    }

    @Test
    public void testCustomLogout() {
        controller.customLogout()
        assertEquals 200, controller.response.status
        assertEquals(controller.modelAndView.viewName, '/logout/' + controller.VIEW_CUSTOM_LOGOUT)
        assertEquals(controller.modelAndView.model.uri, ControllerUtils.getHomePageURL())
        assertTrue(controller.modelAndView.model.show)
    }

    @Test
    public void testCustomLogoutWithParamError() {
        controller.request.setParameter('error', 'testError')
        controller.customLogout()
        assertEquals 200, controller.response.status
        assertEquals(controller.modelAndView.model.uri, ControllerUtils.getHomePageURL())
        assertFalse(controller.modelAndView.model.show)
    }

    @Test
    public void testCustomLogoutWithCASEnabled() {
        def casBackup = Holders?.config.banner.sso.authenticationProvider
        try {
            Holders?.config.banner.sso.authenticationProvider = 'cas'
            controller.customLogout()
            assertTrue(controller.request.isRequestedSessionIdValid())
            assertEquals(controller.modelAndView.model.uri, ControllerUtils.getHomePageURL())
            assertFalse(controller.modelAndView.model.show)
            assertEquals(controller.modelAndView.model.logoutUri, ControllerUtils.getAfterLogoutRedirectURI())
        } finally {
            Holders?.config.banner.sso.authenticationProvider = casBackup
        }
    }
}

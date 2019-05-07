/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/


import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import grails.util.Holders
import net.hedtech.banner.controllers.ControllerUtils
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Test cases for LogoutController
 */
@Integration
@Rollback
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

import grails.testing.web.controllers.ControllerUnitTest
import grails.util.Holders
import net.hedtech.banner.controllers.ControllerUtils
import spock.lang.Specification

import org.junit.Assert

class LogoutControllerSpec extends Specification implements ControllerUnitTest<LogoutController> {

/*
    Below test cases are migrated from
    LogoutControllerIntegrationTests.groovy class
*/
    void "Test index"() {
        when: 'The index action is executed'
            controller.index()
        then:
            Assert.assertEquals (302, controller.response.status)
            Assert.assertEquals (controller.response.redirectedUrl, ControllerUtils.buildLogoutRedirectURI())
    }

    void testIndexWithSaml() {
        setup:
            def samlBackup = Holders?.config.banner.sso.authenticationProvider
        when: 'The test index with SAML is executed'
            controller.index()
            Holders?.config.banner.sso.authenticationProvider == 'saml'
        then:
            Assert.assertTrue(controller.request.isRequestedSessionIdValid())
            Assert.assertEquals(controller.response.redirectedUrl, ControllerUtils.buildLogoutRedirectURI())
        cleanup:
            Holders?.config.banner.sso.authenticationProvider == samlBackup
    }

    void testTimeout() {
        when: 'Test timeout'
            controller.request.addHeader(controller.HTTP_REQUEST_REFERER_STRING, '/test/' + controller.LOGIN_AUTH_ACTION_URI)
            controller.timeout()
        then:
            Assert.assertEquals (200, controller.response.status)
            Assert.assertEquals(controller.response.forwardedUrl, '/login')
    }

    void testTimoutWithoutReferer() {
        setup:
            def sessionBackup
            controller.request.addHeader(controller.HTTP_REQUEST_REFERER_STRING, '/test/')
            sessionBackup = controller.session
        when: 'Test timeout without referer'
            controller.timeout()
        then:
            Assert.assertEquals (302, controller.response.status)
            Assert.assertEquals(controller.response.redirectedUrl, controller.createLink([uri: '/ssb/logout/timeoutPage', action: controller.ACTION_TIMEOUT_PAGE, absolute: true]))
        cleanup:
            controller.request.setSession(sessionBackup)
    }

}

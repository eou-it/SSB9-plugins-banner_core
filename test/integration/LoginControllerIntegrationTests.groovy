/*******************************************************************************
 Copyright 2016-2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

import grails.util.Holders
import groovy.sql.Sql
import net.hedtech.banner.exceptions.AuthorizationException
import net.hedtech.banner.i18n.MessageHelper
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.security.authentication.*
import org.springframework.security.core.context.SecurityContextHolder as SCH
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.web.context.request.RequestContextHolder

class LoginControllerIntegrationTests extends BaseIntegrationTestCase {

    def controller

    def msg

    def selfServiceBannerAuthenticationProvider

    private static final String PERSON_HOSWEB001 = 'HOSWEB001'

    private static final def HOSWEB001_PWD = 111111


    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        controller = new LoginController()
        RequestContextHolder?.currentRequestAttributes()?.request?.session?.setAttribute("mep", "BANNER")
        Holders.config.ssbEnabled = true
        Holders.config.ssbOracleUsersProxied = false

    }


    @After
    public void tearDown() {
        RequestContextHolder?.currentRequestAttributes()?.request?.session?.removeAttribute("mep")
        super.tearDown()
        Holders.config.ssbEnabled = false
        Holders.config.ssbOracleUsersProxied = false
    }


    @Test
    void testGetMessageForAccountExpired() {
        AccountExpiredException ae = new AccountExpiredException('')
        msg = controller.getMessageFor(ae)
        assertEquals([:], msg)
    }


    @Test
    void testGetMessageForCredentialsExpired() {
        CredentialsExpiredException ce = new CredentialsExpiredException('');
        msg = controller.getMessageFor(ce)
        assertEquals(MessageHelper.message("net.hedtech.banner.errors.login.expired"), msg)
    }


    @Test
    void testGetMessageForDisabled() {
        DisabledException de = new DisabledException('');
        msg = controller.getMessageFor(de)
        assertEquals(MessageHelper.message("net.hedtech.banner.errors.login.disabled"), msg)
    }


    @Test
    void testGetMessageForLocked() {
        LockedException le = new LockedException('');
        msg = controller.getMessageFor(le)
        assertEquals(MessageHelper.message("net.hedtech.banner.errors.login.locked"), msg)
    }


    @Test
    void testGetMessageForAuthorization() {
        AuthorizationException ate = new AuthorizationException('');
        msg = controller.getMessageFor(ate)
        assertEquals(MessageHelper.message("net.hedtech.banner.access.denied.message"), msg)
    }


    @Test
    void testGetMessageForBadCredentials() {
        BadCredentialsException be = new BadCredentialsException('');
        msg = controller.getMessageFor(be)
        assertEquals(MessageHelper.message("net.hedtech.banner.errors.login.fail"), msg)
    }


    @Test
    void testGetMessageForUsernameNotFound() {
        UsernameNotFoundException ue = new UsernameNotFoundException('');
        msg = controller.getMessageFor(ue)
        assertEquals(MessageHelper.message("net.hedtech.banner.errors.login.credentialnotfound"), msg)
    }


    @Test
    void testGetMessageForOtherException() {
        Exception e = new Exception('');
        msg = controller.getMessageFor(e)
        assertEquals(MessageHelper.message("net.hedtech.banner.errors.login.fail"), msg)
    }


    @Test
    void testGetMessageForNoException() {
        msg = controller.getMessageFor()
        assertEquals("", msg)
    }


    @Test
    void testBuildLogoutSuccess() {
        def mep = RequestContextHolder?.currentRequestAttributes()?.request?.session?.getAttribute("mep")
        def expectedUri = "/logout/customLogout?error=true&mepCode=BANNER"
        def actualUri = controller.buildLogout()
        assertEquals(expectedUri, actualUri)
    }


    @Test
    void testBuildLogoutFailure() {
        RequestContextHolder?.currentRequestAttributes()?.request?.session?.removeAttribute("mep")
        def expectedUri = "/logout/customLogout?error=true"
        def actualUri = controller.buildLogout()
        assertEquals(expectedUri, actualUri)
    }


    @Test
    void testIndexSuccess() {
        def auth = selfServiceBannerAuthenticationProvider.authenticate(new UsernamePasswordAuthenticationToken(PERSON_HOSWEB001, HOSWEB001_PWD))
        controller.index()
        assertEquals(302, controller.response.status)
    }


    @Test
    void testIndexFail() {
        SCH.context.authentication = null
        controller.index()
        assertEquals(302, controller.response.status)
        assertEquals "/login/auth", controller.response.redirectedUrl
    }


    @Test
    void testAuthSuccess() {
        SCH.context.authentication = null
        controller.auth()
        assertEquals(200, controller.response.status)
    }


    @Test
    void testAuthSuccessWithLoggedIn() {
        def auth = selfServiceBannerAuthenticationProvider.authenticate(new UsernamePasswordAuthenticationToken(PERSON_HOSWEB001, HOSWEB001_PWD))
        controller.auth()
        assertEquals(302, controller.response.status)
        assertEquals('/', controller.response.redirectedUrl)
    }


    @Test
    void testAuthAjax() {
        SCH.context.authentication = null
        controller.authAjax()
        assertEquals(200, controller.response.status)
    }


    @Test
    void testAuthAjaxWithLoggedIn() {
        def auth = selfServiceBannerAuthenticationProvider.authenticate(new UsernamePasswordAuthenticationToken(PERSON_HOSWEB001, HOSWEB001_PWD))
        controller.authAjax()
        assertEquals(302, controller.response.status)
    }


    @Test
    void testDenied() {
        SCH.context.authentication = null
        controller.denied()
        assertEquals(200, controller.response.status)
    }


    @Test
    void testDeniedWithLoggedIn() {
        def auth = selfServiceBannerAuthenticationProvider.authenticate(new UsernamePasswordAuthenticationToken(PERSON_HOSWEB001, HOSWEB001_PWD))
        controller.denied()
        assertEquals(200, controller.response.status)
    }


    @Test
    void testFull() {
        SCH.context.authentication = null
        controller.full()
        assertEquals(200, controller.response.status)
    }


    @Test
    void testAjaxSuccess() {
        def auth = selfServiceBannerAuthenticationProvider.authenticate(new UsernamePasswordAuthenticationToken(PERSON_HOSWEB001, HOSWEB001_PWD))
        controller.ajaxSuccess()
        assertEquals(200, controller.response.status)
    }


    @Test
    void testAjaxDenied() {
        SCH.context.authentication = null
        controller.ajaxDenied()
        assertEquals(200, controller.response.status)
    }


    @Test
    void testError() {
        SCH.context.authentication = null
        controller.error()
        assertEquals(200, controller.response.status)
    }


    @Test
    void testForgotPassword() {
        controller.request.setParameter("j_username", "TEST_USER")
        controller.forgotpassword()
        assertEquals(200, controller.response.status)
    }


    @Test
    void testForgotPasswordUserNameEmpty() {
        controller.request.setParameter("j_username", "  ")
        controller.forgotpassword()
        assertEquals(200, controller.response.status)
    }


    @Test
    void testForgotPasswordNoUser() {
        controller.forgotpassword()
        assertEquals(200, controller.response.status)
    }


    @Test
    void testAuthFail() {
        controller.authfail()
        assertEquals(200, controller.response.status)
    }


    @Test
    void testAuthFailElse() {
        controller.request.setParameter("ajax", "true")
        controller.authfail()
        assertEquals(200, controller.response.status)
    }
 }

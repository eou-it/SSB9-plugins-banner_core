/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

import grails.util.Holders
import groovy.sql.Sql
import net.hedtech.banner.exceptions.AuthorizationException
import net.hedtech.banner.i18n.MessageHelper
import net.hedtech.banner.security.SelfServiceBannerAuthenticationProvider
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.security.authentication.*
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder as SCH
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.web.context.request.RequestContextHolder

class LoginControllerIntegrationTests extends BaseIntegrationTestCase {

    def controller
    def msg
    private SelfServiceBannerAuthenticationProvider provider
    def conn
    Sql sqlObj
    def dataSource
    def testUser
    public static final String PERSON_HOSWEB002 = 'HOSWEB002'
    def auth


    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        controller = new LoginController()
        RequestContextHolder?.currentRequestAttributes()?.request?.session?.setAttribute("mep", "BANNER")
        Holders.config.ssbEnabled = true
        Holders?.config.ssbOracleUsersProxied = false
        conn = dataSource.getSsbConnection()
        sqlObj = new Sql(conn)
        provider = Holders.applicationContext.getBean("selfServiceBannerAuthenticationProvider")
        testUser = existingUser(PERSON_HOSWEB002, 123456)
        enableUser(sqlObj, testUser.pidm)

    }

    @After
    public void tearDown() {
        RequestContextHolder?.currentRequestAttributes()?.request?.session?.removeAttribute("mep")
        super.tearDown()
        sqlObj.close()
        conn.close()
        Holders.config.ssbEnabled = false
        Holders?.config.ssbOracleUsersProxied = false
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
        auth = provider.authenticate(new TestAuthenticationRequest(testUser))
        controller.index()
        assertEquals(302, controller.response.status)
    }

    @Test
    void testIndexFail() {
        SCH.context.authentication = null
        controller.index()
        assertEquals(302, controller.response.status)
    }

    @Test
    void testAuthSuccess() {
        SCH.context.authentication = null
        controller.auth()
        assertEquals(200, controller.response.status)
    }

    @Test
    void testAuthSuccessWithLoggedIn() {
        auth = provider.authenticate(new TestAuthenticationRequest(testUser))
        controller.auth()
        assertEquals(302, controller.response.status)
    }

    @Test
    void testAuthAjax() {
        SCH.context.authentication = null
        controller.authAjax()
        assertEquals(200, controller.response.status)
    }

    @Test
    void testAuthAjaxWithLoggedIn() {
        auth = provider.authenticate(new TestAuthenticationRequest(testUser))
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
        auth = provider.authenticate(new TestAuthenticationRequest(testUser))
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
        auth = provider.authenticate(new TestAuthenticationRequest(testUser))
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


    private def existingUser(userId, newPin) {
        def existingUser = [name: userId]

        def testAuthenticationRequest = new TestAuthenticationRequest(existingUser)
        existingUser['pidm'] = provider.getPidm(testAuthenticationRequest, sqlObj)
        sqlObj.commit()
        sqlObj.call("{call gb_third_party_access.p_update(p_pidm=>${existingUser.pidm}, p_pin=>${newPin})}")
        sqlObj.commit()
        existingUser.pin = newPin
        return existingUser
    }

    private void enableUser(Sql db, pidm) {
        db.executeUpdate("update gobtpac set gobtpac_pin_disabled_ind='N' where gobtpac_pidm=$pidm")
        db.commit()
    }
}

class TestAuthenticationRequest implements Authentication {

    def user

    public TestAuthenticationRequest(user) {
        this.user = user
    }

    public Collection getAuthorities() { [] }

    public Object getCredentials() { user.pin }

    public Object getDetails() { user }

    public Object getPrincipal() { user }

    public boolean isAuthenticated() { false }

    public void setAuthenticated(boolean b) {}

    public String getName() { user.name }

    public Object getPidm() { user.pidm }

    public Object getOracleUserName() { user.oracleUserName }
}

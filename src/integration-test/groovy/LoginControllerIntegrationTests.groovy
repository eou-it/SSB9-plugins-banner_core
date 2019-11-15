/*******************************************************************************
 Copyright 2016-2019 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import grails.util.Holders
import groovy.sql.Sql
import net.hedtech.banner.exceptions.AuthorizationException
import net.hedtech.banner.i18n.MessageHelper
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.*
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder as SCH
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.web.context.request.RequestContextHolder

@Integration
@Rollback
class LoginControllerIntegrationTests extends BaseIntegrationTestCase{

    //@Autowired
    //LoginController controller

    def msg
    def selfServiceBannerAuthenticationProvider
    def authenticationTrustResolver
    def springSecurityService

    private static final String PERSON_HOSWEB001 = 'HOSWEB001'

    private static final def HOSWEB001_PWD = 111111

    def testUser
    def dataSource
    def conn
    Sql db
    def grailsApplication

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        controller = new LoginController()
        super.setUp()
        controller.springSecurityService = springSecurityService
        controller.authenticationTrustResolver = authenticationTrustResolver
        RequestContextHolder?.currentRequestAttributes()?.request?.session?.setAttribute("mep", "BANNER")
        Holders.config.ssbEnabled = true
        Holders.config.ssbOracleUsersProxied = false
        conn = dataSource.getSsbConnection()
        conn.setAutoCommit(false)
        db = new Sql(conn)
        testUser = existingUser(PERSON_HOSWEB001, HOSWEB001_PWD)
        enableUser (db, testUser.pidm)
    }


    @After
    public void tearDown() {
        RequestContextHolder?.currentRequestAttributes()?.request?.session?.removeAttribute("mep")
        super.tearDown()
        Holders.config.ssbEnabled = false
        Holders.config.ssbOracleUsersProxied = false
        db.close()
        conn.close()
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
        controller.request.setParameter("username", "TEST_USER")
        controller.forgotpassword()
        assertEquals(200, controller.response.status)
    }


    @Test
    void testForgotPasswordUserNameEmpty() {
        controller.request.setParameter("username", "  ")
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
    private void enableUser(Sql db, pidm) {
        db.executeUpdate("update gobtpac set gobtpac_pin_disabled_ind='N' where gobtpac_pidm=$pidm")
        db.commit()
    }
    private def existingUser (userId, newPin) {
        def existingUser = [ name: userId]

        def testAuthenticationRequest = new TestAuthenticationRequest(existingUser)
        existingUser['pidm'] = selfServiceBannerAuthenticationProvider.getPidm(testAuthenticationRequest, db )
        db.commit()
        db.call ("{call gb_third_party_access.p_update(p_pidm=>${existingUser.pidm}, p_pin=>${newPin})}")
        db.commit()
        existingUser.pin = newPin
        return existingUser
    }
}
class TestAuthenticationRequest implements Authentication {

    def user

    public TestAuthenticationRequest( user ) {
        this.user = user
    }

    public Collection getAuthorities() { [] }
    public Object getCredentials() { user.pin }
    public Object getDetails() { user }
    public Object getPrincipal() { user }
    public boolean isAuthenticated() { false }
    public void setAuthenticated( boolean b ) { }
    public String getName() { user.name }
    public Object getPidm() { user.pidm }
    public Object getOracleUserName() { user.oracleUserName }
}

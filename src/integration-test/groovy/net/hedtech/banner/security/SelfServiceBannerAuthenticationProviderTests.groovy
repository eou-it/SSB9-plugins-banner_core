/*******************************************************************************
Copyright 2009-2018 Ellucian Company L.P. and its affiliates.
*******************************************************************************/
package net.hedtech.banner.security

import grails.gorm.transactions.Rollback
import grails.spring.BeanBuilder
import grails.testing.mixin.integration.Integration
import grails.util.Holders
import groovy.sql.Sql
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.apache.commons.dbcp.BasicDataSource
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.context.ApplicationContext
import org.springframework.security.authentication.CredentialsExpiredException
import org.springframework.security.authentication.DisabledException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.web.context.request.RequestContextHolder
import static groovy.test.GroovyAssert.shouldFail




/**
 * Integration test for the self service Banner authentication provider.
 **/
@Integration
@Rollback
class SelfServiceBannerAuthenticationProviderTests extends BaseIntegrationTestCase{


    public static final String PERSON_HOSWEB002 = 'HOSWEB002'
    private SelfServiceBannerAuthenticationProvider provider
    def conn
    Sql sqlObj
    def testUser
    def usage
    public final String LFMI= "LFMI"
    def dataSource

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        Holders.config.ssbEnabled = true
        Holders?.config.ssbOracleUsersProxied = false
        conn = dataSource.getSsbConnection()
        sqlObj = new Sql( conn )
        provider = Holders.applicationContext.getBean("selfServiceBannerAuthenticationProvider")
        testUser = existingUser(PERSON_HOSWEB002, 123456)
        enableUser (sqlObj, testUser.pidm)
    }

    @After
    public void tearDown() {
        super.tearDown()
        sqlObj.close()
        conn.close()
        Holders.config.ssbEnabled = false
        Holders?.config.ssbOracleUsersProxied = false
    }

    @Test
    void testGetPidm() {
        def pidm = provider.getPidm( new TestAuthenticationRequest( testUser ), sqlObj )
        assertEquals testUser.pidm, pidm
    }

    @Test
    void testSsbAuthentication() {

        Holders.config.guestAuthenticationEnabled= true

        def session = RequestContextHolder.currentRequestAttributes().getSession()
        session.setMaxInactiveInterval(1800)

        def auth = provider.authenticate( new TestAuthenticationRequest( testUser ) )
        assertTrue    auth.isAuthenticated()
        assertEquals  auth.name, testUser.name as String
        assertTrue    auth.details.credentialsNonExpired
        assertEquals  auth.pidm,testUser.pidm
        assertTrue    auth.webTimeout >= 30
        assertEquals auth.fullName,"McKall, Bernadette"
    }



    @Test
    void testSsbAuthenticationWithoutUsage() {

        Holders.config.guestAuthenticationEnabled= true
        Holders?.config?.productName ="testApp";
        Holders?.config?.banner.applicationName ="testApp";

        def auth = provider.authenticate( new TestAuthenticationRequest( testUser ) )
        assertTrue    auth.isAuthenticated()
        assertEquals  auth.name, testUser.name as String
        assertTrue    auth.details.credentialsNonExpired
        assertEquals  auth.pidm,testUser.pidm
        assertEquals auth.fullName,"Bernadette McKall"

    }


    @Test
    void testSsbAuthenticationWithUsage() {

        Holders.config.guestAuthenticationEnabled= true
        Holders?.config?.productName ="testApp_LFMI";
        Holders?.config?.banner.applicationName ="testApp_LFMI";

        def auth = provider.authenticate( new TestAuthenticationRequest( testUser ) )
        assertTrue    auth.isAuthenticated()
        assertEquals  auth.name, testUser.name as String
        assertTrue    auth.details.credentialsNonExpired
        assertEquals  auth.pidm,testUser.pidm
        assertEquals auth.fullName,"McKall, Bernadette"

    }

    @Test
    void testAuthorization() {
        def auth = provider.authenticate( new TestAuthenticationRequest( testUser ) )
        assertTrue    auth.isAuthenticated()
        assertNotNull auth.authorities.find { it.toString() == "ROLE_SELFSERVICE-ALLROLES_BAN_DEFAULT_M" }
        assertNotNull auth.authorities.find { it.toString() == "ROLE_SELFSERVICE-STUDENT_BAN_DEFAULT_M" }
        assertTrue  (auth.authorities.size() > 0)
    }

    @Test
    void testExpiredPin() {
       expireUser(testUser.pidm)

        shouldFail{
            provider.authenticate( new TestAuthenticationRequest( testUser ) )
        }

        extendExpiration(testUser.pidm)
    }

    @Test
    void testInvalidAccount() {

        def invalidUser = testUser.clone()
        invalidUser['pin'] = invalidUser.pin + '1'

        assertNull(provider.authenticate(new TestAuthenticationRequest(invalidUser)))

        resetInvalidLoginAttemptCount()
    }


    @Test // TODO: Renable after a single PL/SQL 'authenticate' API is provided. The twbkslib.f_fetchpidm function does not return a PIDM if the ssn is used.
    void testAuthenticateWithSocialSecurity() {
        sqlObj.executeUpdate "update twgbparm set twgbparm_param_value = 'Y' where twgbparm_param_name = 'ALLOWSSNLOGIN'"
        def auth = provider.authenticate( new TestAuthenticationRequest(testUser ) )
        assertTrue auth.isAuthenticated()
        assertEquals testUser.pidm, auth.pidm
    }


    @Test // TODO: Renable after a single PL/SQL 'authenticate' API is provided. The twbkslib.f_fetchpidm function does not return a PIDM if the ssn is used.
    void testDisableOnInvalidLogins() {
        resetInvalidLoginAttemptCount()

        def user = testUser.clone()
        user.pin = "XXXXXX"
        (1..4).each {
            assertNull provider.authenticate( new TestAuthenticationRequest( user) )
        }

        shouldFail {
            provider.authenticate( new TestAuthenticationRequest( user ) )
        }
        enableUser (sqlObj, testUser.pidm)
    }

    @Test
    public void testSupports () {
        assertTrue(provider.supports(UsernamePasswordAuthenticationToken.class))
        Holders.config.ssbEnabled = true
        assertTrue(provider.supports(UsernamePasswordAuthenticationToken.class))
    }

    @Test
    public void testIsSsbEnabled () {
        Holders.config.ssbEnabled = true
        assertTrue(provider.isSsbEnabled())
        Holders.config.ssbEnabled = false
        assertFalse(provider.isSsbEnabled())
    }

    //----------------------------- Helper Methods ------------------------------

    private void extendExpiration(pidm) {
        sqlObj.executeUpdate("update gobtpac set gobtpac_pin_exp_date = NULL where gobtpac_pidm=${pidm}")
        sqlObj.commit()
    }

    private void expireUser(pidm) {
        sqlObj.executeUpdate("update gobtpac set gobtpac_pin_exp_date = (SYSDATE-1) where gobtpac_pidm=${pidm}")
        sqlObj.commit()
    }


    private void resetInvalidLoginAttemptCount() {
        sqlObj.executeUpdate("update twgbwses set twgbwses_login_attempts=0 where twgbwses_pidm=$testUser.pidm")
        sqlObj.commit()
    }

    private def existingUser (userId, newPin) {
        def existingUser = [ name: userId]

        def testAuthenticationRequest = new TestAuthenticationRequest(existingUser)
        existingUser['pidm'] = provider.getPidm(testAuthenticationRequest, sqlObj )
        sqlObj.commit()
        sqlObj.call ("{call gb_third_party_access.p_update(p_pidm=>${existingUser.pidm}, p_pin=>${newPin})}")
        sqlObj.commit()
        existingUser.pin = newPin
        return existingUser
    }

    private ApplicationContext createUnderlyingSsbDataSourceBean() {
        def bb = new BeanBuilder()
        bb.beans {
            underlyingSsbDataSource(BasicDataSource) {
                maxActive = 5
                maxIdle = 2
                defaultAutoCommit = "false"
                driverClassName = "${Holders.config.bannerSsbDataSource.driver}"
                url = "${Holders.config.bannerSsbDataSource.url}"
                password = "${Holders.config.bannerSsbDataSource.password}"
                username = "${Holders.config.bannerSsbDataSource.username}"
            }
        }
        ApplicationContext testSpringContext = bb.createApplicationContext()
        return testSpringContext
    }


    private void enableUser(Sql db, pidm) {
        db.executeUpdate("update gobtpac set gobtpac_pin_disabled_ind='N' where gobtpac_pidm=$pidm")
        db.commit()
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

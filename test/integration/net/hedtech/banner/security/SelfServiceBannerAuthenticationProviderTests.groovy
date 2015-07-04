/*******************************************************************************
Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
*******************************************************************************/ 
package net.hedtech.banner.security

import grails.util.Holders
import groovy.sql.Sql
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.apache.commons.dbcp.BasicDataSource
import org.codehaus.groovy.grails.commons.ConfigurationHolder as CH
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.springframework.context.ApplicationContext
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.CredentialsExpiredException
import org.springframework.security.authentication.DisabledException
import org.springframework.security.core.Authentication

/**
 * Integration test for the self service Banner authentication provider.  
 **/
class SelfServiceBannerAuthenticationProviderTests extends BaseIntegrationTestCase{


    def provider    // set in setUp
    def conn        // set in setUp
    def db          // set in setUp
    def testUser    // set in setUp
    def currentSsbEnabledValue

    @Before
    public void setUp() {

        def bb = new grails.spring.BeanBuilder()
        bb.beans {
            underlyingSsbDataSource(BasicDataSource) {
                maxActive = 5
                maxIdle = 2
                defaultAutoCommit = "false"
                driverClassName = "${CH.config.bannerSsbDataSource.driver}"
                url = "${CH.config.bannerSsbDataSource.url}"
                password = "${CH.config.bannerSsbDataSource.password}"
                username = "${CH.config.bannerSsbDataSource.username}"
            }
        }

        ApplicationContext testSpringContext = bb.createApplicationContext()
        dataSource.underlyingSsbDataSource =  testSpringContext.getBean("underlyingSsbDataSource")

        provider = new SelfServiceBannerAuthenticationProvider()
        provider.dataSource = dataSource

        conn = dataSource.getSsbConnection()
        db = new Sql( conn )

        testUser = newUserMap( 'HOSWEB002' )
        // super.setUp()
    }

    @After
    public void tearDown() {
        dataSource.underlyingSsbDataSource =  null;
        //super.tearDown()
    }

    @Test
    void testRetrievalOfRoleBasedTimeouts() {

        def timeouts = provider.retrieveRoleBasedTimeOuts( db )
        assertTrue timeouts.size() > 0
    }

    @Test
    void testGetPidm() {  
        
        def pidm = provider.getPidm( new TestAuthenticationRequest( testUser ), db )
        assertEquals testUser.pidm, pidm
    }

    @Test
    void testAuthentication() {
        
        def auth = provider.authenticate( new TestAuthenticationRequest( testUser ) )
        assertTrue    auth.isAuthenticated()
        assertEquals  auth.name, testUser.id as String
        assertNotNull auth.oracleUserName
        assertTrue    auth.details.credentialsNonExpired
        assertEquals  auth.pidm,testUser.pidm
        assertTrue    auth.webTimeout >= 30
        assertEquals auth.fullName,"Edward Engle"
    }
        
    @Test
    void testAuthorization() {
        
        def auth = provider.authenticate( new TestAuthenticationRequest( testUser ) )
        assertTrue    auth.isAuthenticated()
        assertNotNull auth.authorities.find { it.toString() == "ROLE_SELFSERVICE-ALUMNI_BAN_DEFAULT_M" }
        assertNotNull auth.authorities.find { it.toString() == "ROLE_SELFSERVICE-STUDENT_BAN_DEFAULT_M" }
//        assertNotNull auth.authorities.find { it.toString() == "ROLE_SELFSERVICE_BAN_DEFAULT_M" }
        assertEquals  2, auth.authorities.size()
    }
    
    @Test
    void testExpiredPin() {
        
        def expiredPinUser = newUserMap( 'HOSS002' )
        shouldFail( CredentialsExpiredException ) {
            provider.authenticate( new TestAuthenticationRequest( expiredPinUser ) )
        }
    }

    @Test
    void testDisabledAccount() {
        
        def disabledUser = newUserMap( 'HOSS003' )
        shouldFail( DisabledException ) {
            provider.authenticate( new TestAuthenticationRequest( disabledUser ) )
        }
    }

    @Test
    void testInvalidAccount() {
           
        def disabledUser = newUserMap( 'HOSS003' )
        disabledUser['pin'] = disabledUser.pin + '1'
        assertNull(provider.authenticate( new TestAuthenticationRequest( disabledUser ) ))   
    }
        
    
    @Test // TODO: Renable after a single PL/SQL 'authenticate' API is provided. The twbkslib.f_fetchpidm function does not return a PIDM if the ssn is used.
    void testAuthenticateWithSocialSecurity() {
        
        db.executeUpdate "update twgbparm set twgbparm_param_value = 'Y' where twgbparm_param_name = 'ALLOWSSNLOGIN'"
        def user = newUserMap( 'HOSS001' )
        def auth = provider.authenticate( new TestAuthenticationRequest( [ id: '111-11-1111', pidm: user.pidm, pin: user.pin ] ) )
        assertTrue auth.isAuthenticated()
        assertTrue auth.pidm = user.pidm
    }


    @Test // TODO: Renable after a single PL/SQL 'authenticate' API is provided. The twbkslib.f_fetchpidm function does not return a PIDM if the ssn is used.
    void testDisableOnInvalidLogins() {
        
        def auth
        def user = newUserMap( 'HOSS001' )
        
        shouldFail( BadCredentialsException ) {
            auth = provider.authenticate( new TestAuthenticationRequest( [ id: '111-11-1111', pidm: user.pidm, pin: 'XXXXXX' ] ) )
        }
        shouldFail( BadCredentialsException ) {
            auth = provider.authenticate( new TestAuthenticationRequest( [ id: '111-11-1111', pidm: user.pidm, pin: 'XXXXXX' ] ) )
        }
        shouldFail( BadCredentialsException ) {
            auth = provider.authenticate( new TestAuthenticationRequest( [ id: '111-11-1111', pidm: user.pidm, pin: 'XXXXXX' ] ) )
        }
        shouldFail( BadCredentialsException ) {
            auth = provider.authenticate( new TestAuthenticationRequest( [ id: '111-11-1111', pidm: user.pidm, pin: 'XXXXXX' ] ) )
        }
        shouldFail( BadCredentialsException ) {
            auth = provider.authenticate( new TestAuthenticationRequest( [ id: '111-11-1111', pidm: user.pidm, pin: 'XXXXXX' ] ) )
        }
        shouldFail( DisabledException) {
            auth = provider.authenticate( new TestAuthenticationRequest( [ id: '111-11-1111', pidm: user.pidm, pin: 'XXXXXX' ] ) )
        }
    }


    //----------------------------- Helper Methods ------------------------------    
    
    
    private def isSsbEnabled() {
        CH.config.ssbEnabled instanceof Boolean ? CH.config.ssbEnabled : false
    }
    
    
    private def newUserMap( id ) {
        def expiredPinUser = [ id: id ]
        expiredPinUser['pidm'] = provider.getPidm( new TestAuthenticationRequest( expiredPinUser ), db )

        // retrieve pin for good user by generating a new one. This will be rolled back .         
        db.call( "{? = call gb_third_party_access.f_proc_pin(?)}", [ Sql.VARCHAR, expiredPinUser.pidm ] ) { pin -> 
            expiredPinUser['pin'] = pin
        }
        expiredPinUser
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
    public String getName() { user.id }
    public Object getPidm() { user.pidm }
    public Object getOracleUserName() { user.oracleUserName }
}

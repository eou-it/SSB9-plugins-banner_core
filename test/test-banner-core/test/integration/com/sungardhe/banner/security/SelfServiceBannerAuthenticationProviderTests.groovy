/** *****************************************************************************
 © 2011 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
package com.sungardhe.banner.security

import com.sungardhe.banner.db.BannerDS as BannerDataSource
import com.sungardhe.banner.exceptions.ApplicationException
import com.sungardhe.banner.service.KeyBlockHolder
import com.sungardhe.banner.service.ServiceBase

import org.codehaus.groovy.grails.commons.ConfigurationHolder as CH

import java.sql.Connection

import groovy.sql.Sql

import org.junit.Ignore

import org.codehaus.groovy.grails.commons.ConfigurationHolder

import org.springframework.security.authentication.AccountExpiredException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.CredentialsExpiredException
import org.springframework.security.authentication.DisabledException
import org.springframework.security.authentication.LockedException
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder


/**
 * Integration test for the self service Banner authentication provider.  
 **/
class SelfServiceBannerAuthenticationProviderTests extends GroovyTestCase {


    def dataSource  // injected by Spring
    
    def provider    // set in setUp
    def conn        // set in setUp
    def db          // set in setUp
    def testUser    // set in setUp

    protected void setUp() {
        
        if (isSsbEnabled()) {
            provider = new SelfServiceBannerAuthenticationProvider()
            provider.dataSource = dataSource

            conn = dataSource.getSsbConnection()                
            db = new Sql( conn )

            testUser = newUserMap( '210009105' )
            super.setUp()
        }
    }
    
    
    protected void tearDown() {
        
        if (isSsbEnabled()) {
            conn?.close()
            db?.close()
        }
    }


    void testGetPidm() {  
        if (!isSsbEnabled()) return     
        def pidm = provider.getPidm( new TestAuthenticationRequest( testUser ), db )
        assertEquals testUser.pidm, pidm
    }


    void testGetOracleUsername() {        
        if (!isSsbEnabled()) return     
        def oracleUserName = provider.getOracleUsername( testUser.pidm, db )
        assertNotNull oracleUserName
    }
    
    
    void testGetGobtpac() {        
        if (!isSsbEnabled()) return     
        def gobtpac = provider.getGobtpac( testUser.pidm, db )
        
        assertNull    gobtpac.ldap_user    
        assertNotNull gobtpac.external_user
        assertEquals  'N', gobtpac.disabled_ind 
        // assertNotNull gobtpac.pin_exp_date 
        
        db.call( "{? = call gb_third_party_access.f_get_pinhash(?,?)}", 
                 [ Sql.VARCHAR, testUser.pidm, testUser.pin ] ) { hash -> 
            testUser['pinHash'] = hash    
        } 
        assertEquals  testUser.pinHash, gobtpac.pin               
    }
    
    
    void testGetLdapParm() {        
        if (!isSsbEnabled()) return     
        def ldapParm = provider.getLdapParm( db )
        assertNotNull ldapParm
    }
    
    
    void testGetLdapId() {        
        if (!isSsbEnabled()) return     
        def gobtpac = provider.getGobtpac( testUser.pidm, db )
        def ldapId = provider.getLdapId( db, gobtpac )
        assertEquals 'eengle', ldapId
    }
    
    
    @Ignore // TODO: Workaround that underlying PL/SQL function is not accessible
    void testIsValidLdap() {        
        if (!isSsbEnabled()) return     
        def gobtpac = provider.getGobtpac( testUser.pidm, db )
        def isValidLdapId = provider.isValidLdap( db, testUser.id, gobtpac )
        assertTrue isValidLdapId
    }
    
    
    void testValidatePin() {        
        if (!isSsbEnabled()) return     
        def gobtpac = provider.getGobtpac( testUser.pidm, db )
        def pinValidation = provider.validatePin( testUser.pidm, testUser.pin, db )
        assertTrue  pinValidation.valid
        assertFalse pinValidation.expired
        assertFalse pinValidation.disabled
    }
    
    
    void testAuthentication() {
        if (!isSsbEnabled()) return     
        def auth = provider.authenticate( new TestAuthenticationRequest( testUser ) )
        assertTrue    auth.isAuthenticated()
        assertEquals  auth.name, testUser.id as String
        assertNotNull auth.oracleUserName
        assertTrue    auth.details.credentialsNonExpired
    }
        
    
    void testAuthorization() {
        if (!isSsbEnabled()) return     
        def auth = provider.authenticate( new TestAuthenticationRequest( testUser ) )
        assertTrue    auth.isAuthenticated()
        assertNotNull auth.authorities.find { it.toString() == "ROLE_SELFSERVICE-ALUMNI_BAN_DEFAULT_M" }
        assertNotNull auth.authorities.find { it.toString() == "ROLE_SELFSERVICE-STUDENT_BAN_DEFAULT_M" }
        assertNotNull auth.authorities.find { it.toString() == "ROLE_SELFSERVICE_BAN_DEFAULT_M" }
        assertEquals  3, auth.authorities.size()
    }
    
    
    void testValidatePinForExpiredPin() {        
        if (!isSsbEnabled()) return 
        
        def expiredPinUser = newUserMap( 'HOSS002' )    
        def pinValidation = provider.validatePin( expiredPinUser.pidm, expiredPinUser.pin, db )
        assertTrue  pinValidation.expired
        assertTrue  pinValidation.valid
        assertFalse pinValidation.disabled
    }
    
    
    void testExpiredPin() {
        if (!isSsbEnabled()) return 
        
        def expiredPinUser = newUserMap( 'HOSS002' )
        shouldFail( CredentialsExpiredException ) {
            provider.authenticate( new TestAuthenticationRequest( expiredPinUser ) )
        }
    }
    
    
    void testValidatePinForDisabledAccount() {        
        if (!isSsbEnabled()) return 
        
        def disabledUser = newUserMap( 'HOSS003' )    
        def pinValidation = provider.validatePin( disabledUser.pidm, disabledUser.pin, db )
        assertFalse pinValidation.expired
        assertTrue  pinValidation.valid
        assertTrue  pinValidation.disabled
    }
    
    
    void testDisabledAccount() {
        if (!isSsbEnabled()) return 
        
        def disabledUser = newUserMap( 'HOSS003' )
        shouldFail( DisabledException ) {
            provider.authenticate( new TestAuthenticationRequest( disabledUser ) )
        }
    }
        
    
    @Ignore // TODO: Renable after a single PL/SQL 'authenticate' API is provided. The twbkslib.f_fetchpidm function does not return a PIDM if the ssn is used.
    void testAuthenticateWithSocialSecurity() {
        db.executeUpdate "update twgbparm set twgbparm_param_value = 'Y' where twgbparm_param_name = 'ALLOWSSNLOGIN'"
        def user = newUserMap( 'HOSS001' )
        def auth = provider.authenticate( new TestAuthenticationRequest( [ id: '111-11-1111', pidm: user.pidm, pin: user.pin ] ) )
        assertTrue auth.isAuthenticated()
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
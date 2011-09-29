/*********************************************************************************
 Copyright 2009-2011 SunGard Higher Education. All Rights Reserved.
 This copyrighted software contains confidential and proprietary information of 
 SunGard Higher Education and its subsidiaries. Any use of this software is limited 
 solely to SunGard Higher Education licensees, and is further subject to the terms 
 and conditions of one or more written license agreements between SunGard Higher 
 Education and the licensee in question. SunGard is either a registered trademark or
 trademark of SunGard Data Systems in the U.S.A. and/or other regions and/or countries.
 Banner and Luminis are either registered trademarks or trademarks of SunGard Higher 
 Education in the U.S.A. and/or other regions and/or countries.
 **********************************************************************************/
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


    void testRetrievalOfRoleBasedTimeouts() {

        if (!isSsbEnabled()) return     
        def timeouts = provider.retrieveRoleBasedTimeOuts( db )
        assertTrue timeouts.size() > 0
    }


    void testGetPidm() {  
        
        if (!isSsbEnabled()) return     
        def pidm = provider.getPidm( new TestAuthenticationRequest( testUser ), db )
        assertEquals testUser.pidm, pidm
    }


    void testAuthentication() {
        
        if (!isSsbEnabled()) return     
        def auth = provider.authenticate( new TestAuthenticationRequest( testUser ) )
        assertTrue    auth.isAuthenticated()
        assertEquals  auth.name, testUser.id as String
        assertNotNull auth.oracleUserName
        assertTrue    auth.details.credentialsNonExpired
        assertEquals  auth.pidm,testUser.pidm
        assertTrue    auth.webTimeout >= 30
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
    

    void testExpiredPin() {
        
        if (!isSsbEnabled()) return
        def expiredPinUser = newUserMap( 'HOSS002' )
        shouldFail( CredentialsExpiredException ) {
            provider.authenticate( new TestAuthenticationRequest( expiredPinUser ) )
        }
    }

    
    void testDisabledAccount() {
        
        if (!isSsbEnabled()) return         
        def disabledUser = newUserMap( 'HOSS003' )
        shouldFail( DisabledException ) {
            provider.authenticate( new TestAuthenticationRequest( disabledUser ) )
        }
    }


    void testInvalidAccount() {
           
        if (!isSsbEnabled()) return
        def disabledUser = newUserMap( 'HOSS003' )
        disabledUser['pin'] = disabledUser.pin + '1'
        assertNull(provider.authenticate( new TestAuthenticationRequest( disabledUser ) ))   
    }
        
    
    @Ignore // TODO: Renable after a single PL/SQL 'authenticate' API is provided. The twbkslib.f_fetchpidm function does not return a PIDM if the ssn is used.
    void testAuthenticateWithSocialSecurity() {
        
        db.executeUpdate "update twgbparm set twgbparm_param_value = 'Y' where twgbparm_param_name = 'ALLOWSSNLOGIN'"
        def user = newUserMap( 'HOSS001' )
        def auth = provider.authenticate( new TestAuthenticationRequest( [ id: '111-11-1111', pidm: user.pidm, pin: user.pin ] ) )
        assertTrue auth.isAuthenticated()
    }


    @Ignore // TODO: Renable after a single PL/SQL 'authenticate' API is provided. The twbkslib.f_fetchpidm function does not return a PIDM if the ssn is used.
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
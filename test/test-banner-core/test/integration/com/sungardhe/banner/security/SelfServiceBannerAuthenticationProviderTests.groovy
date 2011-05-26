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

            testUser = [ spridenId: 210009105, pidm: 24 ]

            // retrieve pin for good user by generating a new one. This will be rolled back .         
            db.call( "{? = call gb_third_party_access.f_proc_pin(?)}", [ Sql.VARCHAR, testUser.pidm ] ) { pin -> 
                testUser['pin'] = pin
            }
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
        
        assertNull gobtpac.ldap_user    
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
        def isValidLdapId = provider.isValidLdap( db, testUser.spridenId, gobtpac )
        assertTrue isValidLdapId
    }
    
    
    void testValidatePin() {        
        if (!isSsbEnabled()) return     
        def gobtpac = provider.getGobtpac( testUser.pidm, db )
        def pinValidation = provider.validatePin( testUser.pidm, testUser.pin, db )
        assertTrue pinValidation.valid
        assertFalse pinValidation.expired
        assertFalse pinValidation.disabled
    }
    
    
    void testAuthentication() {
        if (!isSsbEnabled()) return     
        def auth = provider.authenticate( new TestAuthenticationRequest( testUser ) )
        assertTrue auth.isAuthenticated()
        assertEquals testUser.spridenId as String, auth.name
        assertNotNull auth.oracleUserName
    }
        
    
    void testAuthorization() {
        if (!isSsbEnabled()) return     
        def auth = provider.authenticate( new TestAuthenticationRequest( testUser ) )
        assertTrue auth.isAuthenticated()
        assertNotNull auth.authorities.find { it.toString() == "ROLE_SELFSERVICE-ALUMNI_BAN_DEFAULT_M" }
        assertNotNull auth.authorities.find { it.toString() == "ROLE_SELFSERVICE-STUDENT_BAN_DEFAULT_M" }
        assertNotNull auth.authorities.find { it.toString() == "ROLE_SELFSERVICE_BAN_DEFAULT_M" }
        assertEquals 3, auth.authorities.size()
    }


    //----------------------------- Helper Methods ------------------------------    
    
    
    private def isSsbEnabled() {
        CH.config.ssbEnabled instanceof Boolean ? CH.config.ssbEnabled : false
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
    public String getName() { user.spridenId }
    public Object getPidm() { user.pidm }
    public Object getOracleUserName() { user.oracleUserName }
}
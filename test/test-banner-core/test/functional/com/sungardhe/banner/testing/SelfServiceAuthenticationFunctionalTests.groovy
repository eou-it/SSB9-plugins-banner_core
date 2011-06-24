/** *****************************************************************************
 © 2011 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
package com.sungardhe.banner.testing

import java.sql.Connection

import javax.sql.DataSource

import groovy.sql.Sql

import org.codehaus.groovy.grails.commons.ApplicationHolder as AH
import org.codehaus.groovy.grails.commons.ConfigurationHolder as CH

import org.springframework.context.ApplicationContext


/**
 * Functional tests of self service authentication.
 */
class SelfServiceAuthenticationFunctionalTests extends BaseFunctionalTestCase {


    def dataSource  // injected by Spring


    protected void setUp() {
        formContext = [ 'SELFSERVICE' ]
        super.setUp()
    }


    // Tests ability to access a self service URL. 
    void testSelfServiceUserAccess() {
         if (isSsbEnabled()) {
             
            loginSelfServiceUser( [ spridenId: 210009105, pidm: 24 ] )
             
            get "/ssb/foobar/view"
            
            assertStatus 200
            assertEquals 'text/html', page?.webResponse?.contentType
            
            def stringContent = page?.webResponse?.contentAsString
            assertTrue stringContent ==~ /.*If I had a UI.*/
         }
    }
    
    
    void testSelfServiceExpiredPin() {
         if (isSsbEnabled()) {
             
            loginSelfServiceUser( [ spridenId: 'HOSS002', pidm: 49528 ] )
             
            get "/ssb/foobar/view"
            
            assertTitle 'Login'             
            assertStatus 200
            assertEquals 'text/html', page?.webResponse?.contentType
         }
    }
    
    
    void testSelfServiceDisabled() {
         if (isSsbEnabled()) {
             
            loginSelfServiceUser( [ spridenId: 'HOSS003', pidm: 49529 ] )
             
            get "/ssb/foobar/view"
            
            assertTitle 'Login'             
            assertStatus 200
            assertEquals 'text/html', page?.webResponse?.contentType
         }
    }
    

    //----------------------------- Helper Methods ------------------------------    


    // Expects a map like: [ spridenId: #########, pidm: ### ]
    def loginSelfServiceUser( Map user_credentials ) { 
        def conn
        def db
        
        try {             
            conn = getDataSource().getSsbConnection()                
            db = new Sql( conn )
            
            // retrieve pin for user by generating a new one. This will be rolled back.         
            db.call( "{? = call gb_third_party_access.f_proc_pin(?)}", 
                     [ Sql.VARCHAR, user_credentials.pidm ] ) { 
                pin -> user_credentials['pin'] = pin 
            }             
            login "${user_credentials.spridenId}", "${user_credentials.pin}"
        } 
        finally {
            conn?.close()
            db?.close()
        }         
     }


    private def isSsbEnabled() {
        CH.config.ssbEnabled instanceof Boolean ? CH.config.ssbEnabled : false
    }
     
     
    protected def getDataSource() {
        if (!dataSource) {
            ApplicationContext ctx = (ApplicationContext) AH.getApplication().getMainContext()
            dataSource = (DataSource) ctx.getBean( 'dataSource' )
        }
        dataSource
    }
     

}
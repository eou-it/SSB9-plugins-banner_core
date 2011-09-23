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
             
            loginSelfServiceUser( [ spridenId: '210009105' ] )
             
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
        def pidm = getPidm(user_credentials.spridenId)
        
        try {             
            conn = getDataSource().getSsbConnection()                
            db = new Sql( conn )

            
            // retrieve pin for user by generating a new one. This will be rolled back.         
            db.call( "{? = call gb_third_party_access.f_proc_pin(?)}", 
                     [ Sql.VARCHAR, pidm ] ) {
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


      private def getPidm(id) {
        def pidm
          def conn
        def sql 
  
        try {
            conn = getDataSource().getSsbConnection()
            sql = new Sql( conn )
            sql.eachRow("""
                     SELECT SPRIDEN_PIDM
                       FROM SPRIDEN
                       WHERE SPRIDEN_ID = ?
                   """, [id]) {

                pidm = it[0]

            };

        } finally {
            sql?.close()
        }

        pidm
    }
     

}
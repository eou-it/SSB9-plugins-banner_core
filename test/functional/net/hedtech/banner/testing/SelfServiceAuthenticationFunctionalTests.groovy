/*******************************************************************************
Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
*******************************************************************************/ 
package net.hedtech.banner.testing

import groovy.sql.Sql
import grails.util.Holders  as AH
import grails.util.Holders  as CH
import org.junit.After
import org.junit.Before
import org.springframework.context.ApplicationContext

import javax.sql.DataSource

/**
 * Functional tests of self service authentication.
 */
class SelfServiceAuthenticationFunctionalTests extends BaseFunctionalTestCase {


    def dataSource  // injected by Spring

    @Before
    protected void setUp() {
        formContext = [ 'SELFSERVICE' ]
        super.setUp()
    }

    @After
    public void tearDown() {
        super.tearDown()
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
            
            assertTitle 'Sign In'
            assertStatus 200
            assertEquals 'text/html', page?.webResponse?.contentType
         }
    }
    
    
    void testSelfServiceDisabled() {
         if (isSsbEnabled()) {
             
            loginSelfServiceUser( [ spridenId: 'HOSS003', pidm: 49529 ] )
             
            get "/ssb/foobar/view"
            
            assertTitle 'Sign In'
            assertStatus 200
            assertEquals 'text/html', page?.webResponse?.contentType
         }
    }
    


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
     
     
    def getDataSource() {
        if (!dataSource) {
            ApplicationContext ctx = (ApplicationContext) AH.grailsApplication.getMainContext()
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

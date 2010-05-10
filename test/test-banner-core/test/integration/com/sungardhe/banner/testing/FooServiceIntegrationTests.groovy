/** *****************************************************************************

 © 2010 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
package com.sungardhe.banner.testing

import com.sungardhe.banner.json.JsonHelper
import com.sungardhe.banner.testing.BaseIntegrationTestCase

import java.sql.Connection

import groovy.sql.Sql

import grails.converters.JSON

import org.codehaus.groovy.grails.commons.ConfigurationHolder

import org.springframework.security.context.SecurityContextHolder


/**
 * Integration test for the Foo service.  This test is 'not transactional'
 * and is used to verify the desired declarative transactions are in effect.
 * This is NOT a model for normal 'service' tests -- it is a framework test. 
 **/
class FooServiceIntegrationTests extends BaseIntegrationTestCase {

    def fooService   // injected by Spring
    
    
    protected void setUp() {
        formContext = ['STVCOLL'] 
// Transactional testing is not yet functional -- TODO: Resolve proxy issues preventing use of @Transactional annotation
//        useTransactions = false   // this is what most tests should do, to ensure declarative transactions        
        assert fooService != null            
        super.setUp()
        tearDownTestFoo( true ) // tearDown should take care of this
    }
    
    protected void tearDown() {
        tearDownTestFoo()
        super.tearDown()
    }
    
    
    private def tearDownTestFoo( boolean warnIfFound = false ) {
        def testFoo = Foo.findByDescription( "Horizon Test Data" )
        if (testFoo) {
            if (warnIfFound) log.warn "Test data was found that should have been torn down!"
            Foo.withTransaction { testFoo.delete( failOnError:true, flush: true ) }
            assertNull Foo.findByDescription( "Horizon Test Data" )
        }
    }
    
    
    void testSave() { 
        def foo = fooService.create( newTestFooParams() )
        assertNotNull foo.id
        assertEquals "Horizon Test Data", foo.description 
    }
    
    
    void testReadOnlyMethod() { 
        def foo = fooService.create( newTestFooParams() )
        assertNotNull foo.id
        assertNotNull fooService.fetch( foo.id ) // note that additional assertions will be made by this 'fetch' test method
    }
    

// Currently disabled pending full support of transaction testing and transaction demarcation using @Transaction annotations
    // This tests that we can see pending creates in the
    // current transaction and that we cannot see pending creates in a different transaction.
    // We'll put this here as we need the full database environment versus a unit test.
/*    void testTransactionCommitted() {
        Connection conn = null
        def sql = null
        def found 
        def otherThread
    
        try {
            def foo = fooService.create( newTestFooParams() )
            assertNotNull foo.id
        
            // and ensure we can see it using a connnection from another thread -- i.e., that the transaction committed
            otherThread = new Thread( {  // we'll use a closure to get a connection via a different thread
                com.sungardhe.banner.security.FormContext.set( ['STVCOLL'] )
                login()
                def sqlB = null
                try {
                    sqlB = new Sql( dataSource )
                    found = sqlB.firstRow( "select STVCOLL_DESC from saturn.STVCOLL where STVCOLL_DESC = 'Horizon Test Data'" )
                } finally {
                    sqlB?.close()
                }
            })
            otherThread.start()
        } finally {
            sql?.close()
            // note we don't close the connection - the test framework will perform a rollback and then close
        }
    
        otherThread.join()
        assertTrue found?.toString().contains( "Horizon Test Data" )
    }
*/                       

    private Map newTestFooParams() {
        [ code: "TT", description: "Horizon Test Data", addressStreetLine1: "TT", addressStreetLine2: "TT", addressStreetLine3: "TT", addressCity: "TT",
		  addressState: "TT", addressCountry: "TT", addressZipCode: "TT", systemRequiredIndicator: "N", voiceResponseMessageNumber: 1, statisticsCanadianInstitution: "TT", 
		  districtDivision: "TT", houseNumber: "TT", addressStreetLine4: "TT" ]
    }

}
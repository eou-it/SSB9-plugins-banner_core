/** *****************************************************************************
 © 2010 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
package com.sungardhe.banner.testing

import java.sql.Connection

import groovy.sql.Sql
import org.springframework.security.core.context.SecurityContextHolder
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.junit.Ignore

/**
 * Integration test for the Foo service.  This test is 'not transactional'
 * and is used to verify the desired declarative transactions are in effect.
 * This is NOT a model for normal 'service' tests -- it is a framework test. 
 **/
class FooServiceIntegrationTests extends BaseIntegrationTestCase {

    
    def fooService                     // injected by Spring
    def supplementalDataService        // injected by Spring
    def sessionContext                 // injected by Spring
    
    
    protected void setUp() {
        formContext = ['STVCOLL'] 
        assert fooService != null            
        super.setUp()
        tearDownTestFoo( true ) // tearDown should take care of this -- will really only be effective once we stop managing
                                // transactions within the test framework (that is, for a specific framework test of transactions) 
                                // but instead rely on declarative transactions. 
    }
    
    protected void tearDown() {
        tearDownTestFoo()
        super.tearDown()
    }


    void testSaveOutsideService() {
        def foo = new Foo( newTestFooParams() )
        save foo
        
        assertNotNull foo.id
        assertEquals "Horizon Test - TT", foo.description

        assertEquals ConfigurationHolder.config?.dataOrigin, foo.dataOrigin
        assertEquals SecurityContextHolder.context?.authentication?.principal?.username, foo.lastModifiedBy
        assertNotNull foo.lastModified
    }
    
    
    void testSave() { 
        def foo = fooService.create( newTestFooParams() )
        assertNotNull foo.id
        assertEquals "Horizon Test - TT", foo.description

        assertEquals ConfigurationHolder.config?.dataOrigin, foo.dataOrigin
        assertEquals SecurityContextHolder.context?.authentication?.principal?.username, foo.lastModifiedBy
        assertNotNull foo.lastModified
    }

    
    void testUpdate() { 
        def foo = fooService.create( newTestFooParams() )
        assertNotNull foo.id
        assertEquals "Horizon Test - TT", foo.description

        assertFalse foo.isDirty()
        foo.description = "Updated"
        assertTrue foo.isDirty()
        
        def id = foo.id 
        def version = foo.version
        def lastModified = foo.lastModified

        def updatedFoo = fooService.update( foo )
        assertEquals id, updatedFoo.id
        assertEquals version + 1, updatedFoo.version
        assertFalse lastModified == updatedFoo.lastModified
    }

    
    void testUpdateNotDirty() { 
        def foo = fooService.create( newTestFooParams() )
        assertNotNull foo.id
        assertEquals "Horizon Test - TT", foo.description
        
        def id = foo.id 
        def version = foo.version
        def lastModified = foo.lastModified
        
        def updatedFoo = fooService.update( foo )
        assertEquals id, updatedFoo.id
        assertEquals version, updatedFoo.version
        assertTrue lastModified == updatedFoo.lastModified
    }


    void testUpdateMultipleNotDirty() {
        def foos = [ fooService.create( newTestFooParams(), false ),
                     fooService.create( newTestFooParams( 'UU' ), false ),
                     fooService.create( newTestFooParams( 'VV' ), false ) ]

        def properties = []
        foos.each {
            properties << [ code: it.code, version: it.version, lastModified: it.lastModified ]
        }

        foos.find { it.code == "UU" }.description = "Updated"
        def updatedFoos = fooService.createOrUpdate( foos )

        assertEquals 3, updatedFoos.size()
        assertEquals 1, updatedFoos.findAll { it.description == "Updated" }.size()
        assertEquals 2, updatedFoos.findAll { it.description.contains( "Horizon Test -" ) }.size()
        assertEquals properties.find { it.code == "TT" }.version, updatedFoos.find { it.code == "TT" }.version
        assertEquals properties.find { it.code == "UU" }.version + 1, updatedFoos.find { it.code == "UU" }.version
        assertEquals properties.find { it.code == "VV" }.version, updatedFoos.find { it.code == "VV" }.version
    }

    
    // Note: This test is not effective, and thus only ensures we don't encounter exceptions etc.
    // There does not seem to be exposed callbacks to monitor when the flush actually occurs, 
    // and using flush=false does not seem to have any effect -- at least when running integration tests.
    // The code exercised by this test is retained simply as it does pass the boolean in accordance
    // with Grails conventions. TODO: Implement true test, investigate why flushing is immediate regardless of 'flush: false'. 
    // 
    void testSaveWithoutFlush() { 
        Foo.withSession { session ->            
            def foos = [ fooService.create( newTestFooParams(), false ),
                         fooService.create( newTestFooParams( 'UU' ), false ),
                         fooService.create( newTestFooParams( 'VV' ), false ) ]
/*
        // Note: This currently finds the record -- proving that the DDL has in fact been executed. 
        def sql = null
            try {
                sql = new Sql( session.connection() )
                assertNull sql.firstRow( "select STVCOLL_CODE from saturn.STVCOLL where STVCOLL_CODE = 'UU'" )
            } finally {
                sql?.close()
            }
*/
            fooService.flush()
            assertEquals 3, foos.findAll { it.id > 0 }.size()
        }
    }


    // This is a 'framework' test -- please do not copy it into your normal service integration tests.
    // This tests that we can see pending creates in the current transaction and that we cannot see 
    // pending creates in a different transaction.
    void testTransactionIsolation() {
        Connection conn = null
        def found 
        def otherThread
    
        def foo = fooService.create( newTestFooParams() )
        assertNotNull foo.id
        
        // and ensure we can see it using a connnection from another thread -- i.e., that the transaction committed
        otherThread = new Thread( {  // we'll use a closure to get a connection via a different thread
            com.sungardhe.banner.security.FormContext.set( ['STVCOLL'] )
            login()
            def sql = null
            try {
                sql = new Sql( dataSource )
                found = sql.firstRow( "select STVCOLL_DESC from saturn.STVCOLL where STVCOLL_DESC = 'Horizon Test Data'" )
            } finally {
                sql?.close()
            }
        })
        otherThread.start()
    
        otherThread.join()
        assertNull found
    }


    void testUsingReadOnlyTransaction() {
        assertTrue fooService.useReadOnlyRequiredTransaction()
    }


    void testUsingRequiredTransaction() {
        assertTrue fooService.useRequiredTransaction()
    }


    void testUsingSupportsTransaction() {
        assertTrue fooService.useSupportsTransaction()
    }


    // TODO: Enable this test (or employ an alternate means to verify support for 'REQUIRES_NEW' transaction propagation)
    @Ignore // Results in an exception:  No value for key [org.hibernate.impl.SessionFactoryImpl@cb8a0c8] bound to thread [main]
    void testUsingRequiresNewTransaction() {
        Foo.withTransaction {
            assertTrue fooService.useRequiresNewTransaction()
        }
    }
                       
    
    // really only effective once we stop managing transactions within tests and truly use the declarative transaction boundaries.
    // This testing, once the @Transactional attribute is working, may need to be performed within a functional test...
    private def tearDownTestFoo( boolean warnIfFound = false ) {
        def testFoos = Foo.findAllByDescriptionLike( "Horizon Test" )
        if (testFoos?.size() > 0) {
            if (warnIfFound) log.warn "Test data was found that should have been torn down!"
            Foo.withTransaction { 
                testFoos.each { it.delete( failOnError:true, flush: true ) } 
            }
            assertEquals 0, Foo.findAllByDescriptionLike( "Horizon Test" )
        }
    }
    

    private Map newTestFooParams( code = "TT" ) {
        [ code: code, description: "Horizon Test - $code", addressStreetLine1: "TT", addressStreetLine2: "TT", addressStreetLine3: "TT", addressCity: "TT",
		  addressState: "TT", addressCountry: "TT", addressZipCode: "TT", systemRequiredIndicator: "N", voiceResponseMessageNumber: 1, statisticsCanadianInstitution: "TT", 
		  districtDivision: "TT", houseNumber: "TT", addressStreetLine4: "TT" ]
    }

}


/** *****************************************************************************
 © 2010 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
package com.sungardhe.banner.testing

import com.sungardhe.banner.db.BannerDS as BannerDataSource
import com.sungardhe.banner.exceptions.ApplicationException
import com.sungardhe.banner.security.FormContext
import com.sungardhe.banner.service.KeyBlockHolder
import com.sungardhe.banner.service.ServiceBase

import java.sql.Connection

import groovy.sql.Sql

import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.springframework.security.core.context.SecurityContextHolder

import org.junit.Ignore

/**
 * Integration test for the Foo service.  
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
        fooService.testKeyBlock = null // remove any testkeyBlock (this is a test-only field exposed by the FooService)
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
    

    void testSaveInvalid() {
        try {
            // note: this will be invalid for both code and description, resulting in two errors
            fooService.create( newTestFooParams( 'TTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT' ) )
            fail( "Was able to save an invalid Foo!" )
        } catch (ApplicationException e) {
            def returnMap = e.returnMap( new FooController().localizer )
            assertTrue "Return map not as expected but was: ${returnMap.message}",
            returnMap.message ==~ /.*The Foo cannot be saved, as it contains errors.*/
            assertFalse returnMap.success
            // note that validation exceptions, unlike most others, may hold many localized error messages that may be presented to a user
            assertEquals 2L, returnMap.errors?.size()
            if (returnMap.errors instanceof List){
                def errors = returnMap.errors
                errors.each { error ->
                    assertTrue ((error.field == "description" && error.model == "com.sungardhe.banner.testing.Foo" && error.message?.contains( "exceeds the maximum size of" )
                    && error.rejectedValue == "Horizon Test - TTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT") ||
                    (error.field == "code" && error.model == "com.sungardhe.banner.testing.Foo" && error.message=="The foo code is too long, it must be no more than 2 characters"
                    && error.rejectedValue == "TTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT"))
                }
            }  else {
                assertTrue returnMap.errors ==~ /.*The foo code is too long, it must be no more than 2 characters.*/
            }
            assertNotNull returnMap.underlyingErrorMessage // this is the underlying exception's message (which is not appropriate to display to a user)
        }
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
        def lastModifiedTime = foo.lastModified.time
        
        sleep 1000      // would not really be needed, except that 'Foo' is marked (artificially, and incorrectly) as needing a refresh() immediately 
                        // after being saved (just to ensure code is exercised during the test). Since lastModified is mapped to a SQL DATE type, 
                        // the fractional seconds are truncated -- so to test that the date is 'changed' we need some time to elapse...

        def updatedFoo = fooService.update( foo )
        assertEquals id, updatedFoo.id
        assertEquals "Updated", updatedFoo.description
        assertEquals version + 1, updatedFoo.version
        assertFalse "original lastModified was $lastModifiedTime and updatedFoo.lastModified is ${updatedFoo.lastModified.time}", lastModifiedTime == updatedFoo.lastModified.time
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


    void testUpdateReadOnlyPropertyDirty() {
        def foo = fooService.create( newTestFooParams() )
        foo.description = "Changed"
        foo.addressCountry = "NO"
        foo.addressZipCode = "989823"

        try {
            fooService.update( foo )
        } catch (ApplicationException e) {
            def returnMap = e.returnMap( new FooController().localizer )
            assertTrue "Return map not as expected but was: ${returnMap.message}",
                returnMap.message ==~ /.*There was an attempt to modify read-only properties: Address Country, Address Zip Code\. This has been logged already.*/
        }
    }

    // Note: Changes to the sequence generator strategy resolved delaying the flush, which has now uncovered issues within ServiceBase.
    //
    //
    @Ignore // TODO: Remove flushImmediate option on ServiceBase methods, or correctly handle a delayed flush within ServiceBase
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
    // Note: Changes to the sequence generator strategy resolved delaying the flush, which has now uncovered issues within ServiceBase.
    //
    //
    @Ignore // TODO: Remove flushImmediate option on ServiceBase methods, or correctly handle a delayed flush within ServiceBase
    void testSaveWithoutFlush() {
        Foo.withSession { session ->
            def foos = [ fooService.create( newTestFooParams(), false ),
                         fooService.create( newTestFooParams( 'UU' ), false ),
                         fooService.create( newTestFooParams( 'VV' ), false ) ]
/*
        // Note: This currently finds the record -- showing the DDL has in fact been executed.
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


    void testUsingRequiresNewTransaction() {
        def newTransaction 
        def otherThread = new Thread( {  // we'll use a closure to interact with the service on a different thread
            newTransaction = fooService.useRequiresNewTransaction()
        } )
        otherThread.start()
        otherThread.join()
        
        assertTrue newTransaction
    }
       

    void testKeyBlockFoundInMap() {
        def foo = fooService.create( newTestFooParams() )
        foo.description = "Updated"
        
        assertNull fooService.testKeyBlock

        def updatedFoo = fooService.update( [ foo: foo, keyBlock: [ kb: 'Dummy KeyBlock' ] ] )
        assertEquals "Dummy KeyBlock", fooService.getTestKeyBlock()?.kb
    }


    void testKeyBlockFoundInHolder() {
        def foo = fooService.create( newTestFooParams() )
        foo.description = "Updated"
    
        assertNull fooService.testKeyBlock
        KeyBlockHolder.set( [ kb: 'Dummy KeyBlock' ] )
    
        def updatedFoo = fooService.update( foo )
        assertEquals "Dummy KeyBlock", fooService.getTestKeyBlock()?.kb
    }
    
    
    void testApiContextVariableUsage() {
        Foo.withTransaction {
            fooService.setApiContext( 'GB_MEDICAL', 'CHECK_HR_SECURITY', 'Y' )
            def sql = new Sql( sessionFactory.getCurrentSession().connection() )
            sql.call( "{? = call gb_common.f_get_context(?,?)}", [ Sql.VARCHAR, 'GB_MEDICAL', 'CHECK_HR_SECURITY' ] ) { value ->
               assertEquals 'Y', value 
            }         
        }
    }
    
    
    // While not really a 'service' test, it is implemented here for convenience
    void testDatabaseSessionIdentifier() {
        def conn = sessionFactory.getCurrentSession().connection()
        (dataSource as BannerDataSource).setIdentifier( conn, "Donald_Duck" )  
        def sql = new Sql( conn ) // test will close this connection                          
        def row = sql.firstRow( "select sys_context( 'USERENV', 'CLIENT_IDENTIFIER' ) FROM DUAL" )
        assertEquals "Donald_Duck", row."SYS_CONTEXT('USERENV','CLIENT_IDENTIFIER')"                 
    }
    
    
    // While not really a 'service' test, it is implemented here for convenience
    void testSettingDbmsApplicationInfo() {
        def conn = sessionFactory.getCurrentSession().connection()
        (dataSource as BannerDataSource).setDbmsApplicationInfo( conn, "${this.class.simpleName}", 'testSettingDbmsApplicationInfo()' )             
        def sql = new Sql( conn )        
        sql.call( "{call dbms_application_info.read_module(?,?)}", [ Sql.out(Sql.VARCHAR.type), Sql.out(Sql.VARCHAR.type) ] ) { module, action ->
            assertEquals "${this.class.simpleName}", module
            assertEquals 'testSettingDbmsApplicationInfo()', action 
        }            
    }
    
    
// -------------------------------------- Supporting Methods -------------------------------------------    
    

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


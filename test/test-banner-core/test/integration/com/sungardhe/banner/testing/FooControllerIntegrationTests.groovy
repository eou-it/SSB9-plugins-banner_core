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
 * Integration test for the Foo controller.
 **/
class FooControllerIntegrationTests extends BaseIntegrationTestCase {

    def fooService   // injected by Spring
    
    static List serviceCallbacks
    
    
    protected void setUp() {
        
        // You should set a 'controller' prior to calling super.setUp(), as this will be used 
        // to set the FormContext list that identifies the Oracle Forms/Objects being replaced by this controller, 
        // as well as wire in the convenience maps: params, flash, redirectMap, and renderMap.
        
        // Note: The FormContext is needed to ensure the appropriate roles are unlocked on the proxied connection. 
        // You may set the formContext explicitly before calling super.setUp(). This may be needed if the formControllerMap 
        // within the configuration uses a different controller name than the 'grails name' for the controller. 
                 
        // formContext = ['STVCOLL'] // will be set automatically since we'll set our controller before calling super.setUp()

        controller = new FooController()    
        
        assert fooService != null    
        controller.fooService = fooService        
        
        super.setUp()
    }
    
    
    void testSave() { // with html
        params = newFooParams()        
        controller.save()
        assertEquals "Redirect action not as expected: ${redirectMap.action}", 'show', redirectMap.action 
        assertNotNull "Redirect id not as expected: ${redirectMap.id}", redirectMap.id
        assertTrue flash ==~ /.*created.*/
    }
    

    void testShowWithJSON() { 
        def entity = new Foo( newFooParamsWithAuditTrailProperties() )
        save entity
        
        params = [ id: entity.id ] 
        
        controller.request.contentType = "application/json"
        controller.show() 
         
        def result = JSON.parse( controller.response.contentAsString )

        JsonHelper.replaceJSONObjectNULL( result?.data ) // Minimal workaround for Jira Grails-5585; TODO: Remove when Jira is resolved.
    
        assertEquals "Found ${result?.data?.id} but expected ${entity.id}", entity.id, result?.data?.id 
        assertEquals "Found ${result?.data?.code} but expected ${entity.code}", entity.code, result?.data?.code 
        assertEquals "Found ${result?.data?.description} but expected ${entity.description}", entity.description, result?.data?.description
    }                


    void testInsertWithJSON() {    
        params = [ data: "${newFooParams() as JSON}" ] // notice we send json as well as receive json        

        controller.request.contentType = "text/json"
        controller.save() 
        
        def content = controller.response.contentAsString
        assertNotNull content

        def result = JSON.parse( content )
        assertTrue result?.success
      
        assertNotNull result?.data?.id
        assertEquals 'TT', result?.data?.code 
        
        assertEquals ConfigurationHolder.config?.dataOrigin, result?.data?.dataOrigin
        assertEquals SecurityContextHolder.context?.authentication?.principal?.username, result?.data?.lastModifiedBy
        assertNotNull result?.data?.lastModified
    }


    void testInsertWithJsonUsingInvalidEntity() { 
        def paramMap = newFooParams()  
        paramMap.code = "TOO_LONG"              
        params = [ data: "${paramMap as JSON}" ] 

        controller.request.contentType = "text/json"      
        controller.save() 
        
        def content = controller.response.contentAsString
        assertNotNull content
        
        def result = JSON.parse( content )
        assertFalse result?.success
        
        assertTrue "Message was not expected: ${result?.message}", 
                    result?.message ==~ /.*Foo cannot be saved, as it contains errors.*/
        assertTrue "Errors were not expected: ${result?.errors}", 
                    result?.errors ==~ /.*The foo code is too long, it must be no more than 2 characters.*/                     
    }
    
    
    void testUpdateWithJSON() {
        def entity = new Foo( newFooParamsWithAuditTrailProperties() )
        save entity
        int version = entity.version
        def code = "JJ"
        params = [ data: "${((newFooParams() + [ id: entity.id, version: entity.version, code: code ]) as JSON)}" ]
        controller.request.contentType = "application/json"
        controller.update()
    
        def content = controller.response.contentAsString
        assertNotNull content

        def result = JSON.parse( content )
        assertTrue result?.success

        assertNotNull result?.message // TODO: assert correct localized message
        
        entity = Foo.get( entity.id )
        assertEquals code, entity.code
        assertEquals version + 1, entity.version
    }
    
    
    void testUpdateNotFound() {
        params = [ data: "${((newFooParams() + [ id: -66666 ]) as JSON)}" ] // notice we send json as well as receive json      
          
        controller.request.contentType = "application/json"
        controller.update()
    
        def content = controller.response.contentAsString
        assertNotNull content
      
        def result = JSON.parse( content )
        assertFalse result?.success
        assertTrue "Message was not expected: ${result?.message}", result?.message ==~ /.*not found.*/
    }


    void testOptimisticLock() { 
        def entity = new Foo( newFooParamsWithAuditTrailProperties() )
        save entity
        int version = entity.version
        
        // Now we'll issue a SQL update statement to adjust the version column
        executeUpdateSQL "update STVCOLL set STVCOLL_VERSION = 999 where STVCOLL_SURROGATE_ID = ?", entity.id
    
        def description = "This better fail" // due to optimistic lock exception
        params = [ data: "${(newFooParams() + [ id: entity.id, version: entity.version, description: description ]) as JSON}" ] 
        
        controller.request.contentType = "application/json"
        controller.update() 
                
        def content = controller.response.contentAsString
        assertNotNull content
        
        def result = JSON.parse( content )
        assertFalse result?.success
        
        JsonHelper.replaceJSONObjectNULL( result ) // Minimal workaround for Jira Grails-5585
        assertNull result?.errors           
        
        assertTrue "Message was not expected: ${result?.message}", 
                   result?.message ==~ /.*Another user has updated this Foo while you were editing.*/
        
        assertTrue "Errors were not expected: ${result?.internalError}", 
                   result?.underlyingErrorMessage ==~ /.*optimistic locking failed.*/ // underlying hibernate error message, not presentable to user...
    }


    void testOptimisticLockFailuresTrumpValidationFailures() { 
        def entity = new Foo( newFooParamsWithAuditTrailProperties() )
        save entity
        int version = entity.version
        
        // Now we'll issue a SQL update statement to adjust the version column
        executeUpdateSQL "update STVCOLL set STVCOLL_VERSION = 999 where STVCOLL_SURROGATE_ID = ?", entity.id
    
        // Should fail with optimistic lock even though it would also fail validation due to code being set to null
        params = [ data: "${[ id: entity.id, code: '', version: entity.version ] as JSON}" ] 
        
        controller.request.contentType = "application/json"
        controller.update() 
        
        def content = controller.response.contentAsString
        assertNotNull content
        
        def result = JSON.parse( content )
        assertFalse result?.success
        
        JsonHelper.replaceJSONObjectNULL( result ) // Minimal workaround for Jira Grails-5585
        assertNull result?.errors
        
        assertTrue "Message was not expected: ${result?.message}", 
                   result?.message ==~ /.*Another user has updated this Foo while you were editing.*/
        assertTrue "Errors were not expected: ${result?.internalError}", 
                   result?.underlyingErrorMessage ==~ /.*optimistic locking failed.*/ // underlying hibernate error message, TODO: Make presentable to user...
    }
    
    
    void testDeleteUsingJson() {
        def entity = new Foo( newFooParamsWithAuditTrailProperties() )
        save entity
        params = [ data: "${((newFooParams() + [ id: entity.id, version: entity.version ]) as JSON)}" ]
        controller.request.contentType = "application/json"
        controller.delete()
    
        def content = controller.response.contentAsString
        assertNotNull content

        def result = JSON.parse( content )
        assertTrue "Expected success to be true but found results = $result", result?.success
        
        assertNull Foo.get( entity.id )
    }
   
    
    void testListWithJSON() {
        def MAX = 15 
        params = [ max: MAX ]  
        
        controller.request.contentType = "application/json"
        controller.list() 
         
        def result = JSON.parse( controller.response.contentAsString )
        assertTrue "Found ${result?.data?.size()} but expected $MAX", result?.data?.size() == MAX 
        assertTrue "Total count expected to be ${Foo.count()} but was ${result?.totalCount}", result?.totalCount == Foo.count()
        
        // Next we'll reconstitute Foo model instances from the JSON

        // The following fails when 'unit' tests are run alongside of integration tests. The exception is a 'null' violation 
        // due to the lastModified date not being set. The underlying exception is a JSON converter exception due to not being 
        // able to parse the date format. The Config.groovy sets grails.converters.json.date = "javascript", however when 
        // unit tests are run this setting is apparently not effective. 
        // 
// For now, this isn't integral to the framework plugin testing, so we'll comment out so all tests can run together.
// This test exists within banner_on_grails (e.g., CollegeControllerIntegrationTests) which forces unit and integration tests to be 
// executed separately. 
/*        result.data.each { 
            JsonHelper.replaceJSONObjectNULL( it ) // Minimal workaround for Jira Grails-5585
            def foo = new Foo( it )
            if (!foo.validate()) {
                def message
                foo.errors.allErrors.each {
                    message += "${it}\n"
                }
                fail( "Validation errors: $message" )
            }
        }           
*/
    }


    // Not really a 'controller' test, this tests that we can see pending creates in the
    // current transaction and that we cannot see pending creates in a different transaction.
    // We'll put this here as we need the full database environment versus a unit test.
    void testJoiningTransaction() {
        Connection conn = null
        def sql = null
        def found = "I better be set to null by otherThread!"
        def otherThread
    
        try {
            Foo.withTransaction {
                def foo = new Foo( newFooParamsWithAuditTrailProperties() ).save( flush: true )
    
                // Now try from a connection in the same thread...
                conn = sessionFactory.getCurrentSession().connection()
                sql = new Sql( conn )
                def row = sql.firstRow("select STVCOLL_CODE from saturn.STVCOLL where STVCOLL_CODE = 'TT'")
                assertEquals("Expected code 'TT' but found code ${row?.STVCOLL_CODE}", "TT", row?.STVCOLL_CODE)
    
                // and lastly ensure we can't see it using a connnection from another thread...
                otherThread = new Thread( {  // we'll use a closure to get a connection via a different thread
                    com.sungardhe.banner.security.FormContext.set( ['STVCOLL'] )
                    login()
                    def sqlB = null
                    try {
                        sqlB = new Sql( dataSource )
                        found = sqlB.firstRow( "select STVCOLL_CODE from saturn.STVCOLL where STVCOLL_CODE = 'TT'" )
                    } finally {
                        sqlB?.close()
                    }
                })
                otherThread.start()
            }
        } finally {
            sql?.close()
            // note we don't close the connection - the test framework will perform a rollback and then close
        }
    
        otherThread.join()
        assertNull( found )
    }
    
    
    // NOTICE: A 'functional' test is used to test the XML interface (as it relies on GSP processing that is beyond the scope of an integration test)
    
 
    // NOTICE: Depends on seed data having pidm 1627 and having at least one medical condition and disability record each with surrogate key 1
    // Creates params appropriate for the controller
    private Map newFooParams() {
        [ code: "TT", description: "TT", addressStreetLine1: "TT", addressStreetLine2: "TT", addressStreetLine3: "TT", addressCity: "TT",
		  addressState: "TT", addressCountry: "TT", addressZipCode: "TT", systemRequiredIndicator: "N", voiceResponseMessageNumber: 1, statisticsCanadianInstitution: "TT", 
		  districtDivision: "TT", houseNumber: "TT", addressStreetLine4: "TT" ]
    }
    
 
    // Includes all properties so that the foo is valid and can be saved directly via GORM versus going through the service
    private Map newFooParamsWithAuditTrailProperties() {
        (newFooParams() + [ lastModified: new Date(), lastModifiedBy: 'horizon', dataOrigin: 'horizon' ])
    }

}


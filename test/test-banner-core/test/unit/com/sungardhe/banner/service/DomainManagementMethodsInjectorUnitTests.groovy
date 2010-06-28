/** *****************************************************************************

 © 2010 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
package com.sungardhe.banner.service

import grails.test.GrailsUnitTestCase

import org.codehaus.groovy.grails.commons.ConfigurationHolder

import org.hibernate.Session // we mock this within this test

import org.springframework.security.core.context.SecurityContextHolder


/**
 * Integration test for the DomainManagementMethodsInjector.
 **/
class DomainManagementMethodsInjectorUnitTests extends GrailsUnitTestCase {
    
    def myService           // assigned in setUp method
    List serviceCallbacks   // assigned in setUp method
    
    
    protected void setUp() {
        super.setUp()
        DomainManagementMethodsInjector.injectDataManagement( NonOverridingTestService, MyMock )        
        myService = new NonOverridingTestService() // all methods are the injected ones
        
        // This test injects 'test' callback handlers to test a service's ability to receive pre/post create/update/delete 
        // callbacks from it's injected CRUD method implementations. Services may implement these callbacks to provide 
        // additional behavior to be executed before and after the 'normal' injected CRUD implementations.                                                                         
        // Since 'these' specific callbacks are only for testing purposes versus 'real' behavior needed to support College, 
        // they are injected here by this test.                              
        serviceCallbacks = new ArrayList().asSynchronized() 
        injectTestCallbacks( myService )  

        def myMockedModels = []
        (0..4).each { myMockedModels << new MyMock( name: "Mocked_$it" ) }
        mockDomain( MyMock, myMockedModels )
        
        // We'll mock the session and just return the number of times we've called a method on it...                      
        MyMock.metaClass.static.withSession = { Closure closure ->
            def sessionMock = [:].asType( Session )
            sessionMock.metaClass.invokeMethod { String name, args -> } 
            closure( sessionMock )
        }
    }
    
    
    // --------------------------- Test 'create' method ----------------------------
    
    
    void testCreateUsingModelInstance() {
        def svc = new AnotherTestService()
        DomainManagementMethodsInjector.injectDataManagement( svc, MyMock )        
        def createdDomain = svc.create( new MyMock( newMyMockParams() ) )
        assertNotNull createdDomain?.id 
    }
    
    
    void testCreateUsingModelProperties() { 
        def svc = new AnotherTestService()
        DomainManagementMethodsInjector.injectDataManagement( svc, MyMock )        
        def createdDomain = svc.create( new MyMock( newMyMockParams() ).properties ) // map will have a 'class' key, that can be confused with model.class
        assertNotNull createdDomain?.id 
    }
    
    
    void testCreateUsingParamsMap() {
        def svc = new AnotherTestService()
        DomainManagementMethodsInjector.injectDataManagement( svc, MyMock )        
        def createdDomain = svc.create( newMyMockParams() ) // map doesn't contain the 'class' key -- it is a 'params' map
        assertNotNull createdDomain?.id 
    }
    
    
    void testCreateUsingMapHavingDomainModelKeyHoldingModelInstance() { 
        def svc = new AnotherTestService()
        DomainManagementMethodsInjector.injectDataManagement( svc, MyMock )        
        def createdDomain = svc.create( [ domainModel: new MyMock( newMyMockParams() ), otherJunk: 'Some validation stuff maybe?' ] )
        assertNotNull createdDomain?.id 
    }


    void testCreateUsingMapHavingDomainModelClassPropertyNameKeyHoldingModelInstance() {  // how's that for a name ;-)
        def svc = new AnotherTestService()
        DomainManagementMethodsInjector.injectDataManagement( svc, MyMock )
        def createdDomain = svc.create( [ myMock: new MyMock( newMyMockParams() ), otherJunk: 'Some validation stuff maybe?' ] )
        assertNotNull createdDomain?.id
    }

    
    void testCreateUsingModelInstanceAndNotFlushingSession() { 
        // actually, can't really test that via mocking, but we'll at least test signatures here and 
        // implement 'real' testing to the FooServiceIntegrationTests. The below is more or less, documentation. 
        def svc = new AnotherTestService()
        DomainManagementMethodsInjector.injectDataManagement( svc, MyMock )        
        def createdDomains = [ svc.create( new MyMock( newMyMockParams() ), false ), // yup, looks like we can pass a boolean
                               svc.create( new MyMock( newMyMockParams() ), false ), // without getting a methodMissing exception
                               svc.create( new MyMock( newMyMockParams() ), false ) ]
        // since the mocking library won't delegate to the session (as it isn't even mocked normally),
        // all we'll really be able to do here is ensure we don't get a method missing exception... 
        // Ideally, the mocking will become more sophisticated to handle flush: true. 
        svc.flush()
    }


    // --------------------------- Test batch 'create' invocations ----------------------------


    void testBatchCreateUsingModelInstance() {
        def svc = new AnotherTestService()
        DomainManagementMethodsInjector.injectDataManagement( svc, MyMock )
        List models = []
        (0..4).each{ models << new MyMock( name: "Mock_$it", description: "MockDesc_$it" ) }
        def createdDomains = svc.create( models )
        assertEquals 5, createdDomains.size()
        assertTrue  createdDomains.every { it.id }
    }


    void testBatchCreateUsingModelProperties() {
        def svc = new AnotherTestService()
        DomainManagementMethodsInjector.injectDataManagement( svc, MyMock )
        List models = []
        (0..4).each{ models << new MyMock( newMyMockParams( it ) ).properties }
        def createdDomains = svc.create( models )
        assertEquals 5, createdDomains.size()
        assertTrue  createdDomains.every { it.id }
    }


    void testBatchCreateUsingParamsMap() {
        def svc = new AnotherTestService()
        DomainManagementMethodsInjector.injectDataManagement( svc, MyMock )
        List models = []
        (0..4).each { models << newMyMockParams( it ) + [ someExtraInfoNeededForValidation: "I need this in my callback!" ] }

        def createdDomains = svc.create( models )
        assertEquals 5, createdDomains.size()
        assertTrue  createdDomains.every { it.id }
    }


    void testBatchCreateUsingMapHavingDomainModelKeyHoldingModelInstance() {
        def svc = new AnotherTestService()
        DomainManagementMethodsInjector.injectDataManagement( svc, MyMock )

        List models = []
        (0..4).each { models << [ domainModel: new MyMock( newMyMockParams( it ) ), otherJunk: 'Some validation stuff maybe?' ] }

        def createdDomains = svc.create( models )
        assertEquals 5, createdDomains.size()
        assertTrue  createdDomains.every { it.id }
    }


    void testBatchCreateUsingMapHavingDomainModelClassPropertyNameKeyHoldingModelInstance() {
        def svc = new AnotherTestService()
        DomainManagementMethodsInjector.injectDataManagement( svc, MyMock )

        List models = []
        (0..4).each { models << [ myMock: new MyMock( newMyMockParams( it) ), otherJunk: 'Some validation stuff maybe?' ] }

        def createdDomains = svc.create( models )
        assertEquals 5, createdDomains.size()
        assertTrue  createdDomains.every { it.id }
    }

    
    // --------------------------- Test 'update' method ----------------------------
    
    
    void testUpdateUsingModelInstance() {
        def existingModel = MyMock.findByName( 'Mocked_1' )
        existingModel.description = "Updated"
        def svc = new AnotherTestService()
        DomainManagementMethodsInjector.injectDataManagement( svc, MyMock )        
        def updatedDomain = svc.update( existingModel )
        assertNotNull updatedDomain.id 
        assertEquals "Updated", updatedDomain.description
    }
    
    
    void testUpdateUsingModelProperties() {
        def existingModel = MyMock.findByName( 'Mocked_1' )
        existingModel.description = "Updated"
        def svc = new AnotherTestService()
        DomainManagementMethodsInjector.injectDataManagement( svc, MyMock )        
        def updatedDomain = svc.update( existingModel.properties )
        assertNotNull updatedDomain.id 
        assertEquals "Updated", updatedDomain.description
    }
    
    
    void testUpdateUsingParamsMap() {
        def existingModel = MyMock.findByName( 'Mocked_1' )
        def svc = new AnotherTestService()
        DomainManagementMethodsInjector.injectDataManagement( svc, MyMock )        
        def updatedDomain = svc.update( [ id: existingModel.id, name: existingModel.name, 
                                          description: "Updated", version: existingModel.version ] )
        assertNotNull updatedDomain.id 
        assertEquals "Updated", updatedDomain.description
    }
    
    
    void testUpdateUsingMapHavingDomainModelKeyHoldingModelInstance() {
        def existingModel = MyMock.findByName( 'Mocked_1' )
        existingModel.description = "Updated"
        def svc = new AnotherTestService()
        DomainManagementMethodsInjector.injectDataManagement( svc, MyMock )        
        def updatedDomain = svc.update( [ domainModel: existingModel, otherJunk: 'Some validation stuff maybe?' ] )
        assertNotNull updatedDomain.id 
        assertEquals "Updated", updatedDomain.description
    }


    void testUpdateUsingMapHavingDomainModelClassPropertyNameKeyHoldingModelInstance() {
        def existingModel = MyMock.findByName( 'Mocked_1' )
        existingModel.description = "Updated"
        def svc = new AnotherTestService()
        DomainManagementMethodsInjector.injectDataManagement( svc, MyMock )
        def updatedDomain = svc.update( [ myMock: existingModel, otherJunk: 'Some validation stuff maybe?' ] )
        assertNotNull updatedDomain.id
        assertEquals "Updated", updatedDomain.description
    }


    // --------------------------- Test batch 'update' invocations ----------------------------


    void testBatchUpdateUsingModelInstance() {
        def svc = new AnotherTestService()
        DomainManagementMethodsInjector.injectDataManagement( svc, MyMock )

        def existingModels = MyMock.list()
        existingModels.eachWithIndex { model, i -> model.description = "Updated_$i" }

        def createdDomains = svc.update( existingModels )
        assertEquals 5, createdDomains.size()
        assertTrue createdDomains.every { it.id }
        assertTrue createdDomains.every { it.description ==~ /.*Updated.*/ }
    }


    void testBatchUpdateUsingModelProperties() {
        def svc = new AnotherTestService()
        DomainManagementMethodsInjector.injectDataManagement( svc, MyMock )

        def existingModels = MyMock.list()
        def existingProperties = []
        existingModels.eachWithIndex { model, i -> existingProperties << (model.properties + [ description: "Updated_$i" ]) }

        def createdDomains = svc.update( existingProperties )
        assertEquals 5, createdDomains.size()
        assertTrue  createdDomains.every { it.id }
        assertTrue createdDomains.every { it.description ==~ /.*Updated.*/ }
    }


    void testBatchUpdateUsingParamsMap() {
        def svc = new AnotherTestService()
        DomainManagementMethodsInjector.injectDataManagement( svc, MyMock )

        def existingModels = MyMock.list()
        def existingParams = []
        existingModels.eachWithIndex { model, i -> existingParams << [ id: model.id, name: model.name,
                                                                       description: "Updated_$i", version: model.version ] }
        def createdDomains = svc.update( existingParams )
        assertEquals 5, createdDomains.size()
        assertTrue  createdDomains.every { it.id }
        assertTrue createdDomains.every { it.description ==~ /.*Updated.*/ }
    }


    void testBatchUpdateUsingMapHavingDomainModelKeyHoldingModelInstance() {
        def svc = new AnotherTestService()
        DomainManagementMethodsInjector.injectDataManagement( svc, MyMock )

        def existingModels = MyMock.list()
        def existingModelsInMapValues = []
        existingModels.eachWithIndex { model, i ->
            model.description = "Updated_$i"
            existingModelsInMapValues << [ domainModel: model, otherJunk: 'Some validation stuff maybe?' ]
        }

        def createdDomains = svc.update( existingModelsInMapValues )
        assertEquals 5, createdDomains.size()
        assertTrue  createdDomains.every { it.id }
        assertTrue createdDomains.every { it.description ==~ /.*Updated.*/ }
    }


    void testBatchUpdateUsingMapHavingDomainModelClassPropertyNameKeyHoldingModelInstance() {
        def svc = new AnotherTestService()
        DomainManagementMethodsInjector.injectDataManagement( svc, MyMock )

        def existingModels = MyMock.list()
        def existingModelsInMap = []
        existingModels.eachWithIndex { model, i ->
            model.description = "Updated_$i"
            existingModelsInMap << [ myMock: model, otherJunk: 'Some validation stuff maybe?' ]  
        }

        def createdDomains = svc.update( existingModelsInMap )
        assertEquals 5, createdDomains.size()
        assertTrue  createdDomains.every { it.id }
        assertTrue createdDomains.every { it.description ==~ /.*Updated.*/ }
    }


    // --------------------------- Test batch 'createOrUpdate' invocations ----------------------------


    void testBatchCreateOrUpdateUsingModelInstance() {
        def svc = new AnotherTestService()
        DomainManagementMethodsInjector.injectDataManagement( svc, MyMock )

        def requestList = []
        (0..4).each{ requestList << new MyMock( name: "Mock_$it", description: "MockDesc_$it" ) }

        def existingModels = MyMock.list()
        existingModels.eachWithIndex { model, i -> requestList << (model.properties + [ description: "Updated_$i" ]) }

        def result = svc.createOrUpdate( requestList )
        assertEquals 10, result.size()
        assertTrue result.every { it.id }
result.each { println "XXXXXXXXXX $it" } 
        assertEquals 5, result.findAll { it.description.contains( "Updated" ) }.size()
    }


    void testBatchCreateOrUpdateUsingModelProperties() {
        def svc = new AnotherTestService()
        DomainManagementMethodsInjector.injectDataManagement( svc, MyMock )

        def requestList = []
        (0..4).each{ requestList << new MyMock( name: "Mock_$it", description: "MockDesc_$it" ) }

        def existingModels = MyMock.list()
        existingModels.eachWithIndex { model, i -> requestList << (model.properties + [ description: "Updated_$i" ]) }

        def result = svc.createOrUpdate( requestList )
        assertEquals 10, result.size()
        assertTrue  result.every { it.id }
        assertEquals 5, result.findAll { it.description.contains( "Updated" ) }.size()
    }


    void testBatchCreateOrUpdateUsingParamsMap() {
        def svc = new AnotherTestService()
        DomainManagementMethodsInjector.injectDataManagement( svc, MyMock )

        def requestList = []
        (0..4).each{ requestList << new MyMock( name: "Mock_$it", description: "MockDesc_$it" ) }

        def existingModels = MyMock.list()
        existingModels.eachWithIndex { model, i -> requestList << [ id: model.id, name: model.name,
                                                                       description: "Updated_$i", version: model.version ] }

        def result = svc.createOrUpdate( requestList )
        assertEquals 10, result.size()
        assertTrue  result.every { it.id }
        assertEquals 5, result.findAll { it.description.contains( "Updated" ) }.size()
    }


    void testBatchCreateOrUpdateUsingMapHavingDomainModelKeyHoldingModelInstance() {
        def svc = new AnotherTestService()
        DomainManagementMethodsInjector.injectDataManagement( svc, MyMock )

        def requestList = []
        (0..4).each{ requestList << new MyMock( name: "Mock_$it", description: "MockDesc_$it" ) }

        def existingModels = MyMock.list()
        existingModels.eachWithIndex { model, i ->
            model.description = "Updated_$i"
            requestList << [ domainModel: model, otherJunk: 'Some validation stuff maybe?' ]
        }

        def result = svc.createOrUpdate( requestList )
        assertEquals 10, result.size()
        assertTrue  result.every { it.id }
        assertEquals 5, result.findAll { it.description.contains( "Updated" ) }.size()
    }


    void testBatchCreateOrUpdateUsingMapHavingDomainModelClassPropertyNameKeyHoldingModelInstance() {
        def svc = new AnotherTestService()
        DomainManagementMethodsInjector.injectDataManagement( svc, MyMock )

        def requestList = []
        (0..4).each{ requestList << new MyMock( name: "Mock_$it", description: "MockDesc_$it" ) }

        def existingModels = MyMock.list()
        existingModels.eachWithIndex { model, i ->
            model.description = "Updated_$i"
            requestList << [ myMock: model, otherJunk: 'Some validation stuff maybe?' ]
        }

        def result = svc.createOrUpdate( requestList )
        assertEquals 10, result.size()
        assertTrue  result.every { it.id }
        assertEquals 5, result.findAll { it.description.contains( "Updated" ) }.size()
    }

    
    // --------------------------- Test 'delete' method ----------------------------
    
    
    void testDeleteUsingModelInstance() {
        def existingModel = MyMock.findByName( 'Mocked_1' )
        def svc = new AnotherTestService()
        DomainManagementMethodsInjector.injectDataManagement( svc, MyMock )        
        assertTrue svc.delete( existingModel )
        assertNull MyMock.findByName( 'Mocked_1' )
    }
    
    
    void testDeleteUsingModelProperties() { 
        def existingModel = MyMock.findByName( 'Mocked_1' )
        def svc = new AnotherTestService()
        DomainManagementMethodsInjector.injectDataManagement( svc, MyMock )        
        assertTrue svc.delete( existingModel.properties )
        assertNull MyMock.findByName( 'Mocked_1' )
    }
    
    
    void testDeleteUsingParamsMap() {
        def existingModel = MyMock.findByName( 'Mocked_1' )
        def svc = new AnotherTestService()
        DomainManagementMethodsInjector.injectDataManagement( svc, MyMock )        
        assertTrue svc.delete( [ id: existingModel.id, name: existingModel.name, 
                                 description: existingModel.description, version: existingModel.version ] )
        assertNull MyMock.findByName( 'Mocked_1' )
    }
    
    
    void testDeleteUsingMapHavingDomainModelKeyHoldingModelInstance() {
        def existingModel = MyMock.findByName( 'Mocked_1' )
        def svc = new AnotherTestService()
        DomainManagementMethodsInjector.injectDataManagement( svc, MyMock )
        assertTrue svc.delete( [ domainModel: existingModel, otherJunk: 'Some validation stuff maybe?' ] )
        assertNull MyMock.findByName( 'Mocked_1' )
    }
    
    
    void testDeleteUsingLongId() {
        def existingModel = MyMock.findByName( 'Mocked_1' )
        def svc = new AnotherTestService()
        DomainManagementMethodsInjector.injectDataManagement( svc, MyMock )        
        assertTrue svc.delete( (Long) existingModel.id )
        assertNull MyMock.findByName( 'Mocked_1' )
    }
    
    
    void testDeleteUsingNumber() {
        def svc = new AnotherTestService()
        DomainManagementMethodsInjector.injectDataManagement( svc, MyMock )        
        assertTrue svc.delete( 1 )
        assertNull MyMock.get( 1 )
    }
    
    
    void testDeleteUsingPrimativeId() {
        def existingModel = MyMock.findByName( 'Mocked_1' )
        def svc = new AnotherTestService()
        DomainManagementMethodsInjector.injectDataManagement( svc, MyMock )        
        assertTrue svc.delete( (long) existingModel.id )
        assertNull MyMock.findByName( 'Mocked_1' )
    }
    
    
    void testDeleteUsingString() {
        def svc = new AnotherTestService()
        DomainManagementMethodsInjector.injectDataManagement( svc, MyMock )        
        assertTrue svc.delete( '1' )
        assertNull MyMock.get( 1 )
    }


    // --------------------------- Test batch 'delete' invocations ----------------------------


    void testBatchDeleteUsingModelInstance() {
        def svc = new AnotherTestService()
        DomainManagementMethodsInjector.injectDataManagement( svc, MyMock )

        def existingModels = MyMock.list()
        svc.delete( existingModels )
        assertNull MyMock.findByName( 'Mocked_1' )
        assertTrue MyMock.list()?.size() == 0
    }


    void testBatchDeleteUsingModelProperties() {
        def svc = new AnotherTestService()
        DomainManagementMethodsInjector.injectDataManagement( svc, MyMock )

        def existingModels = MyMock.list()
        def existingProperties = []
        existingModels.eachWithIndex { model, i -> existingProperties << (model.properties + [ description: "Updated_$i" ]) }
        svc.delete( existingProperties )
        assertNull MyMock.findByName( 'Mocked_1' )
        assertTrue MyMock.list()?.size() == 0
    }


    void testBatchDeleteUsingParamsMap() {
        def svc = new AnotherTestService()
        DomainManagementMethodsInjector.injectDataManagement( svc, MyMock )

        def existingModels = MyMock.list()
        def existingParams = []
        existingModels.eachWithIndex { model, i -> existingParams << [ id: model.id, name: model.name,
                                                                       description: model.description, version: model.version ] }
        svc.delete( existingParams )
        assertNull MyMock.findByName( 'Mocked_1' )
        assertTrue MyMock.list()?.size() == 0
    }


    void testBatchDeleteUsingMapHavingDomainModelKeyHoldingModelInstance() {
        def svc = new AnotherTestService()
        DomainManagementMethodsInjector.injectDataManagement( svc, MyMock )

        def existingModels = MyMock.list()
        def existingModelsInMapValues = []
        existingModels.each { model ->
            existingModelsInMapValues << [ domainModel: model, otherJunk: 'Some validation stuff maybe?' ]
        }
        svc.delete( existingModelsInMapValues )
        assertNull MyMock.findByName( 'Mocked_1' )
        assertTrue MyMock.list()?.size() == 0
    }


    void testBatchDeleteUsingLongIds() {
        def svc = new AnotherTestService()
        DomainManagementMethodsInjector.injectDataManagement( svc, MyMock )

        def existingModels = MyMock.list()
        def existingIds = []
        existingModels.each { model -> existingIds << (Long) model.id }
        svc.delete( existingIds )
        assertNull MyMock.findByName( 'Mocked_1' )
        assertTrue MyMock.list()?.size() == 0
    }


    void testBatchDeleteUsingPrimativeId() {
        def svc = new AnotherTestService()
        DomainManagementMethodsInjector.injectDataManagement( svc, MyMock )

        def existingModels = MyMock.list()
        def existingIds = []
        existingModels.each { model -> existingIds << (long) model.id }
        svc.delete( existingIds )
        assertNull MyMock.findByName( 'Mocked_1' )
        assertTrue MyMock.list()?.size() == 0
    }


    void testBatchDeleteUsingStrings() {
        def svc = new AnotherTestService()
        DomainManagementMethodsInjector.injectDataManagement( svc, MyMock )

        def existingModels = MyMock.list()
        def existingStringIds = []
        existingModels.each { model -> existingStringIds << Long.toString( model.id ) }
        svc.delete( existingStringIds )
        assertNull MyMock.findByName( 'Mocked_1' )
        assertTrue MyMock.list()?.size() == 0
    }

    
    // --------------------------- Test method callbacks ----------------------------
    
    
    void testCreateCallbacks() { 
        assertTrue serviceCallbacks.size() == 0
        myService.create( newMyMockParams() )
        assertTrue serviceCallbacks.containsAll( [ 'preCreate', 'postCreate' ] ) && serviceCallbacks.size() == 2
    }
    
    
    void testCreateExplicitMethodCallback() { 
        def svc = new AnotherWithCallbacksTestService()
        DomainManagementMethodsInjector.injectDataManagement( svc, MyMock )        
        svc.create( newMyMockParams() )        
        assertTrue svc.preCreateCalled
    }
    
        
    void testUpdateCallbacks() { 
        assertTrue serviceCallbacks.size() == 0
        myService.update( newMyMockParams() + [ id: 1, description: "Updated", version: 0 ] )
        assertTrue serviceCallbacks.containsAll( [ 'preUpdate', 'postUpdate' ] ) && serviceCallbacks.size() == 2
    }
    
    
    void testCreateOrUpdateWithCallbacks() { 
        assertTrue serviceCallbacks.size() == 0
        def createdDomain = myService.create( newMyMockParams() )
        assertNotNull createdDomain?.id
        assertTrue serviceCallbacks.containsAll( [ 'preCreate', 'postCreate' ] ) && serviceCallbacks.size() == 2
        
        // now we'll issue the same and expect an update
        createdDomain.description = "Updated" // note the update callback handler asserts this specific content
        assertTrue serviceCallbacks?.size() == 2
        def updatedDomain = myService.update( createdDomain ) 
        assertEquals createdDomain.id, updatedDomain?.id
        assertTrue serviceCallbacks.containsAll( [ 'preUpdate', 'postUpdate' ] ) && serviceCallbacks.size() == 4
    }
    
    
    void testDeleteCallbacks() { 
        assertTrue serviceCallbacks.size() == 0
        myService.delete( 1 )
        assertTrue serviceCallbacks.containsAll( [ 'preDelete', 'postDelete' ] ) && serviceCallbacks.size() == 2
    }
    
    
    void testCreateOverride() { 
        DomainManagementMethodsInjector.injectDataManagement( OverridingCreateTestService, MyMock )
        def testService = new OverridingCreateTestService()
        assertFalse testService.createInvoked
        testService.create( newMyMockParams() )
        assertTrue testService.createInvoked
        
        // now we'll test again, this time ensuring we can pass a domain instance versus a map
        testService = new OverridingCreateTestService()
        assertFalse testService.createInvoked
        testService.create( new MyMock( newMyMockParams() ) )
        assertTrue testService.createInvoked
    }
    
    
    void testUpdateOverride() { 
        DomainManagementMethodsInjector.injectDataManagement( OverridingUpdateTestService, MyMock )
        def testService = new OverridingUpdateTestService()
        
        def entity = testService.create( newMyMockParams() )
        assertTrue entity.id > 0  // this tests that a create method was successfully injected
        assertNotNull MyMock.get( entity.id )
        
        assertFalse testService.updateInvoked
        testService.update( newMyMockParams() + [ id: entity.id, version: entity.version, name: "This won't be updated" ] )
        assertTrue testService.updateInvoked  // this tests that we were able to implement the update, but not the create
        assertEquals "Test", entity.name      // and further tests that the injected update was not used
        
        testService.delete( entity.id )
        assertNull MyMock.get( entity.id )
    }
    
    
    void testDeleteOverride() { 
        DomainManagementMethodsInjector.injectDataManagement( OverridingDeleteTestService, MyMock )
        def testService = new OverridingDeleteTestService()
        
        def entity = testService.create( newMyMockParams() )
        assertTrue entity.id > 0  // this tests that the create method was injected
        assertNotNull MyMock.get( entity.id )
        
        testService.update( newMyMockParams() + [ id: entity.id, version: entity.version, name: "Update Me" ] )
        assertEquals "Update Me", entity.name // this tests that the update method was injected
        
        assertFalse testService.deleteInvoked
        testService.delete( entity.id )
        assertTrue testService.deleteInvoked  // this tests that the delete method was 'not' injected
        assertNotNull MyMock.get( entity.id )
    }


    // ------------------------------ Helper Methods ---------------------------------
    
    
    private Map newMyMockParams( index = null ) {
        def name = index ? "Test_$index" : "Test"
        def desc = index ? "Desc_$index" : "Description"
        [ name: name, description: desc ]  + [ someExtraInfoNeededForValidation: "I need this in my callback!" ]
    }
        
    
    // This injects 'test' callbacks into the service, so that the injected CRUD methods of that service can provide callbacks.
    // The specific callback handlers simply track the callbacks for testing purposes. Normally, these would be implemented 
    // within the service directly (but since these are 'test only' callbacks, they are injected here).
    //
    private def injectTestCallbacks( service ) {
        // We'll inject some pre and post CRUD behavior into our service to test service callbacks (this test is NOT College specific)
        service.metaClass.preCreate = { domainObjectOrParams -> 
            registerCallback 'preCreate'
            assertNull domainObjectOrParams.id
            assertEquals "Test", domainObjectOrParams.name
            assertEquals "I need this in my callback!", domainObjectOrParams['someExtraInfoNeededForValidation']
        }
        service.metaClass.postCreate = { results -> 
            registerCallback 'postCreate'
            assertNotNull results.before
            assertNull results.before.id
            assertNotNull results.after.id
        }
        service.metaClass.preUpdate = { domainObjectOrParams -> 
            registerCallback 'preUpdate'
            assertNotNull domainObjectOrParams
        }
        service.metaClass.postUpdate = { results -> 
            registerCallback 'postUpdate'
            assertNotNull results.before
            assertEquals "Updated", results.after.description
        }
        service.metaClass.preDelete = { domainObjectOrParams -> 
            registerCallback 'preDelete'
            assertNotNull domainObjectOrParams
        }
        service.metaClass.postDelete = { results -> 
            registerCallback 'postDelete'
            assertNotNull results.before
            assertNull results.after
        }        
    }
    
    
    private registerCallback( String callBackName ) {
        serviceCallbacks << callBackName
    }

}


class MyMock {
    Long id
    long version
    
    def name
    def description
    def dataOrigin
    def lastModifiedBy
    def lastModified

    
    public static MyMock get( id ) {
        new MyMock( id: id, name: 'Mocked' )
    }

    public boolean isDirty() { true } // we'll 'always' be dirty
    public List getDirtyPropertyNames() { new ArrayList() }

    public String toString() {
        "${super.toString()}, id=$id, name=$name, description=$description, version=$version"
    }
}


class NonOverridingTestService {
    
    boolean transactional = true       // Grails won't find this service, so this is here 'just because services should have this'
    static defaultCrudMethods = true   // we'll need to inject this explicitly here, so this is 'just to follow our convention'    
}


class OverridingCreateTestService {
    
    boolean transactional = true       // Grails won't find this service, so this is here 'just because services should have this'
    static defaultCrudMethods = true   // we'll need to inject this explicitly here, so this is 'just to follow our convention'    
    boolean createInvoked
    
    
    def create( college ) {
        createInvoked = true
    }
}


class OverridingUpdateTestService {
    
    boolean transactional = true       // Grails won't find this service, so this is here 'just because services should have this'
    static defaultCrudMethods = true   // we'll need to inject this explicitly here, so this is 'just to follow our convention'    
    boolean updateInvoked
    
    def update( college ) {
        updateInvoked = true
    }    
}


class OverridingDeleteTestService {
    
    boolean transactional = true       // Grails won't find this service, so this is here 'just because services should have this'
    static defaultCrudMethods = true   // we'll need to inject this explicitly here, so this is 'just to follow our convention'    
    boolean deleteInvoked
    
    def delete( long id ) {
        deleteInvoked = true
    }
}


class AnotherTestService {

    boolean transactional = true       // Grails won't find this service, so this is here 'just because services should have this'
    static defaultCrudMethods = true   // we'll need to inject this explicitly here, so this is 'just to follow our convention'    
}


class AnotherWithCallbacksTestService {

    boolean transactional = true       // Grails won't find this service, so this is here 'just because services should have this'
    static defaultCrudMethods = true   // we'll need to inject this explicitly here, so this is 'just to follow our convention'    
    boolean preCreateCalled = false
    
    def preCreate( map ) {
        preCreateCalled = true
    }
}

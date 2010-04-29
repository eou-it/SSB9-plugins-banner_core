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

import org.springframework.security.context.SecurityContextHolder


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
        
        mockDomain( MyMock, [ new MyMock( name: 'First' ), new MyMock( name: 'Second' ) ] )
    }
    
    
    void testCreateUsingInstance() {
        def svc = new AnotherTestService()
        DomainManagementMethodsInjector.injectDataManagement( svc, MyMock )        
        def createdDomain = svc.create( new MyMock( newMyMockParams() ) )
        assertNotNull createdDomain?.id 
    }
    
    
    void testCreateUsingParams() {
        def svc = new AnotherTestService()
        DomainManagementMethodsInjector.injectDataManagement( svc, MyMock )        
        def createdDomain = svc.create( newMyMockParams() )
        assertNotNull createdDomain?.id 
    }
    
    
    void testCreateUsingParamsFromModelProperties() {
        def svc = new AnotherTestService()
        DomainManagementMethodsInjector.injectDataManagement( svc, MyMock )        
        def createdDomain = svc.create( new MyMock( newMyMockParams() ).properties ) // map will have a 'class' key, that can be confused with model.class
        assertNotNull createdDomain?.id 
    }
    
    
    void testCreateWithMapHavingDomainModelKeyWithInstance() { 
        def svc = new AnotherTestService()
        DomainManagementMethodsInjector.injectDataManagement( svc, MyMock )        
        def createdDomain = svc.create( [ domainModel: new MyMock( newMyMockParams() ), otherJunk: 'Some validation stuff maybe?' ] )
        assertNotNull createdDomain?.id 
    }
    
    
    void testCreateCallbacks() { 
        assertFalse serviceCallbacks.containsAll( [ 'preCreate', 'postCreate' ] ) && serviceCallbacks.size() == 2
        myService.create( newMyMockParams() )
        assertTrue serviceCallbacks.containsAll( [ 'preCreate', 'postCreate' ] ) && serviceCallbacks.size() == 2
    }
    
    
    void testCreateMethodCallback() { 
        def svc = new AnotherWithCallbacksTestService()
        DomainManagementMethodsInjector.injectDataManagement( svc, MyMock )        
        svc.create( newMyMockParams() )        
        assertTrue svc.preCreateCalled
    }
    
        
    void testUpdateCallbacks() { // with html
        assertFalse serviceCallbacks.containsAll( [ 'preUpdate', 'postUpdate' ] ) && serviceCallbacks.size() == 2
        myService.update( newMyMockParams() + [ id: 1, version: 0 ] )
        assertTrue serviceCallbacks.containsAll( [ 'preUpdate', 'postUpdate' ] ) && serviceCallbacks.size() == 2
    }
    
    
    void testDeleteCallbacks() { 
        assertFalse serviceCallbacks.containsAll( [ 'preDelete', 'postDelete' ] ) && serviceCallbacks.size() == 2
        myService.delete( 1 )
        assertTrue serviceCallbacks.containsAll( [ 'preDelete', 'postDelete' ] ) && serviceCallbacks.size() == 2
    }
    
    
    void testCreateOverride() { 
        DomainManagementMethodsInjector.injectDataManagement( OverridingCreateTestService, MyMock )
        def testService = new OverridingCreateTestService()
        assertFalse testService.createInvoked
        testService.create( newMyMockParams() )
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
    
    
    private Map newMyMockParams() {
        [ name: "Test" ] + [ someExtraInfoNeededForValidation: "I need this in my callback!" ]
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
        service.metaClass.postCreate = { domainObject -> 
            registerCallback 'postCreate'
            assertNotNull domainObject.id
        }
        service.metaClass.preUpdate = { domainObjectOrParams -> 
            registerCallback 'preUpdate'
        }
        service.metaClass.postUpdate = { domainObject -> 
            registerCallback 'postUpdate'
        }
        service.metaClass.preDelete = { domainObjectOrParams -> 
            registerCallback 'preDelete'
        }
        service.metaClass.postDelete = { domainObject -> 
            registerCallback 'postDelete'
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
    def dataOrigin
    def lastModifiedBy
    def lastModified
    
    public static MyMock get( id ) {
        new MyMock( id: id, name: 'Mocked' )
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

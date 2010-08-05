/** *****************************************************************************
 © 2010 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
package com.sungardhe.banner.supplemental

import com.sungardhe.banner.testing.Foo
import com.sungardhe.banner.testing.BaseIntegrationTestCase
import com.sungardhe.banner.testing.AreaLibrary
import com.sungardhe.banner.testing.Bar

/**
 * Integration tests of the supplemental data service.
 */
class SupplementalDataServiceIntegrationTests extends BaseIntegrationTestCase {

    def fooService                     // injected by Spring
    def supplementalDataService        // injected by Spring
    def sessionContext                 // injected by Spring


    protected void setUp() {
        formContext = ['STVCOLL']
        super.setUp()
    }


    protected void tearDown() {
        super.tearDown()
    }


    /**
     * Tests that models can contain supplemental data values. Note that models may hold 'any' additional
     * properties -- but only those that are configured and known to the SupplementalDataService will be
     * persisted. Any 'unknown' properties will simply be 'dropped on the floor' (and logged as an error).
     */
    void testBasicModelSupplementalDataSupport() {
        def foo = fooService.create( newTestFooParams() )

        assertFalse foo.hasSupplementalProperties() // i.e., we haven't set a property on this instance yet

        foo.suppPropA = null
        assertTrue foo.hasSupplementalProperties()  // now that we've set a property (albeit to null), this should be true...

        foo.suppPropA = "I'm supplemental!"
        assertTrue foo.hasSupplementalProperties()
        assertEquals "Expected 1 but have ${foo.supplementalPropertyNames().size()} properties: ${foo.supplementalPropertyNames().join(', ')}", 1, foo.supplementalPropertyNames().size()
        assertTrue foo.hasSupplementalProperty( 'suppPropA' )
        assertFalse foo.hasSupplementalProperty( 'nope' )
        assertEquals "I'm supplemental!", foo.suppPropA

        def fooToo = new Foo( newTestFooParams() )
        assertNull fooToo.suppPropA
        assertFalse fooToo.hasSupplementalProperty( 'suppPropA' )
        assertFalse fooToo.hasSupplementalProperties()
        fooToo.suppPropA = null
        assertTrue fooToo.hasSupplementalProperties() // we've set a supplemental property to null
        assertTrue fooToo.hasSupplementalProperty( 'suppPropA' )
        fooToo.suppPropA = "fooToo has suppPropA too!"
        assertTrue fooToo.hasSupplementalProperties()
        assertTrue fooToo.hasSupplementalProperty( 'suppPropA' )

        foo.bar = "A second supplemental property -- but it is not configured and thus would not be saved!"
        assertEquals 2, foo.supplementalPropertyNames().size()
        assertTrue foo.hasSupplementalProperty( 'bar' )
        assertEquals "A second supplemental property -- but it is not configured and thus would not be saved!", foo.bar

        foo.suppPropA = "changed!"
        assertEquals "changed!", foo.suppPropA
        assertEquals "A second supplemental property -- but it is not configured and thus would not be saved!", foo.bar
        assertEquals 2, foo.supplementalPropertyNames().size()
        assertTrue 'suppPropA' in foo.supplementalPropertyNames()
        assertTrue 'bar' in foo.supplementalPropertyNames()
        assertTrue 'A second supplemental property -- but it is not configured and thus would not be saved!' in foo.supplementalProperties?.collect { k, v -> v }
        assertTrue 'changed!' in foo.supplementalProperties?.collect { k, v -> v }

        assertTrue foo.hasSupplementalProperty( 'suppPropA' )
        assertTrue foo.hasSupplementalProperty( 'bar' )
        assertFalse foo.hasSupplementalProperty( 'nope' )

        foo.bar = null
        assertTrue foo.hasSupplementalProperty( 'bar' )
    }


    /**
     * Tests that when a model is loaded by Hibernate, the 'SupplementalDataSupportListener' requests the
     * SupplementalDataService to load the supplemental data properties for the model.
     */
    void testSupplementalDataLoad() {
        // We'll slam configuration for Foo into the SupplementalDataService
        supplementalDataService.appendSupplementalDataConfiguration( [ "com.sungardhe.banner.testing.Foo":
                                                                            [ testSuppA: [ required: false, dataType: String ],
                                                                              testSuppB: [ required: false, dataType: boolean ]
                                                                            ]
                                                                      ] )

        supplementalDataService.supplementalDataPersistenceManager = new SupplementalDataPersistenceTestManager()
        assertTrue supplementalDataService.supportsSupplementalProperties( Foo )
        assertFalse supplementalDataService.supportsSupplementalProperties( AreaLibrary )


        def foo = new Foo( newTestFooParams() )
        foo.testSuppA = "Supplemental property A"
        foo.testSuppB = "Supplemental property B"

        foo = fooService.create( foo )
        assertNotNull foo.id
        // test that when a foo is retrieved from the database, it has our supplemental data properties

        foo.supplementalDataContent = [:] // force reset
        foo.refresh()
        
        def found = Foo.get( foo.id )
        assertTrue found?.hasSupplementalProperties()
        assertEquals "Supplemental property A", found.testSuppA
        assertEquals "Supplemental property B", found.testSuppB
    }


    /**
     * Tests that a model's supplemental data has no effect on another model's supplemental data
     * when the other model is the same type.
     */
    void testSupplementalDataIsolationWithinModelType() {
        // We'll slam configuration for Foo into the SupplementalDataService
        supplementalDataService.appendSupplementalDataConfiguration( [ "com.sungardhe.banner.testing.Foo":
                                                                            [ testSuppA: [ required: false, dataType: String ],
                                                                              testSuppB: [ required: false, dataType: boolean ],
                                                                              testSuppC: [ required: false, dataType: boolean ]
                                                                            ],
                                                                      ] )

        supplementalDataService.supplementalDataPersistenceManager = new SupplementalDataPersistenceTestManager()
        assertTrue supplementalDataService.supportsSupplementalProperties( Foo )
        assertFalse supplementalDataService.supportsSupplementalProperties( Bar ) // nope, in this test we've just configured Foo
        assertFalse supplementalDataService.supportsSupplementalProperties( AreaLibrary )

        def foo = new Foo( newTestFooParams() )
        foo.testSuppA = "Supplemental property A"
        foo.testSuppB = "Supplemental property B"

        def bar = new Foo( newTestFooParams( "#C" ) )
        assertTrue foo.hasSupplementalProperties()
        assertFalse bar.hasSupplementalProperties() // we haven't set any yet on the model instance

        foo = fooService.create( foo )
        bar = fooService.create( bar )

        assertTrue Foo.get( foo.id ).hasSupplementalProperties()
        assertFalse Foo.get( bar.id).hasSupplementalProperties() // we still haven't set any yet on the model instance

        bar.testSuppC = "Supplemental property C"
        bar.save() // we'll use GORM directly, bypassing our service

        foo.supplementalDataContent = [:] // not normally needed, but we'll do it here for this test
        foo.refresh() // force our hibernate listener will need to load the model

        assertTrue Foo.get( bar.id).hasSupplementalProperties()
        assertTrue Foo.get( bar.id ).supplementalProperties?.keySet()?.contains( "testSuppC" )
        assertTrue Foo.get( bar.id ).hasSupplementalProperty( "testSuppC" )
        assertFalse Foo.get( bar.id ).hasSupplementalProperty( "testSuppD" )
        assertEquals 1, Foo.get( bar.id ).supplementalProperties?.size()
        assertEquals 2, Foo.get( foo.id ).supplementalProperties?.size()

        // now we'll add a second supplemental data property (that is the same as
        bar.testSuppB = "My different value!"
        bar.save()

        assertEquals 2, Foo.get( bar.id ).supplementalProperties?.size()
        
        assertNull Foo.get( bar.id ).testSuppA
        assertEquals "My different value!", Foo.get( bar.id ).testSuppB
        assertEquals "Supplemental property C", Foo.get( bar.id ).testSuppC

        assertEquals "Supplemental property A", Foo.get( foo.id ).testSuppA
        assertEquals "Supplemental property B", Foo.get( foo.id ).testSuppB
        assertNull Foo.get( foo.id ).testSuppC
    }


    /**
     * Tests that a model's supplemental data has no effect on another model's supplemental data when
     * the other model is a different type.
     */
    void testSupplementalDataIsolationAcrossModelTypes() {
        // We'll slam configuration for Foo into the SupplementalDataService
        supplementalDataService.appendSupplementalDataConfiguration( [ "com.sungardhe.banner.testing.Foo":
                                                                            [ testSuppA: [ required: false, dataType: String ],
                                                                              testSuppB: [ required: false, dataType: boolean ],
                                                                            ],
                                                                       "com.sungardhe.banner.testing.Bar":
                                                                           [ testSuppB: [ required: false, dataType: String ],
                                                                             testSuppC: [ required: false, dataType: boolean ]
                                                                           ],
                                                                      ] )

        supplementalDataService.supplementalDataPersistenceManager = new SupplementalDataPersistenceTestManager()
        assertTrue supplementalDataService.supportsSupplementalProperties( Foo )
        assertTrue supplementalDataService.supportsSupplementalProperties( Bar )
        assertFalse supplementalDataService.supportsSupplementalProperties( AreaLibrary )

        def foo = new Foo( newTestFooParams() )
        foo.testSuppA = "Supplemental property A"
        foo.testSuppB = "Supplemental property B"

        def bar = new Bar( newTestFooParams( "#C" ) )
        assertTrue foo.hasSupplementalProperties()
        assertFalse bar.hasSupplementalProperties() // we haven't set any yet on the model instance

        foo = fooService.create( foo )
        bar.save( flushImmediately: true )
        assertNotNull bar.id

        assertTrue Foo.get( foo.id ).hasSupplementalProperties()
        assertFalse Bar.get( bar.id ).hasSupplementalProperties() // we still haven't set any yet on the model instance

        bar.testSuppC = "Supplemental property C"
        bar.save( flushImmediately: true ) // we'll use GORM directly, bypassing our service

        foo.supplementalDataContent = [:] // not normally needed, but we'll do it here for this test
        foo.refresh() // force our hibernate listener will need to load the model

        assertTrue Bar.get( bar.id).hasSupplementalProperties()
        assertTrue Bar.get( bar.id ).supplementalProperties?.keySet()?.contains( "testSuppC" )
        assertTrue Bar.get( bar.id ).hasSupplementalProperty( "testSuppC" )
        assertFalse Bar.get( bar.id ).hasSupplementalProperty( "testSuppD" )
        assertEquals 1, Bar.get( bar.id ).supplementalProperties?.size()
        assertEquals 2, Foo.get( foo.id ).supplementalProperties?.size()

        // now we'll add a second supplemental data property (that is the same as
        bar.testSuppB = "My different value!"
        bar.save( flushImmediately: true )

        assertEquals 2, Bar.get( bar.id ).supplementalProperties?.size()

        assertNull Bar.get( bar.id ).testSuppA
        assertEquals "My different value!", Bar.get( bar.id ).testSuppB
        assertEquals "Supplemental property C", Bar.get( bar.id ).testSuppC

        assertEquals "Supplemental property A", Foo.get( foo.id ).testSuppA
        assertEquals "Supplemental property B", Foo.get( foo.id ).testSuppB
        assertNull Foo.get( foo.id ).testSuppC
    }


    /**
     * Tests that properties set on a model that are not configured as supplemental data are
     * dropped on the floor, and do not prevent saving the model (including 'pre-defined' supplemental data properties).
     */
    void testUnsupportedPropertiesAreDroppedOnFloor() {
        // We'll slam configuration for Foo into the SupplementalDataService
        supplementalDataService.appendSupplementalDataConfiguration( [ "com.sungardhe.banner.testing.Foo":
                                                                            [ testSuppA: [ required: false, dataType: String ],
                                                                              testSuppB: [ required: false, dataType: boolean ],
                                                                            ],
                                                                      ] )

        supplementalDataService.supplementalDataPersistenceManager = new SupplementalDataPersistenceTestManager()

        def foo = new Foo( newTestFooParams() )
        foo.testSuppA = "Supplemental property A"
        foo.testSuppB = "Supplemental property B"
        foo.testSuppC = "A not supported property C"

        assertTrue foo.hasSupplementalProperties()
        assertEquals 3, foo.supplementalProperties.size() // as far as we care, we have 3 supplemental properties -- even though one won't be saved

        foo = fooService.create( foo )
        assertNotNull foo.id
        assertEquals 2, foo.supplementalProperties.size() // our 'not supported' property is no longer here...
        assertFalse foo.supplementalProperties.keySet().contains( "testSuppC" )
    }



    private Map newTestFooParams( code = "TT" ) {
        [ code: code, description: "Horizon Test - $code", addressStreetLine1: "TT", addressStreetLine2: "TT", addressStreetLine3: "TT", addressCity: "TT",
          addressState: "TT", addressCountry: "TT", addressZipCode: "TT", systemRequiredIndicator: "N", voiceResponseMessageNumber: 1, statisticsCanadianInstitution: "TT",
          districtDivision: "TT", houseNumber: "TT", addressStreetLine4: "TT" ]
    }
    
}


class SupplementalDataPersistenceTestManager {


    Map persistentStore = [:] // [ modelClass: [ key: [ prop1: val1, prop2: val2 ], key2: [ prop1: valx ] ] ]


    public def loadSupplementalDataFor( model ) {
        def modelKey = "${model.class.name}-${model.id}"
        if (persistentStore."$modelKey") {
            model.supplementalProperties = persistentStore."$modelKey".clone()
        }
    }


    public def persistSupplementalDataFor( model ) {
        def modelKey = "${model.class.name}-${model.id}"
        if (!persistentStore."$modelKey" ) {
           persistentStore."$modelKey" = new HashMap()
        }
        def storage = persistentStore."$modelKey"

        model.supplementalProperties?.each { k, v ->
            storage.put( k, v )
        }
    }


    public def removeSupplementalDataFor( model ) {
        def modelKey = "${model.class.name}-${model.id}"
        persistentStore."$modelKey" = null
    }

}
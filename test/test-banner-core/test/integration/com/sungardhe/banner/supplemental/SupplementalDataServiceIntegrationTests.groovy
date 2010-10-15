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
import com.sungardhe.banner.service.ServiceBase

/**
 * Integration tests of the supplemental data service.  Note that this test contains
 * tests of the framework that uses a mocked out persistence manager (that simply holds
 * supplemental data values in memeory.  Please see SdeServiceIntegrationTests that
 * test the end-to-end supplemental data engine support.
 * @see SdeServiceIntegrationTests
 */
class SupplementalDataServiceIntegrationTests extends BaseIntegrationTestCase {

    def fooService                         // injected by Spring
    def supplementalDataService            // injected by Spring
    def sessionContext                     // injected by Spring
	def supplementalDataPersistenceManager // injected by Spring
    def testSupplementalDataPersistenceTestManager

    protected void setUp() {
        formContext = ['STVCOLL']
        super.setUp()

        configureSupplementalData( [ "com.sungardhe.banner.testing.Foo":
                                       [ testSuppA: [ required: false, dataType: String ],
                                         testSuppB: [ required: false, dataType: boolean ] ],
                                     "com.sungardhe.banner.testing.Bar":
                                       [ testSuppA: [ required: false, dataType: String ],
                                         testSuppB: [ required: false, dataType: boolean ] ]] )
    }


    protected void tearDown() {
		//clean up spring-injected persistence manager
		supplementalDataService.supplementalDataConfiguration.remove( "com.sungardhe.banner.testing.Foo" )
		supplementalDataService.supplementalDataPersistenceManager = supplementalDataPersistenceManager
        testSupplementalDataPersistenceTestManager = null

		super.tearDown()
    }


    /**
     * Tests that a model that supports supplemental properties will reflect that when it is
     * loaded from the database.
     */
    void testLoadModelsWithSupplementalDataSupport() {

        assertTrue supplementalDataService.supportsSupplementalProperties( Foo )
        assertFalse supplementalDataService.supportsSupplementalProperties( AreaLibrary )

        def foo = fooService.create( newTestFooParams() )
        assertTrue foo.hasSupplementalProperties()
        assertEquals 2, foo.supplementalProperties?.size()
        assertTrue foo.supplementalPropertyNames.containsAll( [ 'testSuppA', 'testSuppB' ] )
        assertEquals 1, foo.testSuppA.size() // only a single value
        assertEquals 1, foo.testSuppB.size() // only a single value
        shouldFail( MissingPropertyException ) { foo.betterNotFindMe }
    }


    void testSetSupplementalPropertyValue() {

        shouldFail( MissingPropertyException ) { new Foo( newTestFooParams() ).testSuppA }

        def foo = fooService.create( newTestFooParams() )
        foo.testSuppA = newPropertyValue( value: "I'm supplemental!" )
        foo.description = "Updated" // TODO: Currently a core property of the model MUST be modified for supplemental properties to be persisted

        foo = fooService.update( foo )
        assertTrue foo.hasSupplementalProperties()
        assertEquals "Expected 2 but have ${foo.supplementalPropertyNames().size()} properties: ${foo.supplementalPropertyNames().join(', ')}", 2, foo.supplementalPropertyNames().size()
        assertTrue foo.hasSupplementalProperty( 'testSuppA' )
        assertEquals "I'm supplemental!", foo.testSuppA."1".value
    }


    void testCreateWithSupplementalPropertyValue() {
        def foo = new Foo( newTestFooParams() )
        shouldFail( MissingPropertyException ) { foo.testSuppA = newPropertyValue( value: "I'm not available yet!" ) }

        // but we can still add supplemental properties directly...
        foo.supplementalProperties = [ testSuppA: newPropertyValue( value: "I'm a new supplemental property!" ),
                                       testSuppB: newPropertyValue( value: "Me too!" ) ]

        foo = fooService.create( foo )
        assertTrue foo.hasSupplementalProperties()
        assertEquals "Expected 2 but have ${foo.supplementalPropertyNames().size()} properties: ${foo.supplementalPropertyNames().join(', ')}", 2, foo.supplementalPropertyNames().size()
        assertTrue foo.hasSupplementalProperty( 'testSuppA' )
        assertTrue foo.hasSupplementalProperty( 'testSuppB' )
        assertEquals "I'm a new supplemental property!", foo.testSuppA."1".value
        assertEquals "Me too!", foo.testSuppB."1".value
    }


    /**
     * Tests that a model's supplemental data has no effect on another model's supplemental data
     * when the other model is the same type.
     */
    void testSupplementalDataIsolationWithinModelType() {
        runTestSupplementalDataIsolation( fooService )
    }


    /**
     *  Tests that a model's supplemental data doesn't conflict with that of another.
     **/
    void testSupplementalDataIsolationAcrossModels() {
        supplementalDataService.appendSupplementalDataConfiguration(  "com.sungardhe.banner.testing.Bar":
                                                                           [ testSuppA: [ required: false, dataType: String ],
                                                                             testSuppB: [ required: false, dataType: boolean ]
                                                                           ]
                                                                     )
        runTestSupplementalDataIsolation( new BarService() )
    }


    private runTestSupplementalDataIsolation( barService ) {

        def foo = fooService.create( newTestFooParams() )
        def bar = barService.create( newTestFooParams( "#C" ) )

        assertTrue supplementalDataService.supportsSupplementalProperties( Foo )
        assertFalse supplementalDataService.supportsSupplementalProperties( AreaLibrary )

        foo.testSuppA = newPropertyValue( value: "Supplemental property A" )
        foo.testSuppB = new SupplementalPropertyValue( [ '1': new SupplementalPropertyDiscriminatorContent( value: "Supplemental property B" ),
                                                         '2': new SupplementalPropertyDiscriminatorContent( value: "Supplemental property B2" ) ] )
        foo = fooService.update( foo )

        bar.testSuppA = newPropertyValue( value: "Bar Supplemental property A" )
        bar.testSuppB = newPropertyValue( value: "Bar Supplemental property B" )
        bar = barService.update( bar )

        assertTrue foo.hasSupplementalProperties()
        assertTrue bar.hasSupplementalProperties()

        assertTrue foo.hasSupplementalProperty( "testSuppA" )
        assertTrue bar.hasSupplementalProperty( "testSuppA" )

        assertTrue foo.hasSupplementalProperty( "testSuppB" )
        assertTrue bar.hasSupplementalProperty( "testSuppB" )

        assertEquals 2, foo.supplementalProperties?.size()
        assertEquals 2, bar.supplementalProperties?.size()

        assertEquals 1, foo.testSuppA.size()
        assertEquals 1, bar.testSuppA.size()

        assertEquals 2, foo.testSuppB?.size()
        assertEquals 1, bar.testSuppB?.size()

        assertEquals "Supplemental property A", foo.testSuppA.'1'.value
        assertEquals "Bar Supplemental property A", bar.testSuppA.'1'.value

        assertEquals "Supplemental property B", foo.testSuppB.'1'.value
        assertEquals "Supplemental property B2", foo.testSuppB.'2'.value
        assertEquals "Bar Supplemental property B", bar.testSuppB.'1'.value

        shouldFail( MissingPropertyException ) { foo.testSuppX }
        shouldFail( MissingPropertyException ) { bar.testSuppX }
    }


    private Map newTestFooParams( code = "TT" ) {
        [ code: code, description: "Horizon Test - $code", addressStreetLine1: "TT", addressStreetLine2: "TT", addressStreetLine3: "TT", addressCity: "TT",
          addressState: "TT", addressCountry: "TT", addressZipCode: "TT", systemRequiredIndicator: "N", voiceResponseMessageNumber: 1, statisticsCanadianInstitution: "TT",
          districtDivision: "TT", houseNumber: "TT", addressStreetLine4: "TT" ]
    }


    private def configureSupplementalData( Map map ) {
        supplementalDataService.appendSupplementalDataConfiguration( map )
        testSupplementalDataPersistenceTestManager = new SupplementalDataPersistenceTestManager()
        supplementalDataService.supplementalDataPersistenceManager = testSupplementalDataPersistenceTestManager
    }


    public SupplementalPropertyValue newPropertyValue( Map content ) {
        SupplementalPropertyDiscriminatorContent discProp = new SupplementalPropertyDiscriminatorContent( content )
        new SupplementalPropertyValue( [ (discProp.disc): discProp ] )
    }

}


class SupplementalDataPersistenceTestManager {

    // Map contains: [ modelKey: [ propertyName: supplementalPropertyValueInstance ] ]
    Map<String,Map<String,SupplementalPropertyValue>> persistentStore  = new HashMap()


    public def loadSupplementalDataFor( model ) {
        println "SupplementalDataPersistenceTestManager.load called for model $model"
        def modelKey = "${model.class.name}-${model.id}"
        if (persistentStore."$modelKey") {
            model.supplementalProperties = persistentStore."$modelKey".clone()
            model
        }
        else {
            println "...and will load default definitions (since there is no existing supplemental data"
            // load in common specification of the supplemental data properties
            Map defaultSuppData = [ testSuppA: newDefaultPropertySpec( '1' ),
                                    testSuppB: newDefaultPropertySpec( '1', Boolean ) ]
            model.supplementalProperties = defaultSuppData
            model
        }
    }


    public def persistSupplementalDataFor( model ) {
        println "SupplementalDataPersistenceTestManager.persist called"
        def modelKey = "${model.class.name}-${model.id}"
        persistentStore."$modelKey" = new HashMap()

        model.supplementalProperties?.each { k, v ->
            persistentStore."$modelKey".put( k, v )
        }
        loadSupplementalDataFor model
    }


    public def removeSupplementalDataFor( model ) {
        // not needed in this test implementation
    }


    private SupplementalPropertyValue newDefaultPropertySpec( discriminator = '1', dataType = String ) {
        SupplementalPropertyDiscriminatorContent discProp =
            new SupplementalPropertyDiscriminatorContent( disc: discriminator, dataType: dataType )

        new SupplementalPropertyValue( [ (discProp.disc): discProp ] )
    }

}


class BarService extends ServiceBase {
    public BarService() { domainClass = Bar }
}

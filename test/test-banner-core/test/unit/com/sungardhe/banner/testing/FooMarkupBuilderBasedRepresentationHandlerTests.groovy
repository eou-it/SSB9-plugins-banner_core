package com.sungardhe.banner.testing

import grails.test.GrailsUnitTestCase
import groovy.xml.StreamingMarkupBuilder
import groovy.xml.XmlUtil
import javax.xml.transform.stream.StreamSource
import javax.xml.XMLConstants
import javax.xml.validation.SchemaFactory

/**
 * Tests the ability to generated the needed XML using a StreamingMarkupBuilder.
 */
class FooMarkupBuilderBasedRepresentationHandlerTests extends GrailsUnitTestCase {


    void testSingle() {
        def handler = new FooMarkupBuilderBasedRepresentationHandler()
        def builder = handler.singleBuilder()

        def foo = new Foo( newTestFooParams() )
        foo.id = 8
        def content = [ data: foo, refBase: "http://myTest.com/test-banner-core/foo" ]
        def result = builder.buildRepresentation( content )

        def xml = new XmlSlurper().parseText( result ).declareNamespace( [ '': 'urn:com:sungardhe:Student' ])
        assert xml instanceof groovy.util.slurpersupport.GPathResult

        assertEquals 'FooInstance', xml.name()
        assertTrue "Ref element not as expected: ${xml.Foo[0]?.Ref}", "${xml.Foo[0]?.Ref}" ==~ /.*test-banner-core\/foo\/8/
        assertEquals "Expected Foo id of '8' but got: ${xml.Foo[0]?.@id.text().toString()}", '8', xml.Foo[0]?.@id.text().toString()
    }


    void testMultiple() {
        def handler = new FooMarkupBuilderBasedRepresentationHandler()
        def builder = handler.collectionBuilder()

        def foos = []
        [ "TT", "UU", "VV" ].eachWithIndex { code, i ->
            def foo = new Foo( newTestFooParams( code ) )
            foo.id = i
            foos << foo
        }

        def content = [ data: foos, refBase: "http://myTest.com/test-banner-core/foo", totalCount: 45, pageOffset: 2, pageMaxSize: 5 ]
        def result = builder.buildRepresentation( content )
        println "XXXXXXXXXZZZZZZZZZZZZZZZZZZZZZZZZZZZ $result"

        def xmlList = new XmlSlurper().parseText( result )

        assertEquals 'FooList', xmlList.name()
        assertEquals '1.0', xmlList.@apiVersion.text().toString()
        assertTrue 45 <= xmlList.@totalCount.text().toInteger()
        assertEquals "5", xmlList.@pageSize.text()
        assertEquals '2', xmlList.@page.text()
        assertEquals 3, xmlList.Foo.size()

        assertEquals '1.0', xmlList.children()[ 0 ].@apiVersion.text().toString()
        assertTrue "${xmlList.Ref}" ==~ /.*test-banner-core\/foo.*/

        def xsdUrl = "file:///${grails.util.BuildSettingsHolder.settings?.baseDir}/grails-app/views/foo/list.foo.v1.0.xsd"
        def validator = schemaValidatorFor( xsdUrl )
        validator.validate( new StreamSource( new StringReader( result ) ) )                
    }


    private Map newTestFooParams( code = "TT" ) {
        [ code: code, description: "Horizon Test - $code", addressStreetLine1: "TT", addressStreetLine2: "TT", addressStreetLine3: "TT", addressCity: "TT",
		  addressState: "TT", addressCountry: "TT", addressZipCode: "TT", systemRequiredIndicator: "N", voiceResponseMessageNumber: 1, statisticsCanadianInstitution: "TT",
		  districtDivision: "TT", houseNumber: "TT", addressStreetLine4: "TT", lastModified: new Date(), lastModifiedBy: "Me", dataOrigin: "Me",
          version: 2 ]
    }


    protected def schemaValidatorFor( xsdUrl ) {
        def factory = SchemaFactory.newInstance( XMLConstants.W3C_XML_SCHEMA_NS_URI )
        def schema = factory.newSchema( new StreamSource( new URL( xsdUrl ).openStream() ) )
        schema.newValidator()
    }

    
}

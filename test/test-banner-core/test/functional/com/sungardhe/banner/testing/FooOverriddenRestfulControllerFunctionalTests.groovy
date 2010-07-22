/** *****************************************************************************
 © 2010 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
package com.sungardhe.banner.testing

import javax.xml.transform.stream.StreamSource

/**
 * Functional tests of the Foo REST API, where params extraction and rendering are custom.
 */
class FooOverriddenRestfulControllerFunctionalTests extends BaseFunctionalTestCase {


    protected void setUp() {
        formContext = ['STVCOLL']
        super.setUp()

        login()
    }


    // Note: We've mapped URIs with 'foo2' to the 'FooOverriddenRestfulController', in order
    // to test overridden params extraction and rendering. This is not normal -- it is for testing only. 


    void testRestXmlApiForList() {

        def pageSize = 15
        get("/api/foo2?max=$pageSize") {
            headers[ 'Content-Type' ] = 'application/vnd.sungardhe.student.v0.01+xml'
            headers[ 'Authorization' ] = authHeader()
        }

        assertStatus 200
        assertEquals 'application/vnd.sungardhe.student.v0.01+xml', page?.webResponse?.contentType
        def stringContent = page?.webResponse?.contentAsString
        def xmlList = new XmlSlurper().parseText(stringContent)

        assertEquals 'FooList', xmlList.name()
        assertEquals '1.0', xmlList.@apiVersion.text().toString()
        assertTrue 45 <= xmlList.@totalCount.text().toInteger()
        assertEquals "$pageSize", xmlList.@pageSize.text()
        assertEquals '0', xmlList.@page.text()
        assertEquals pageSize, xmlList.Foo.size()
        assertEquals pageSize + 1, xmlList.children().size() // foo elements plus Ref element
        assertEquals '1.0', xmlList.children()[ 0 ].@apiVersion.text().toString()
        assertTrue "${xmlList.Ref}" ==~ /.*test-banner-core\/foo\/list/  // note this Ref is expected by convention, but in this contrived case wouldn't take us to the correct controller

        // Now we'll ensure the xml complies it's XML Schema
        // validate will throw exceptions, and the test will fail, if the stringContent doesn't comply with the schema
        def xsdUrl = "file:///${grails.util.BuildSettingsHolder.settings?.baseDir}/grails-app/views/foo/list.foo.v1.0.xsd"
        def validator = schemaValidatorFor(xsdUrl)

        validator.validate( new StreamSource( new StringReader(stringContent) ) )
    }


     void testRestXmlApiForShow() {

         get( "/api/foo2/1" ) {  // 'GET' /api/foo2/id => 'show'
             headers[ 'Content-Type' ] = 'application/vnd.sungardhe.student.v0.01+xml'
             headers[ 'Authorization' ] = authHeader()
         }
         assertStatus 200
         assertEquals 'application/vnd.sungardhe.student.v0.01+xml', page?.webResponse?.contentType
         def stringContent = page?.webResponse?.contentAsString
         def xml = new XmlSlurper().parseText( stringContent )

         assertEquals 'FooInstance', xml.name()
         assertTrue "Ref element not as expected: ${xml.Ref}", "${xml.Ref}" ==~ /.*test-banner-core\/foo\/1/
         assertEquals "Expected Foo id of '1' but got: ${xml.Foo[0]?.@id.text().toString()}", '1', xml.Foo[0]?.@id.text().toString()
    }


    // We'll test create, update, and delete within this single test to avoid overhead.
    void testRestXmlApiForCreateUpdateAndDelete() {
        def id
        try {
            post( "/api/foo2" ) {   // 'POST' /api/foo2 => 'create'
                headers[ 'Content-Type' ] = 'application/vnd.sungardhe.student.v0.01+xml'
                headers[ 'Authorization' ] = authHeader()
                body { """
                    <FooInstance apiVersion="1.0">
                        <Foo systemRequiredIndicator="N">
                            <Code>#Z</Code>
                            <Description>Created via XML</Description>
                        </Foo>
                    </FooInstance>
                    """
            	}
            }
            assertStatus 201
            assertEquals 'application/vnd.sungardhe.student.v0.01+xml', page?.webResponse?.contentType
            def stringContent = page?.webResponse?.contentAsString
            def xml = new XmlSlurper().parseText( stringContent )

            id = xml.Foo[0]?.@id.text().toInteger() // we'll grab this first so subsequent failures don't prevent our finally block from deleting this new foo
            assertNotNull "Expected new foo with id but got: ${xml.Foo[0]?.toString()}", xml.Foo[0]?.@id?.text()?.toString()

            assertEquals 'FooInstance', xml.name()
            assertTrue "Ref element not as expected: ${xml.Ref}", "${xml.Ref}" ==~ /.*test-banner-core\/foo.*/
            def ref = "${xml.Ref}"
            assertEquals "Expected foo with code '#Z' but got: ${xml.Foo[0]?.Code[0].text()}", '#Z', xml.Foo[0]?.Code[0].text()

            put( "/api/foo2/$id" ) {  // 'PUT' /api/foo2 => 'update'
                headers[ 'Content-Type' ] = 'application/vnd.sungardhe.student.v0.01+xml'
                headers[ 'Authorization' ] = authHeader()
                body { """
                    <FooInstance apiVersion="1.0">
                        <Foo id='$id' systemRequiredIndicator="N" optimisticLockVersion="0">
                            <Description>Updated!</Description>
                        </Foo>
                    </FooInstance>
                    """
            	}
            }
            assertStatus 200
            assertEquals 'application/vnd.sungardhe.student.v0.01+xml', page?.webResponse?.contentType
            stringContent = page?.webResponse?.contentAsString
            xml = new XmlSlurper().parseText( stringContent )

            assertEquals 'FooInstance', xml.name()
            assertEquals "Ref element after 'update' not the same as that after 'create': ${xml.Ref}", ref, "${xml.Ref}"
            assertEquals "Expected id $id but got: ${xml.Foo[0]?.@id.text()}", id, xml.Foo[0]?.@id.text().toInteger()
            assertEquals "Expected foo with code '#Z' but got: ${xml.Foo[0]?.Code[0].text()}", '#Z', xml.Foo[0]?.Code[0].text()
            assertEquals "Expected foo with description 'Updated!' but got: ${xml.Foo[0]?.Description[0].text()}", 'Updated!', xml.Foo[0]?.Description[0].text()

        } finally {

            delete( "/api/foo2/$id" ) {
                headers[ 'Content-Type' ] = 'application/vnd.sungardhe.student.v0.01+xml'
                headers[ 'Authorization' ] = authHeader()
            }
            assertStatus 200
            assertEquals 'text/xml', page?.webResponse?.contentType // we do not currently have a 'custom' delete confirmation message, so no custom MIME type is used here
            def stringContent = page?.webResponse?.contentAsString
            def xml = new XmlSlurper().parseText( stringContent )

            assertTrue "Response not as expected: ${xml}", "${xml}" ==~ /.*true.*/

            get( "/api/foo2/$id" ) {  // 'GET' /api/foo2/id => 'show'
                headers[ 'Content-Type' ] = 'application/vnd.sungardhe.student.v0.01+xml'
                headers[ 'Authorization' ] = authHeader()
            }
            assertStatus 404
        }

   }

}

/** *****************************************************************************
 © 2010 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
package com.sungardhe.banner.testing

import grails.converters.JSON
import grails.converters.XML
import javax.xml.transform.stream.StreamSource

/**
 * Functional tests of the Foo Controller.
 */
class FooControllerFunctionalTests extends BaseFunctionalTestCase {


    protected void setUp() {
        formContext = [ 'STVCOLL' ]
        super.setUp()
    }


    // --------------------- Test HTML Representation, non-RESTfully ------------------------


     // Test ability to use a 'non RESTful' URL to access a mixed-in action.
     // Here we explicitly specify the 'action' ('list') and format (json) in the URI
     void testViewNonRestfully_HTML() {

         login()
         get( "/foobar/view" )

         assertStatus 200
         assertEquals 'text/html', page?.webResponse?.contentType

         def stringContent = page?.webResponse?.contentAsString
         assertTrue stringContent ==~ /.*If I had a UI.*/
     }


    // -------------------------------- Test JSON Representations ---------------------------------


    void testList_JSON() {

        def pageSize = 5
        get( "/api/foobar?max=$pageSize" ) {
            headers[ 'Content-Type' ] = 'application/json'
            headers[ 'Authorization' ] = authHeader()
        }
        assertStatus 200
        assertEquals 'application/json', page?.webResponse?.contentType

        def stringContent = page?.webResponse?.contentAsString
        def data = JSON.parse( stringContent )
        assertTrue 45 <= data.totalCount
        assertEquals 5, data.data.size()
    }


    // Test ability to use a 'non RESTful' URL to access a mixed-in action.
    // Here we explicitly specify the 'action' ('list') and format (json) in the URI
    void testListNonRestfully_JSON() {

        login()

        def pageSize = 5
        get( "/foobar/list.json?max=$pageSize" )

        assertStatus 200
        assertEquals 'application/json', page?.webResponse?.contentType

        def stringContent = page?.webResponse?.contentAsString
        def data = JSON.parse( stringContent )
        assertTrue 45 <= data.totalCount
        assertEquals 5, data.data.size()
    }


    void testShow_JSON() {

        get( "/api/foobar/1" ) {
            headers[ 'Content-Type' ] = 'application/json'
            headers[ 'Authorization' ] = authHeader()
        }
        assertStatus 200
        assertEquals 'application/json', page?.webResponse?.contentType
        def stringContent = page?.webResponse?.contentAsString
        def data = JSON.parse( stringContent )
        assertEquals 1, data.data.id
    }


    // We'll test create, update, and delete within this single test to avoid overhead.
    void testCreateUpdateAndDelete_JSON() {
        def id
        try {
            post("/api/foobar") {   // 'POST' /api/foobar => 'create'
                headers[ 'Content-Type' ] = 'application/json'
                headers[ 'Authorization' ] = authHeader()
                body { """
                    {
                        "class": "com.sungardhe.banner.testing.Foo",
                        "description": "No College Designated",
                        "code": "Z9",
                        "systemRequiredIndicator": "Y",
                    }
                    """
                }
            }
            assertStatus 201
            assertEquals 'application/json', page?.webResponse?.contentType
            def stringContent = page?.webResponse?.contentAsString
            def data = JSON.parse( stringContent ).data

            id = data.id
            assertNotNull "Expected new Foo with an id but got: ${data}", id
            assertEquals "Expected code 'Z9' but got ${data.code}", 'Z9', data.code

            put( "/api/foobar/$id" ) {  // 'PUT' /api/foobar => 'update'
                headers[ 'Content-Type' ] = 'application/json'
                headers[ 'Authorization' ] = authHeader()
                body {
                    """
                        {
                            "class": "com.sungardhe.banner.testing.Foo",
                            "id": "$id",
                            "description": "Updated",
                        }"""
                } // Notice the body only needed to include the properties we want to change, plus the id and class
            }
            assertStatus 200
            assertEquals 'application/json', page?.webResponse?.contentType
            stringContent = page?.webResponse?.contentAsString
            data = JSON.parse( stringContent ).data

            assertNotNull "Expected new Foo with an id but got: ${data}", id
            assertEquals "Expected code 'Z9' but got ${data.code}", 'Z9', data.code
            assertEquals "Expected description 'Updated' but got ${data.description}", 'Updated', data.description
        }
        finally {
            delete( "/api/foobar/$id" ) {
                headers[ 'Content-Type' ] = 'application/json'
                headers[ 'Authorization' ] = authHeader()
            }
            assertStatus 200
            assertEquals 'application/json', page?.webResponse?.contentType

            def stringContent = page?.webResponse?.contentAsString
            def data = JSON.parse( stringContent )
            assertTrue "Response not as expected: ${data.success}", data.success

            get( "/api/foobar/$id" ) {  // 'GET' /api/college/id => 'show'
                headers[ 'Content-Type' ] = 'application/json'
                headers[ 'Authorization' ] = authHeader()
            }
            assertStatus 404
        }
    }


    // -------------------------------- Test 'default' XML Representations ---------------------------------
    //           Note: This controller uses default XML rendering for the text/xml content-type.


    void testShow_XML() {

        get( "/api/foobar/1" ) {  // 'GET' /api/foobar/id => 'show'
            headers[ 'Content-Type' ] = 'text/xml'
            headers[ 'Authorization' ] = authHeader()
        }
        assertStatus 200
        assertEquals 'application/xml', page?.webResponse?.contentType
        def stringContent = page?.webResponse?.contentAsString
        
        def xmlResultMap = new XmlSlurper().parseText( stringContent )
        def xmlFoo = xmlResultMap.entry.findAll { it.@key.text() == "data" }
        assertEquals "Expected Foo id of '1' but got: ${xmlFoo[0].@id.text().toString()}", '1', xmlFoo[0]?.@id.text().toString()
   }


    void testList_XML() {

        def pageSize = 15
        get( "/api/foobar?max=$pageSize" ) {
            headers[ 'Content-Type' ] = 'text/xml'
            headers[ 'Authorization' ] = authHeader()
        }
        assertStatus 200
        assertEquals 'application/xml', page?.webResponse?.contentType
        def stringContent = page?.webResponse?.contentAsString

        def xmlResultMap = new XmlSlurper().parseText( stringContent )
        def foos = xmlResultMap.entry.foo
        assertEquals pageSize, foos.size()

        def xmlTotalCountEntry = xmlResultMap.entry.findAll { it.@key.text() == "totalCount" }
        assertTrue 75 <= xmlTotalCountEntry.text().toInteger()
    }


    // We'll test create, update, and delete within this single test to avoid overhead.
    void testCreateUpdateAndDelete_XML() {
        def id
        try {
            def code = 'Z8'
            def xmlBody = new Foo( code: code, description: 'Desc_Z8' ) as XML

            // 'POST' /api/foobar => 'create'
            post( "/api/foobar" ) {
                headers[ 'Content-Type' ] = 'text/xml'
                headers[ 'Authorization' ] = authHeader()
                body { xmlBody }
            }
            assertStatus 201
            assertEquals 'application/xml', page?.webResponse?.contentType
            def stringContent = page?.webResponse?.contentAsString
            def xmlResultMap = new XmlSlurper().parseText( stringContent )
            def xmlFoos = xmlResultMap.entry.findAll { it.@key.text() == "data" }
            
            // get the id first, so a subsequent failure won't prevent our finally block from deleting this...
            id = xmlFoos[0]?.@id?.text()?.toInteger()
            assertTrue id != null
            def version = xmlFoos[0]?.@version?.text()?.toInteger()
            assertTrue version != null
            assertEquals code, xmlFoos[0].code.text()

            put( "/api/foobar/$id" ) {  // 'PUT' /api/foobar => 'update'
                headers[ 'Content-Type' ] = 'text/xml'
                headers[ 'Authorization' ] = authHeader()
                body { """
                    <foo id="$id" version="$version">
                        <description>Updated</description>
                    </foo>"""
                }
            }
            assertStatus 200
            assertEquals 'application/xml', page?.webResponse?.contentType
            stringContent = page?.webResponse?.contentAsString
            xmlResultMap = new XmlSlurper().parseText( stringContent )
            xmlFoos = xmlResultMap.entry.findAll { it.@key.text() == "data" }

            def updatedFooId = xmlFoos[0]?.@id?.text()?.toInteger()
            assertEquals id, updatedFooId
            def updatedVersion = xmlFoos[0]?.@version?.text()?.toInteger()
            assertEquals version + 1, updatedVersion
            assertEquals code, xmlFoos[0].code.text()
            assertEquals 'Updated', xmlFoos[0].description.text()

        }
        finally {
            delete( "/api/foobar/$id" ) {
                headers[ 'Content-Type' ] = 'text/xml'
                headers[ 'Authorization' ] = authHeader()
            }
            assertStatus 200
            assertEquals 'application/xml', page?.webResponse?.contentType
            def stringContent = page?.webResponse?.contentAsString
            def xml = new XmlSlurper().parseText( stringContent )

            assertTrue "Response not as expected: ${xml}", "${xml}" ==~ /.*true.*/

            get( "/api/foobar/$id" ) {  // 'GET' /api/foobar/id => 'show'
                headers[ 'Content-Type' ] = 'text/xml'
                headers[ 'Authorization' ] = authHeader()
            }
            assertStatus 404
        }

    }


    // -------------------------------- Test 'custom' XML Representations ---------------------------------
    //       Note: This controller supports an application/vnd.sungardhe.student.v0.01+xml MIME type
    //             that is used for specifying a versioned, XML Schema-constrained body.


    void testList_VND() {

        // Note this test repeats its assertions for multiple representations.
        [ 'application/vnd.sungardhe.student.v0.01+xml',
          'application/vnd.sungardhe.student.v0.02+xml',
          'application/vnd.whatsamattau.student.v0.01+xml',
          'application/vnd.whatsamattau.student.v0.02+xml' ].each { contentType ->

            def pageSize = 15
            get("/api/foobar?max=$pageSize") {
                headers[ 'Content-Type' ]  = contentType
                headers[ 'Authorization' ] = authHeader()
            }
            assertStatus 200
            assertEquals contentType, page?.webResponse?.contentType
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
            // validate will throw exceptions, and the test will fail, if the stringContent doesn't comply with the schema.   
            // Note that all representations tested in this closure are really for the same XML, so we can test against one schema
            def xsdUrl = "file:///${grails.util.BuildSettingsHolder.settings?.baseDir}/grails-app/views/foo/list.foo.v1.0.xsd"
            def validator = schemaValidatorFor(xsdUrl)

            validator.validate( new StreamSource( new StringReader(stringContent) ) )
        }
    }


    void testShow_VND() {

        // Note this test repeats its assertions for multiple representations.
        [ 'application/vnd.sungardhe.student.v0.01+xml',
          'application/vnd.sungardhe.student.v0.02+xml',
          'application/vnd.whatsamattau.student.v0.01+xml',
          'application/vnd.whatsamattau.student.v0.02+xml' ].each { contentType ->

            get( "/api/foobar/1" ) {  // 'GET' /api/foobar/id => 'show'
                headers[ 'Content-Type' ]  = contentType
                headers[ 'Authorization' ] = authHeader()
            }
            assertStatus 200
            assertEquals contentType, page?.webResponse?.contentType
            def stringContent = page?.webResponse?.contentAsString
            def xml = new XmlSlurper().parseText( stringContent )

            assertEquals 'FooInstance', xml.name()
            assertTrue "Ref element not as expected: ${xml.Ref}", "${xml.Ref}" ==~ /.*test-banner-core\/foo\/1/
            assertEquals "Expected Foo id of '1' but got: ${xml.Foo[0]?.@id.text().toString()}", '1', xml.Foo[0]?.@id.text().toString()
        }
    }


    // We'll test create, update, and delete within this single test to avoid overhead.
    // Note this test repeats its assertions for multiple representations.
    void testCreateUpdateAndDelete_VND() {
        def id

        // we'll test multiple representations within this test method
        [ 'application/vnd.sungardhe.student.v0.01+xml',
          'application/vnd.sungardhe.student.v0.02+xml',
          'application/vnd.whatsamattau.student.v0.01+xml',
          'application/vnd.whatsamattau.student.v0.02+xml' ].each { contentType ->
            
            try {
                post( "/api/foobar" ) {   // 'POST' /api/foobar => 'create'
                    headers[ 'Content-Type' ] = contentType
                    headers[ 'Authorization' ] = authHeader()
                    body { """
                        <FooInstance apiVersion="1.0">
                            <Foo systemRequiredIndicator="N">
                                <Code>#W</Code>
                                <Description>Created via XML</Description>
                            </Foo>
                        </FooInstance>
                        """
                    }
                }
                assertStatus 201
                assertEquals contentType, page?.webResponse?.contentType
                def stringContent = page?.webResponse?.contentAsString
                def xml = new XmlSlurper().parseText( stringContent )

                id = xml.Foo[0]?.@id.text().toInteger() // we'll grab this first so subsequent failures don't prevent our finally block from deleting this new foo
                assertNotNull "Expected new foo with id but got: ${xml.Foo[0]?.toString()}", xml.Foo[0]?.@id?.text()?.toString()

                assertEquals 'FooInstance', xml.name()
                assertTrue "Ref element not as expected: ${xml.Ref}", "${xml.Ref}" ==~ /.*test-banner-core\/foo.*/
                def ref = "${xml.Ref}"
                assertEquals "Expected foo with code '#W' but got: ${xml.Foo[0]?.Code[0].text()}", '#W', xml.Foo[0]?.Code[0].text()

                put( "/api/foobar/$id" ) {  // 'PUT' /api/foobar => 'update'
                    headers[ 'Content-Type' ] = contentType
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
                assertEquals contentType, page?.webResponse?.contentType
                stringContent = page?.webResponse?.contentAsString
                xml = new XmlSlurper().parseText( stringContent )

                assertEquals 'FooInstance', xml.name()
                assertEquals "Ref element after 'update' not the same as that after 'create': ${xml.Ref}", ref, "${xml.Ref}"
                assertEquals "Expected id $id but got: ${xml.Foo[0]?.@id.text()}", id, xml.Foo[0]?.@id.text().toInteger()
                assertEquals "Expected foo with code '#W' but got: ${xml.Foo[0]?.Code[0].text()}", '#W', xml.Foo[0]?.Code[0].text()
                assertEquals "Expected foo with description 'Updated!' but got: ${xml.Foo[0]?.Description[0].text()}", 'Updated!', xml.Foo[0]?.Description[0].text()

            }
            finally {
                delete( "/api/foobar/$id" ) {
                    headers[ 'Content-Type' ] = contentType
                    headers[ 'Authorization' ] = authHeader()
                }
                assertStatus 200
                assertEquals 'application/xml', page?.webResponse?.contentType // we do not currently have a 'custom' delete confirmation message, so no custom MIME type is used here
                def stringContent = page?.webResponse?.contentAsString
                def xml = new XmlSlurper().parseText( stringContent )

                assertTrue "Response not as expected: ${xml}", "${xml}" ==~ /.*true.*/

                get( "/api/foobar/$id" ) {  // 'GET' /api/foobar/id => 'show'
                    headers[ 'Content-Type' ] = contentType
                    headers[ 'Authorization' ] = authHeader()
                }
                assertStatus 404
            }
        }
    }

}

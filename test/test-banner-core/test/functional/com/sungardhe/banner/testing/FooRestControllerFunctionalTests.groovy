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

/**
 * Functional tests of the Foo REST API.
 */
class FooRestControllerFunctionalTests extends BaseFunctionalTestCase {


    protected void setUp() {
        formContext = ['STVCOLL']
        super.setUp()

        login()
    }


    // -------------------------------- Test JSON Representations ---------------------------------


    void testList_JSON() {

        def pageSize = 5
        get( "/api/foo?max=$pageSize" ) {
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


    void testShow_JSON() {

        get( "/api/foo/1" ) {
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
            // 'POST' /api/college => 'create'
            post("/api/foo") {
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

            // 'PUT' /api/foo => 'update'
            put( "/api/foo/$id" ) {
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

            delete( "/api/foo/$id" ) {
                headers[ 'Content-Type' ] = 'application/json'
                headers[ 'Authorization' ] = authHeader()
            }
            assertStatus 200
            assertEquals 'application/json', page?.webResponse?.contentType

            def stringContent = page?.webResponse?.contentAsString
            def data = JSON.parse( stringContent )
            assertTrue "Response not as expected: ${data.success}", data.success

            get( "/api/foo/$id" ) {  // 'GET' /api/college/id => 'show'
                headers[ 'Content-Type' ] = 'application/json'
                headers[ 'Authorization' ] = authHeader()
            }
            assertStatus 404
        }
    }


    // -------------------------------- Test XML Representations ---------------------------------
    // Note: This controller uses default XML rendering -- see FooOverriddenFunctionalTests for
    //       an example of testing custom rendering.)


    void testShow_XML() {

        get( "/api/foo/1" ) {  // 'GET' /api/foo/id => 'show'
            headers[ 'Content-Type' ] = 'text/xml'
            headers[ 'Authorization' ] = authHeader()
        }
        assertStatus 200
        assertEquals 'text/xml', page?.webResponse?.contentType
        def stringContent = page?.webResponse?.contentAsString
        
        def xmlResultMap = new XmlSlurper().parseText( stringContent )
        def xmlFoo = xmlResultMap.entry.findAll { it.@key.text() == "data" }
        assertEquals "Expected Foo id of '1' but got: ${xmlFoo[0].@id.text().toString()}", '1', xmlFoo[0]?.@id.text().toString()
   }


    void testList_XML() {

        def pageSize = 15
        get( "/api/foo?max=$pageSize" ) {
            headers[ 'Content-Type' ] = 'text/xml'
            headers[ 'Authorization' ] = authHeader()
        }

        assertStatus 200
        assertEquals 'text/xml', page?.webResponse?.contentType
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

            // 'POST' /api/foo => 'create'
            post( "/api/foo" ) {
                headers[ 'Content-Type' ] = 'text/xml'
                headers[ 'Authorization' ] = authHeader()
                body { xmlBody }
            }
            assertStatus 201
            assertEquals 'text/xml', page?.webResponse?.contentType
            def stringContent = page?.webResponse?.contentAsString
            def xmlResultMap = new XmlSlurper().parseText( stringContent )
            def xmlFoos = xmlResultMap.entry.findAll { it.@key.text() == "data" }
            
            // get the id first, so a subsequent failure won't prevent our catch block from deleting this...
            id = xmlFoos[0]?.@id?.text()?.toInteger()
            assertTrue id != null
            def version = xmlFoos[0]?.@version?.text()?.toInteger()
            assertTrue version != null
            assertEquals code, xmlFoos[0].code.text()

            // 'PUT' /api/foo => 'update'
            put( "/api/foo/$id" ) {
                headers[ 'Content-Type' ] = 'text/xml'
                headers[ 'Authorization' ] = authHeader()
                body { """
                    <foo id="$id" version="$version">
                        <description>Updated</description>
                    </foo>"""
                }
            }
            assertStatus 200
            assertEquals 'text/xml', page?.webResponse?.contentType
            stringContent = page?.webResponse?.contentAsString
            xmlResultMap = new XmlSlurper().parseText( stringContent )
            xmlFoos = xmlResultMap.entry.findAll { it.@key.text() == "data" }

            def updatedFooId = xmlFoos[0]?.@id?.text()?.toInteger()
            assertEquals id, updatedFooId
            def updatedVersion = xmlFoos[0]?.@version?.text()?.toInteger()
            assertEquals version + 1, updatedVersion
            assertEquals code, xmlFoos[0].code.text()
            assertEquals 'Updated', xmlFoos[0].description.text()

        } finally {

            delete( "/api/foo/$id" ) {
                headers[ 'Content-Type' ] = 'text/xml'
                headers[ 'Authorization' ] = authHeader()
            }
            assertStatus 200
            assertEquals 'text/xml', page?.webResponse?.contentType
            def stringContent = page?.webResponse?.contentAsString
            def xml = new XmlSlurper().parseText( stringContent )

            assertTrue "Response not as expected: ${xml}", "${xml}" ==~ /.*true.*/

            get( "/api/foo/$id" ) {  // 'GET' /api/foo/id => 'show'
                headers[ 'Content-Type' ] = 'text/xml'
                headers[ 'Authorization' ] = authHeader()
            }
            assertStatus 404
        }

   }

}

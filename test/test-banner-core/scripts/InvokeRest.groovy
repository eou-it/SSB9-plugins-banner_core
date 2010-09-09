/** *****************************************************************************
 © 2010 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */

includeTargets << grailsScript( "Init" )

@Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.5.0')
def authString() {
    "Basic " + "grails_user:u_pick_it".bytes.encodeBase64().toString()
}

// ************* NOTICE: WORK IN PROCESS -- NOT FULLY FUNCTIONAL **************
//
// This script currently supports only 'list', by using GET against a URL without an id.
// Supplying an id currently results in an exception, as the id isn't mapped correctly.
// Other methods (POST/PUT/DELETE) are not yet implemented.
//
target( main: "Programmatically interact with a resource, usage: ModelClient model: foo, httpMethod: GET, mimeType: XML, id: 123") {
    depends( parseArguments )

    def input = argsMap
    def modelUriName = input.model ?: 'foo'
    def httpMethod = input.httpMethod ?: 'GET'
    def mimeType = input.mimeType ?: 'XML'
    def id = input.key

    def url
    if (id) {
        url = "http://localhost:8080/test-banner-core/api/$modelUriName/$id"
    } else {
        url = "http://localhost:8080/test-banner-core/api/$modelUriName"
    }

    println "Now invoking $httpMethod  $url"
    println "(requesting content-type $mimeType)"

    def fooClient = new groovyx.net.http.RESTClient( url )
    fooClient.contentType = mimeType //'application/vnd.sungardhe.student.v0.01+xml'
    fooClient.headers = [ Accept: "$mimeType",
                          'Content-Type': "$mimeType",
                          Authorization: "${authString()}" ]

    def response = fooClient.get( path: modelUriName )

    println "Response from $httpMethod: $response.data"
}

setDefaultTarget( main )


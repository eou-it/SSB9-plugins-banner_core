/*********************************************************************************
 Copyright 2009-2011 SunGard Higher Education. All Rights Reserved.
 This copyrighted software contains confidential and proprietary information of 
 SunGard Higher Education and its subsidiaries. Any use of this software is limited 
 solely to SunGard Higher Education licensees, and is further subject to the terms 
 and conditions of one or more written license agreements between SunGard Higher 
 Education and the licensee in question. SunGard is either a registered trademark or
 trademark of SunGard Data Systems in the U.S.A. and/or other regions and/or countries.
 Banner and Luminis are either registered trademarks or trademarks of SunGard Higher 
 Education in the U.S.A. and/or other regions and/or countries.
 **********************************************************************************/

includeTargets << grailsScript( "Init" )

@Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.5.0')
def authString() {
    "Basic " + "grails_user:u_pick_it".bytes.encodeBase64().toString()
}

// ************* NOTICE: WORK IN PROCESS -- NOT FULLY FUNCTIONAL **************
//
// usage:
// grails invoke-rest -model=<lower case model name> -httpMethod=<get|post|put|delete> -format=<mime type> -id: 123 -host=<host:port> -body=<file path name>
// example 1:
// grails invoke-rest -model=foo -httpMethod=GET -format=application/vnd.sungardhe.student.v0.03+xml -id=31
//                                (note http method can be capitalized if desired)
// example 2:
// grails invoke-rest -model=foo -httpMethod=put -format=application/vnd.sungardhe.student.v0.03+xml -id=31 -body=body_content.txt
// (where body_content.txt contains:
//     <FooInstance apiVersion="1.0">
//         <Foo id='31' systemRequiredIndicator="N" optimisticLockVersion="0">
//             <Description>Updated!</Description>
//         </Foo>
//     </FooInstance>
//
target( main: "Programmatically interact with a resource") {
    depends( parseArguments )

    def host = argsMap.host ?: 'localhost:8080'
    def modelUriName = argsMap.model ?: 'foo'
    def httpMethod = argsMap.httpMethod?.toLowerCase() ?: 'get'
    def format = argsMap.format ?: 'application/xml'
    def id = argsMap.id
    def url = "http://$host/test-banner-core/api"
    def bodyContent

    def restClient = new groovyx.net.http.RESTClient()
    restClient.uri = url
    restClient.client.params.setBooleanParameter 'http.protocol.expect-continue', false
    restClient.contentType = "text/plain"// prevent automatic parsing (we pass what the server will use in the header)

    restClient.auth.basic "grails_user", "u_pick_it"
    restClient.headers = [ 'Accept': "$format",
                           'Content-Type': "$format" ]

    // note the 'api' portion must be in both the uri AND the path.  Strange, but it needs to be this way...
    def path = id ? "api/$modelUriName/$id" : "api/$modelUriName"

    println "About to invoke $httpMethod --> $url   (will request content-type $format)"

    def response
    if (httpMethod == 'post' || httpMethod == 'put') {
        if (argsMap.body) {
            bodyContent = new File( argsMap.body ).getText()
            println "Going to submit body content of: $bodyContent"
            response = restClient."${httpMethod.toLowerCase()}"( path: path, body: bodyContent )
        }
        else {
            println "***ERROR: Cannot use $httpMethod without specifying a body"
            exit( 1 )
        }
    } else {
        response = restClient."${httpMethod.toLowerCase()}"( path: path )
    }

    println "Response from $httpMethod: ${response?.data}"
}


setDefaultTarget( main )


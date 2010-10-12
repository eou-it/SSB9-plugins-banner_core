/** *****************************************************************************
 © 2010 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */

import com.sungardhe.banner.configuration.ApplicationConfigurationUtils as ConfigFinder

import grails.plugins.springsecurity.SecurityConfigType


grails.config.locations = [] // leave this initialized to an empty list, and add your locations
                             // in the EXTERNALIZED CONFIGURATION section below.
def locationAdder = ConfigFinder.&addLocation.curry( grails.config.locations )


// ******************************************************************************
//
//                       +++ EXTERNALIZED CONFIGURATION +++
//
// ******************************************************************************
// config locations should be added to the following map. They will be loaded based upon this search order:
// 1. Load the configuration file if its location was specified on the command line using -DmyEnvName=myConfigLocation
// 2. Load the configuration file if it exists within the user's .grails directory (i.e., convenient for developers)
// 3. Load the configuration file if its location was specified as a system environment variable
//
// Map [ environment variable or -D command line argument name : file path ]
[ bannerGrailsAppConfig: "${userHome}/.grails/banner_on_grails-local-config.groovy",
  customRepresentationConfig: "grails-app/conf/CustomRepresentationConfig.groovy",
].each { envName, defaultFileName -> locationAdder( envName, defaultFileName ) }


// ******************************************************************************
//
//                    +++ INSTANCE-SPECIFIC CONFIGURATION +++
//
// ******************************************************************************

// Developers: You should create a small configuration file that contains your own specific
// configuration (e.g., URIs, usernames, etc.) and that resides at the location specified here:
//     ${userHome}/.grails/banner_on_grails-local-config.groovy

/* ***************************** EXAMPLE local file ******************************
def username = "banproxy"
def password = "u_pick_it"
def url      ="jdbc:oracle:thin:@winxp-50174ccec:1521:ban83" // CHANGE THIS FOR YOUR DATABASE!
def driver   = "oracle.jdbc.OracleDriver"

// Note: When using the com.elvyx.Driver, you may run the standalone elvyx client to see the actual SQL being executed.
// You must download the elvyx-1.0.24_beta.zip from http://sourceforge.net/projects/elvyx/files and unzip where you want to keep it.
// Note: You do NOT need to add the jar file to the project -- it is already present.
// Next, Update the url below in this file for your environment, then
//       Run the elvyz.bat or elvyz.sh file to launch the swing UI, and run your tests or the grails application.

myDataSource.username = username
myDataSource.password = password

myDataSource.driver = driver
// myDataSource.driver = "com.elvyx.Driver"

myDataSource.url = url
// myDataSource.url = "jdbc:elvyx://localhost:4448/?elvyx.real_driver=$driver&elvyx.real_jdbc=$url&user=$username&password=$password"
********************************************************************************* */


grails.mime.file.extensions = true // enables the parsing of file extensions from URLs into the request format
grails.mime.use.accept.header = false
grails.mime.types = [ html: ['text/html','application/xhtml+xml'],
                      xml: ['text/xml', 'application/xml', 'application/vnd.sungardhe.student.v0.01+xml'],
                      text: 'text/plain',
                      js: 'text/javascript',
                      rss: 'application/rss+xml',
                      atom: 'application/atom+xml',
                      css: 'text/css',
                      csv: 'text/csv',
                      all: '*/*',
                      json: ['application/json','text/json'],
                      form: 'application/x-www-form-urlencoded',
                      multipartForm: 'multipart/form-data'
                    ]



// The default codec used to encode data with ${}
grails.views.default.codec = "html" // none, html, base64  **** Setting this to html will ensure html is escaped, to prevent XSS attack ****
grails.views.gsp.encoding = "UTF-8"

grails.converters.domain.include.version = true
grails.converters.encoding = "UTF-8"
grails.converters.json.date = "javascript"
grails.converters.json.pretty.print = true


// enabled native2ascii conversion of i18n properties files
grails.enable.native2ascii = true

// enable GSP preprocessing: replace head -> g:captureHead, title -> g:captureTitle, meta -> g:captureMeta, body -> g:captureBody
grails.views.gsp.sitemesh.preprocess = true

// set per-environment serverURL stem for creating absolute links
environments {
    production {
        grails.serverURL = "http://localhost:8080/${appName}"
    }
    development {
        grails.serverURL = "http://localhost:8080/${appName}"
    }
    test {
        grails.serverURL = "http://localhost:8080/${appName}"
    }
}


// ******************************************************************************
//
//                       +++ DATA ORIGIN CONFIGURATION +++
//
// ******************************************************************************
// This field is a Banner standard, along with 'lastModifiedBy' and lastModified.
// These properties are populated automatically before an entity is inserted or updated
// within the database. The lastModifiedBy uses the username of the logged in user,
// the lastModified uses the current timestamp, and the dataOrigin uses the value
// specified here:
dataOrigin = "Banner"

// ******************************************************************************
//
//                       +++ LOGGER CONFIGURATION +++
//
// ******************************************************************************
// See http://grails.org/doc/1.1.x/guide/3.%20Configuration.html#3.1.2%20Logging
// for more information about log4j configuration.
log4j = {
    appenders {
    	file name:'file', file:'target/logs/development.log'
    }
    root {
        off 'stdout', 'file'
        additivity = true
    }


    // Configure logging for other classes (e.g., in src/ or grails-app/utils/) here:
//  off  'com.sungardhe.banner.security'
//  off  'com.sungardhe.banner.db'
//  off  'com.sungardhe.banner.student'
//    all 'com.sungardhe.banner.testing.FooController'
//    all 'com.sungardhe.banner.testing.FooService'

    info 'com.sungardhe.banner.representations'
    info 'com.sungardhe.banner.supplemental.SupplementalDataService'

//    debug 'org.apache.http.headers'
//    debug 'org.apache.http.wire'

    // Grails framework classes
//  off    'org.codehaus.groovy.grails.web.servlet'        // controllers
//  off    'org.codehaus.groovy.grails.web.pages'          // GSP
//  off    'org.codehaus.groovy.grails.web.sitemesh'       // layouts
//  all    'org.codehaus.groovy.grails.web.mapping.filter' // URL mapping
//  all    'org.codehaus.groovy.grails.web.mapping'        // URL mapping
//	off    'org.codehaus.groovy.grails.commons'            // core / classloading
//	off    'org.codehaus.groovy.grails.plugins'            // plugins
//	off    'org.codehaus.groovy.grails.orm.hibernate'      // hibernate integration
//	off    'org.springframework'                           // Spring IoC
//	off    'org.hibernate'                                 // hibernate ORM

//	all    'grails.plugins.springsecurity'
//	all    'org.springframework.security'
//    all    'com.sungardhe.banner.security.BannerAccessDecisionVoter'

// Grails provides a convenience for enabling logging within artefacts, using 'grails.app.XXX'.
// Unfortunately, this configuration is not effective when 'mixing in' methods that perform logging.
// Therefore, for controllers and services it is recommended that you enable logging using the controller
// or service class name (see above 'class name' based configurations).  For example:
//     all  'com.sungardhe.banner.testing.FooController' // turns on all logging for the FooController
//
// off 'grails.app' // apply to all artefacts
// off 'grails.app.<artefactType>.ClassName // where artefactType is in:
        //  bootstrap  - For bootstrap classes
        //  dataSource - For data sources
        //  tagLib     - For tag libraries
        //  service    // Not effective with mixins -- see comment above
        //  controller // Not effective with mixins -- see comment above
        //  domain     - For domain entities
}


// ******************************************************************************
//
//                       +++ FORM-CONTROLLER MAP +++
//
// ******************************************************************************
// This map relates controllers to the Banner forms that it replaces.  This map
// supports 1:1 and 1:M (where a controller supports the functionality of more than
// one Banner form.  This map is critical, as it is used by the security framework to
// set appropriate Banner security role(s) on a database connection. For example, if a
// logged in user navigates to the 'medicalInformation' controller, when a database
// connection is attained and the user has the necessary role, the role is enabled
// for that user and Banner object.
formControllerMap = [
        'foo' : [ 'STVCOLL' ],
        'foobar' : [ 'STVCOLL' ],
        'nope' : [ 'NOPE' ], // not a real controller - but we'll never get that far...
]

// The bannerAccessDecisionVoter is registered by the banner-core plugin, and needs to be used
// before the normal 'roleVoter' (as we will grant access without requiring roles to be specified per URL
// within the interceptUrlMap below.)
//grails.plugins.springsecurity.voterNames = [ 'authenticatedVoter', 'bannerAccessDecisionVoter', 'roleVoter' ]
grails.plugins.springsecurity.useRequestMapDomainClass = false
grails.plugins.springsecurity.providerNames = ['bannerAuthenticationProvider']
//grails.plugins.springsecurity.rejectIfNoRule = true

grails.plugins.springsecurity.filterChain.chainMap = [
    '/api/**': 'authenticationProcessingFilter,basicAuthenticationFilter,securityContextHolderAwareRequestFilter,anonymousProcessingFilter,basicExceptionTranslationFilter,filterInvocationInterceptor',
    '/**': 'securityContextPersistenceFilter,logoutFilter,authenticationProcessingFilter,securityContextHolderAwareRequestFilter,anonymousProcessingFilter,exceptionTranslationFilter,filterInvocationInterceptor'
]

grails.plugins.springsecurity.securityConfigType = SecurityConfigType.InterceptUrlMap

grails.plugins.springsecurity.interceptUrlMap = [
        '/': ['IS_AUTHENTICATED_ANONYMOUSLY'],
        '/zkau/**': ['IS_AUTHENTICATED_ANONYMOUSLY'],
        '/zkau**': ['IS_AUTHENTICATED_ANONYMOUSLY'],
        '/login/**': ['IS_AUTHENTICATED_ANONYMOUSLY'],
        '/logout/**': ['IS_AUTHENTICATED_ANONYMOUSLY'],
        '/js/**': ['IS_AUTHENTICATED_ANONYMOUSLY'],
        '/css/**': ['IS_AUTHENTICATED_ANONYMOUSLY'],
        '/images/**': ['IS_AUTHENTICATED_ANONYMOUSLY'],
        '/plugins/**': ['IS_AUTHENTICATED_ANONYMOUSLY'],
        '/errors/**': ['IS_AUTHENTICATED_ANONYMOUSLY'],

         // ALL URIs specified with the BannerAccessDecisionVoter.ROLE_DETERMINED_DYNAMICALLY
         // 'role' (it's not a real role) will result in authorization being determined based 
         // upon a user's role assignments to the corresponding form (see 'formControllerMap' above).
         // Note: This 'dynamic form-based authorization' is performed by the BannerAccessDecisionVoter 
         // registered as the 'roleVoter' within Spring Security.
         //
         // Only '/name_used_in_formControllerMap/' and '/api/name_used_in_formControllerMap/'
         // URL formats are supported.  That is, the name_used_in_formControllerMap must be first, or
         // immediately after 'api' -- but it cannot be otherwise nested. URIs may be protected 
         // by explicitly specifying true roles instead -- as long as ROLE_DETERMINED_DYNAMICALLY
         // is NOT specified. 
         //
        '/**': [ 'ROLE_DETERMINED_DYNAMICALLY' ]
]


// -------------------- CAS configurations ---------------------------------------------------------- //

grails.plugins.springsecurity.cas.serverUrlPrefix = 'http://localhost:8080/cas'
grails.plugins.springsecurity.cas.loginUri = '/login'
grails.plugins.springsecurity.cas.serviceUrl = 'http://localhost:8090/banner_on_grails/j_spring_cas_security_check'
grails.plugins.springsecurity.cas.serverName = 'http://localhost:8090'
grails.plugins.springsecurity.cas.sendRenew = false
grails.plugins.springsecurity.cas.proxyCallbackUrl = 'http://localhost:8090/banner_on_grails/secure/receptor'
grails.plugins.springsecurity.cas.proxyReceptorUrl = '/secure/receptor'
grails.plugins.springsecurity.cas.useSingleSignout = true

banner {
    sso {
      authenticationProvider = 'default'
      authenticationAssertionAttribute = 'udcId'
    }
}


// ******************************************************************************
//
//                 +++ RESTful RESOURCE REPESENTATION SUPPORT +++
//
// ******************************************************************************
// Representations officially supported within Banner. Custom representations should not be added to this map,
// but should instead be added to the 'CustomRepresentationConfig.groovy' file, within a 'customRepresentationHandlerMap'
// that follows the same structure as the map below.
bannerRepresentationHandlerMap =
    // Note: 'application/vnd.sungardhe.student.v0.01+xml' is supported directly within FooController and is consequently not registered here.
    //       (although it actually uses the the 'application/vnd.sungardhe.student.v0.02+xml' support found below, for convenience).
    [ "application/vnd.sungardhe.student.v0.02+xml":
        [ "Foo": // prefer to use fully qualified class names, but short names are also handled
            [ paramsExtractor:  { request ->
                                def xml = request.XML.Foo[0]
                                def props = [:]
                                if (xml.@id?.text())                           props.id                 = xml.@id.toInteger()
                                if (xml.@systemRequiredIndicator?.text())      props.systemRequiredIndicator = xml.@systemRequiredIndicator?.text()
                                if (xml.@lastModifiedBy?.text())               props.lastModifiedBy     = xml.@lastModifiedBy.text()
                                if (xml.@lastModified?.text())                 props.lastModified       = xml.@lastModified.text()
                                if (xml.@dataOrigin?.text())                   props.dataOrigin         = xml.@dataOrigin.text()
                                if (xml.@optimisticLockVersion?.text())        props.version            = xml.@optimisticLockVersion.toInteger()

                                if (xml.Code?.text())                          props.code               = xml.Code.text()
                                if (xml.Description?.text())                   props.description        = xml.Description.text()

                                if (xml.AddressStreetLine1?.text())            props.addressStreetLine1 = xml.AddressStreetLine1?.text()
                                if (xml.AddressStreetLine2?.text())            props.addressStreetLine2 = xml.AddressStreetLine2?.text()
                                if (xml.AddressStreetLine3?.text())            props.addressStreetLine3 = xml.AddressStreetLine3?.text()
                                if (xml.AddressStreetLine4?.text())            props.addressStreetLine4 = xml.AddressStreetLine4?.text()
                                if (xml.HouseNumber?.text())                   props.houseNumber        = xml.HouseNumber?.text()

                                if (xml.AddressCity?.text())                   props.addressCity        = xml.AddressCity?.text()
                                if (xml.AddressState?.text())                  props.addressState       = xml.AddressState?.text()
                                if (xml.AddressCountry?.text())                props.addressCountry     = xml.AddressCountry?.text()
                                if (xml.AddressZipCode?.text())                props.addressZipCode     = xml.AddressZipCode?.text()

                                if (xml.VoiceResponseMessageNumber?.text())    props.voiceResponseMessageNumber    = xml.VoiceResponseMessageNumber?.text()
                                if (xml.StatisticsCanadianInstitution?.text()) props.statisticsCanadianInstitution = xml.StatisticsCanadianInstitution?.text()
                                if (xml.DistrictDivision?.text())              props.districtDivision   = xml.DistrictDivision?.text()
                                props
                            },
              singleBuilder: { renderDataMap -> [ template: "/foo/single.v1.0.xml",
                                                   model: [ foo: renderDataMap.data, refBase: renderDataMap.refBase ] ] },
              collectionBuilder: { renderDataMap -> [ template: "/foo/list.v1.0.xml",
                                                      model: [ fooList: renderDataMap.data, totalCount: renderDataMap.totalCount,
                                                               refBase: renderDataMap.refBase ] ] }
            ], // end Foo support

            // next model supported by this same MIME type should go here...
         ],

      // This second example to support v0.03 for Foo use an external groovy file -- please make sure the groovy file is available on the classpath
      "application/vnd.sungardhe.student.v0.03+xml":
          [ "Foo": "com.sungardhe.banner.testing.FooMarkupBuilderBasedRepresentationHandler" ],

      // next MIME type would go here
] // end bannerRepresentationHandlerMap

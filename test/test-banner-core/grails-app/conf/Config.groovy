/** *****************************************************************************
 © 2010 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */

import grails.plugins.springsecurity.SecurityConfigType
import org.codehaus.groovy.grails.commons.ConfigurationHolder

// You must create a small configuration file that contains your own specific
// configuration (e.g., URIs, usernames, etc.) and that resides at the location specified here:
//
grails.config.locations = [ "file:${userHome}/.grails/banner_on_grails-local-config.groovy" ]

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
grails.views.default.codec="html" // none, html, base64  **** Charlie note: Setting this to html will ensure html is escaped, to prevent XSS attack ****
grails.views.gsp.encoding="UTF-8"
grails.converters.encoding="UTF-8"

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

    // Configure logging for our grails 'artefacts' aka artifacts -- models, controllers, etc. -- here:
    // Note: You may use 'grails.app.<artefactType>.ClassName where artefactType is in:
    //         bootstrap  - For bootstrap classes
    //         dataSource - For data sources
    //         tagLib     - For tag libraries
    //         service    - For service classes
    //         controller - For controllers
    //         domain     - For domain entities 
    off  'grails.app' // The artefact may be omitted to apply to all artefacts  
        
    // Configure logging for other classes (e.g., in src/ or grails-app/utils/) here:
    off  'com.sungardhe.banner.security'
    off  'com.sungardhe.banner.db'
    off 'com.sungardhe.banner.student'
    
    // Grails framework classes
//  off    'org.codehaus.groovy.grails.web.servlet'        // controllers
//  off    'org.codehaus.groovy.grails.web.pages'          // GSP
//  off    'org.codehaus.groovy.grails.web.sitemesh'       // layouts
all    'org.codehaus.groovy.grails.web.mapping.filter' // URL mapping
all    'org.codehaus.groovy.grails.web.mapping'        // URL mapping
//	off    'org.codehaus.groovy.grails.commons'            // core / classloading
//	off    'org.codehaus.groovy.grails.plugins'            // plugins
//	off    'org.codehaus.groovy.grails.orm.hibernate'      // hibernate integration
//	off    'org.springframework'                           // Spring IoC
//	off    'org.hibernate'                                 // hibernate ORM
	
//	off    'grails.plugins.springsecurity'
//	off    'org.springframework.security'
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
    'foorestful' : [ 'STVCOLL' ]
]


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
        '/foo/**': ['ROLE_STVCOLL_BAN_DEFAULT_M'],
        '/api/foo/**': ['ROLE_STVCOLL_BAN_DEFAULT_M'],
        '/**': ['ROLE_ANY_FORM_BAN_DEFAULT_M']
]
        

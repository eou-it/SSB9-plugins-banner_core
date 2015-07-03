import net.hedtech.banner.configuration.ApplicationConfigurationUtils

/*******************************************************************************
Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
*******************************************************************************/

// ******************************************************************************
//                       +++ DATA ORIGIN CONFIGURATION +++
// ******************************************************************************
// This field is a Banner standard, along with 'lastModifiedBy' and lastModified.
// These properties are populated automatically before an entity is inserted or updated
// within the database. The lastModifiedBy uses the username of the logged in user,
// the lastModified uses the current timestamp, and the dataOrigin uses the value
// specified here:
//
dataOrigin = "Banner"

grails.doc.authors = '''Prepared by: Ellucian
                        |4 Country View Road Malvern, Pennsylvania 19355 United States of America (800) 522 - 4827'''.stripMargin()

grails.doc.footer = '''Contains confidential and proprietary information of Ellucian and its subsidiaries.'''

grails.doc.license = '''Use of these materials is limited to Ellucian licensees, and is subject to the terms and conditions of one or more written license agreements between Ellucian and the licensee in question.
                        |In preparing and providing this publication, Ellucian is not rendering legal, accounting, or other similar professional services. Ellucian makes no claims that an institution's use of this publication or the software for which it is provided will insure compliance with applicable federal or state laws, rules, or regulations. Each organization should seek legal, accounting and other similar professional services from competent providers of the organization’s own choosing.
                        |'''.stripMargin()

grails.doc.copyright = '''© 2010-2012 Ellucian. All rights reserved.
                          |Use of these materials is limited to Ellucian licensees, and is subject to the terms and conditions of one or more written license agreements between Ellucian and the licensee in question.
                          |In preparing and providing this publication, Ellucian is not rendering legal, accounting, or other similar professional services. Ellucian makes no claims that an institution's use of this publication or the software for which it is provided will insure compliance with applicable federal or state laws, rules, or regulations. Each organization should seek legal, accounting and other similar professional services from competent providers of the organization’s own choosing.
                          |'''.stripMargin()

grails.doc.images      = new File( "src/docs/images" )
grails.doc.logo        = '''<img src="../img/banner9_small.png">'''
grails.doc.sponsorLogo = '''<img src="../img/ellucian_small.png">'''

grails.doc.alias.about      = "1. About"
grails.doc.alias.user       = "2. User Guide"
grails.doc.alias.overview   = "2.1 Architecture Overview"
grails.doc.alias.security   = "2.2 Application Security"
grails.doc.alias.services   = "2.3 Services"
grails.doc.alias.dev        = "3. Developer Guide"

grails.config.locations = [] // leave this initialized to an empty list, and add your locations
// in the EXTERNALIZED CONFIGURATION section below.
def locationAdder = ApplicationConfigurationUtils.&addLocation.curry( grails.config.locations )
[ BANNER_APP_CONFIG:              "banner_configuration.groovy",
  BANNER_TEST_APP_CONFIG:         "${appName}_configuration.groovy",
  releaseProperties:              "release.properties",
].each { envName, defaultFileName -> locationAdder( envName, defaultFileName ) }

// In case logging is problematic, we'll just write this to the console immediately
grails.config.locations.each {
    println "Using configuration: " + it
}


// Code Coverage configuration
coverage {
	enabledByDefault = false
}

formControllerMap = [
        'foo':        [ 'GUAGMNU', 'SELFSERVICE' ],
        'foos':       [ 'GUAGMNU', 'SELFSERVICE' ],
        'restfulapi': [ 'GUAGMNU', 'SELFSERVICE' ],
        'foobar':     [ 'GUAGMNU', 'SELFSERVICE' ],
        'term':       [ 'GUAGMNU' ],
        'nope':       [ 'NOPE' ], // not a real controller - used for testing...
]

grails.views.default.codec="none" // none, html, base64
grails.views.gsp.encoding="UTF-8"
privacy.codes = "INT NAV UNI"

// Uncomment and edit the following lines to start using Grails encoding & escaping improvements

/* remove this line 
// GSP settings
grails {
    views {
        gsp {
            encoding = 'UTF-8'
            htmlcodec = 'xml' // use xml escaping instead of HTML4 escaping
            codecs {
                expression = 'html' // escapes values inside null
                scriptlet = 'none' // escapes output from scriptlets in GSPs
                taglib = 'none' // escapes output from taglibs
                staticparts = 'none' // escapes output from static template parts
            }
        }
        // escapes all not-encoded output at final stage of outputting
        filteringCodecForContentType {
            //'text/html' = 'html'
        }
    }
}
remove this line */

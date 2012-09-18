/*******************************************************************************
Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
*******************************************************************************/

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
grails.doc.alias.rest       = "2.4 REST"
grails.doc.alias.dev        = "3. Developer Guide"


// CodeNarc rulesets
codenarc.ruleSetFiles="rulesets/banner.groovy"
codenarc.reportName="target/CodeNarcReport.html"
codenarc.propertiesFile="grails-app/conf/codenarc.properties"


// Code Coverage configuration
coverage {
	enabledByDefault = false
}

grails.views.default.codec="none" // none, html, base64
grails.views.gsp.encoding="UTF-8"

/*********************************************************************************
 Copyright 2009-2012 SunGard Higher Education. All Rights Reserved.
 This copyrighted software contains confidential and proprietary information of
 SunGard Higher Education and its subsidiaries. Any use of this software is limited
 solely to SunGard Higher Education licensees, and is further subject to the terms
 and conditions of one or more written license agreements between SunGard Higher
 Education and the licensee in question. SunGard is either a registered trademark or
 trademark of SunGard Data Systems in the U.S.A. and/or other regions and/or countries.
 Banner and Luminis are either registered trademarks or trademarks of SunGard Higher
 Education in the U.S.A. and/or other regions and/or countries.
 **********************************************************************************/
package net.hedtech.banner.ui

/**
 * Inserts custom css and/or javascript that is present.
 *
 * It'll inspect the css and js directory for bannerSelfService-custom.css and bannerSelfService-custom.js files respectively.
 *t'll then inspect for controller and action specific css and javascript.  By placing a CSS and JS files
  * in the following structure:
 * I
 *
 * css/views/<controller name>/<action name>-custom.css
 * js/views/<controller name>/<action name>-custom.js
 *
 * E.g.
 * css/views/facultyGradeEntry/facultyGradeEntry-custom.css will add this CSS only for the controller 'facultyGradeEntry'
 * and the action 'facultyGradeEntry'.
 */
class CustomResourcesTagLib {

    def customStylesheetIncludes = { attrs ->
        def controller = attrs.controller ?: controllerName
        def action = attrs.action ?: actionName

//        // Check to see bannerSelfService-custom.css exists\r
        writeCssIfExists( out, "css/bannerSelfService-custom.css" )
        // Determine the current page
        writeCssIfExists( out, "css/views/$controller/${action}-custom.css" )
    }

    def customJavaScriptIncludes = { attrs ->
        def controller = attrs.controller ?: controllerName
        def action = attrs.action ?: actionName
//        // Check to see bannerSelfService-custom.js exists\r
        writeJavaScriptIfExists( out, "js/bannerSelfService-custom.js" )

        // Determine the current page
        writeJavaScriptIfExists( out, "js/views/$controller/${action}-custom.js" )
    }


    def specScriptIncludes = { attrs ->
        def name = attrs.name

        writeJavaScriptIfExists( out, "js/specs/${name}.spec.js" )
    }

    private resourceExists( resPath ) {
        return grailsApplication.parentContext.getResource( resPath ).file.exists()
    }


    private writeJavaScriptIfExists( writer, js ) {
        if (resourceExists(js)) {
            def baseUri = grailsAttributes.getApplicationUri(request)

            writer << r.external(uri: (baseUri.endsWith('/') ? '' : '/') + js , type: 'js', disposition: 'defer')
        }
    }

    private writeCssIfExists( writer, css ) {
        if (resourceExists(css)) {
            def baseUri = grailsAttributes.getApplicationUri(request)

            writer << r.external(uri: (baseUri.endsWith('/') ? '' : '/') + css , type: 'css')
        }
    }
}

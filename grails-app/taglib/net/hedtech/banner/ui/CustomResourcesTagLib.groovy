/*******************************************************************************
 Copyright 2009-2020 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.ui

import groovy.util.logging.Slf4j

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
@Slf4j
class CustomResourcesTagLib {

    def customStylesheetIncludes = { attrs ->
        def controller = attrs.controller ?: controllerName
        def action = attrs.action ?: actionName
        log.debug("Controller for this page: " + controller + " and action is " + action)
//        // Check to see bannerSelfService-custom.css exists\r
        writeCssIfExists( out, "css/bannerSelfService-custom.css" )
        // Determine the current page
        writeCssIfExists( out, "css/views/$controller/${action}-custom.css" )
    }

    def customJavaScriptIncludes = { attrs ->
        def controller = attrs.controller ?: controllerName
        def action = attrs.action ?: actionName
        log.debug("Controller for this page: " + controller + " and action is " + action)
//        // Check to see bannerSelfService-custom.js exists\r
        writeJavaScriptIfExists( out, "js/bannerSelfService-custom.js" )

        // Determine the current page
        writeJavaScriptIfExists( out, "js/views/$controller/${action}-custom.js" )
    }


/*    def specScriptIncludes = { attrs ->
        def name = attrs.name
        writeJavaScriptIfExists( out, "js/specs/${name}.spec.js" )
    }*/

    private resourceExists( resPath ) {
        return grailsApplication.parentContext.getResource( resPath ).file.exists()
    }


    private writeJavaScriptIfExists( writer, js ) {
        if (resourceExists(js)) {
            writer << "<script type='text/javascript' src='${resource(file: js)}'></script>"
           }
    }

    private writeCssIfExists( writer, css ) {
        if (resourceExists(css)) {
            writer << "<link rel='stylesheet' href='${resource(file: css)}'/>"
        }
    }
}

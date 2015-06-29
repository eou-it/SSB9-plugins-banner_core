/*******************************************************************************
Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
*******************************************************************************/
package net.hedtech.banner.testing.testing

/**
 * Controller supporting the 'Foo' model that relies on mixed-in RESTful CRUD methods.
 * Note that the actions used to expose a RESTful interface are provided by the RestfulControllerMixin class,
 * which is mixed in at runtime during bootstrap (see BannerCoreGrailsPlugin.groovy).
 *
 * Developer note: A minimal RESTful controller may consist of only two lines:
 *     static List mixInRestActions = [ 'show', 'list', 'create', 'update', 'destroy' ]
 *     def xyzService // injected by Spring
 * These two lines will provide a functioning RESTful controller using default Grails converters
 * for parsing and rendering.
 **/
class TermController {

    // wrap the 'message' invocation within a closure, so it can be passed
    // into an ApplicationException to localize error messages
    def localizer = { mapToLocalize ->
        this.message( mapToLocalize )
    }


    def zipService


    def main = {
          render view : "main"
    }

    def list = {
        def terms = Term.findAll()
          [ terms: terms ]
    }

    def testdual = {
        zipService.testSimple()
        render view : "main"
    }

}

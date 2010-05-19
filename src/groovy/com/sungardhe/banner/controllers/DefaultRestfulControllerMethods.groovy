/** *****************************************************************************

 © 2010 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
package com.sungardhe.banner.testing

import com.sungardhe.banner.exceptions.*

import grails.converters.JSON
import grails.converters.XML
import grails.util.GrailsNameUtils

import org.apache.log4j.Logger


/**
 * Methods that may be injected into controllers, giving them a default RESTful API 
 * supporting XML and JSON representations.
 *
 * Controllers that also render GSP/Zul pages are responsible for implementing separate 
 * actions that do not conflict with the default ones. (In general, this is expected 
 * to be limited to a 'main' action, as well as an index action that redirects to the main action.)
 *
 * NOTE: URI's containing 'api' will be directed to the appropriate controller 
 * action based upon HTTP type (see UriMappings.groovy).  These URIs will also 
 * be authenticated using BasicAuthentication. URIs that do not have 'api' in their name 
 * will be directed to the appropriate action using non-REST conventions 
 * (i.e., the action name must be part of the URI). 
 * 
 * Any of the default RESTful API actions may be overriden by implementing the action 
 * within the controller -- in which case the implementation in this class will not be injected.
 *
 * When using the default RESTful API actions, additional information must be specified in the 
 * controller when conventions cannot sufficiently define the desired behavior.
 *
 * Specifically, controllers using injected RESTful API methods should:
 *
 * Define closures that return appropriate returnMap maps:
 *
 * static def overrides = [
 *     save: [ success: [ success: true, 
 *                        data: foo, 
 *                        message: "${message( code: 'foo.created.message', \
 *                        args: [ message( code: 'foo.label', default: 'Foo'), foo.code ] )}" ]}
 *           ],
 *     update: [ success: [ success: true, 
 *                          data: foo, 
 *                          message: "${message( code: 'foo.created.message', \
 *                          args: [ message( code: 'foo.label', default: 'Foo'), foo.code ] )}" ]}
 *           ]
 *     delete: etc...
 * ]
 * 
 * 
 **/
class DefaultRestfulControllerMethods { 
        

    // The 'message' tag library is available to all controllers and is accessible via a 'message' method.
    // Here we wrap this method as a closure, so that we can pass it as an argument.
    // It is needed when asking a cought ApplicationException to prepare a localized response (i.e., returnMap). 
    protected def localizer = { mapToLocalize ->
        delegate.message( mapToLocalize )     // TODO: Use currying to expose this with the delegate already set by injectRestCrudActions
    }
    
    
    private static Map extractParams( delegate ) {  
        if (delegate.metaClass.respondsTo( delegate.class, "fromParams" )) {
            return delegate.fromParams()
        } else if (delegate.request.format ==~ /.*html.*/) {
            return delegate.params
        } else if (delegate.request.format ==~ /.*json.*/) {
            return JSON.parse( delegate.params?.data )
        } else if (delegate.request.format ==~ /.*xml.*/) {
            return XML.parse( delegate.params ) 
        } else {
            throw new RuntimeException( "@@r1:com.sungardhe.framework.unsupported_content_type:${delegate.request.format}" )
        }        
    }
        
    
    private static void renderResult( delegate, responseMap ) {
        if (delegate.request.format ==~ /.*html.*/) {
            delegate.response.setHeader( "Content-Type", "application/html" ) 
            delegate.render( responseMap )
        } else if (delegate.request.format ==~ /.*json.*/) {
            delegate.response.setHeader( "Content-Type", "application/json" ) 
            delegate?.renderResult( responseMap as JSON )
        } else if (delegate.request.format ==~ /.*xml.*/) {
            // TODO: Need to support multiple approaches / formats / versions / etc.
            delegate.response.setHeader( "Content-Type", "application/xml" ) 
            delegate.render( responseMap as XML )
        } else {
            throw new RuntimeException( "@@r1:com.sungardhe.framework.unsupported_content_type:${delegate.request.format}" )
        }        
    }
    
        
    public static void injectRestCrudActions( controllerClassOrInstance, domainClass ) {
        def controllerClass = (controllerClassOrInstance instanceof Class) ? controllerClassOrInstance : controllerClassOrInstance.class

        def log = Logger.getLogger( controllerClass.name )        
        String domainSimpleName = domainClass.simpleName 
        String serviceName = GrailsNameUtils.getPropertyNameRepresentation( "${domainSimpleName}Service" )

        // Note: The injected save/update/delete actions below are not able to directly render 
        // (e.g., using delegate.render( xxx)) hence we'll inject our own 'render' directly into 
        // the controller which will be used (and that will delegate to the 'normal' render method.)
        // Using 'render' directly results in an NPE at: PogoMetaClassSite.java:39
        controllerClass.metaClass.renderResult = { result ->
            render result
        }

        if (!controllerClass.metaClass.respondsTo( controllerClass, "save" )) {

            controllerClass.metaClass.save = { 
                log.trace "${controllerClass.simpleName}.save invoked with ${delegate.params}"
                def entity
                try {
                    def paramsMap = extractParams( delegate )
                    entity = delegate."$serviceName".create( paramsMap )
                    def successReturnMap
//                  if - else .... the delegate doesn't specify a custom success return map for 'save'.... TODO: implement!
                        successReturnMap = [ success: true, 
                                             data: entity, 
                                             message:  delegate.message( code: 'default.created.message',
                                                                         args: [ delegate.message( code: "${domainSimpleName}.label", default: "${domainSimpleName}" ), 
                                                                                 entity.id ] ) ]
                    renderResult( delegate, successReturnMap )
                } 
                catch (ApplicationException e) {
                    delegate.response.setStatus( e.httpStatusCode ) 
                    renderResult( delegate, (e.returnMap( localizer ) + [ data: entity ]) )
                } 
                catch (e) { // CI logging
                    delegate.response.setStatus( 500 ) 
                    log.error "Caught unexpected exception ${e.class.simpleName} which may be a candidate for wrapping in a ApplicationException, message: ${e.message}", e
                    renderResult( delegate, [ data: entity,
                                              success: false, 
                                              message: delegate.message( code: 'default.not.created.message', 
                                                                         args: [ delegate.message( code: "${domainSimpleName}.label", default: "${domainSimpleName}" ) ] ),
                                              errors: (e.hasProperty( 'errors' ) ? e.errors?.allErrors?.collect { delegate.message( error: it ) } : null),
                                              underlyingErrorMessage: e.message ]  )
                }               
            } // metaClass.save            
        } // if save
        
        if (!controllerClass.metaClass.respondsTo( controllerClass, "update" )) {

            controllerClass.metaClass.update = { 
        
                log.trace "${controllerClass.simpleName}.update invoked with ${delegate.params}"
                def entity
                try {
                    def paramsMap = extractParams( delegate )
                    entity = delegate."$serviceName".update( paramsMap )
                    def successReturnMap
//                  if - else .... the delegate doesn't specify a custom success return map for 'update' .... TODO: implement!
                        successReturnMap = [ success: true, 
                                             data: entity, 
                                             message:  delegate.message( code: 'default.updated.message',
                                                                         args: [ delegate.message( code: "${domainSimpleName}.label", default: "${domainSimpleName}" ), 
                                                                                 entity.id ] ) ]
                    renderResult( delegate, successReturnMap )
                } 
                catch (ApplicationException e) {
                    delegate.response.setStatus( e.httpStatusCode ) 
                    renderResult( delegate, (e.returnMap( localizer ) + [ data: entity ]) )
                } 
                catch (e) { // CI logging
                    delegate.response.setStatus( 500 ) 
                    log.error "Caught unexpected exception ${e.class.simpleName} which may be a candidate for wrapping in a ApplicationException, message: ${e.message}", e
                    renderResult( delegate, [ data: entity,
                                              success: false, 
                                              message: delegate.message( code: 'default.not.updated.message', 
                                                                         args: [ delegate.message( code: "${domainSimpleName}.label", default: "${domainSimpleName}" ) ] ),
                                              errors: (e.hasProperty( 'errors' ) ? e.errors?.allErrors?.collect { delegate.message( error: it ) } : null),
                                              underlyingErrorMessage: e.message ]  )
                }               
        
            } // metaClass.update
        } // if update()

        if (!controllerClass.metaClass.respondsTo( controllerClass, "delete" )) {

            controllerClass.metaClass.delete = { 

                log.trace "${controllerClass.simpleName}.delete invoked with ${delegate.params}"
                def paramsMap
                try {
                    paramsMap = extractParams( delegate )
                    delegate."$serviceName".delete( paramsMap )
                    def successReturnMap
//                  if - else .... the delegate doesn't specify a custom success return map for 'delete' .... TODO: implement!
                        successReturnMap = [ success: true, 
                                             data: null, 
                                             message:  delegate.message( code: 'default.deleted.message',
                                                                         args: [ delegate.message( code: "${domainSimpleName}.label", default: "${domainSimpleName}" ) ] ) ]
                    renderResult( delegate, successReturnMap )
                } 
                catch (ApplicationException e) {
                    delegate.response.setStatus( e.httpStatusCode ) 
                    renderResult( delegate, (e.returnMap( localizer ) + [ data: paramsMap ]) )
                } 
                catch (e) { // CI logging
                    delegate.response.setStatus( 500 ) 
                    log.error "Caught unexpected exception ${e.class.simpleName} which may be a candidate for wrapping in a ApplicationException, message: ${e.message}", e
                    renderResult( delegate, [ data: entity,
                                              success: false, 
                                              message: delegate.message( code: 'default.not.deleted.message', 
                                                                         args: [ delegate.message( code: "${domainSimpleName}.label", default: "${domainSimpleName}" ) ] ),
                                              errors: (e.hasProperty( 'errors' ) ? e.errors?.allErrors?.collect { delegate.message( error: it ) } : null),
                                              underlyingErrorMessage: e.message ]  )
                }               
            } // metaClass.delete
        } // if delete()

        if (!controllerClass.metaClass.respondsTo( controllerClass, "show" )) {

            controllerClass.metaClass.show = { 

                log.trace "${controllerClass.simpleName}.show invoked with ${delegate.params}"
                def paramsMap
                def entity
                try {
                    paramsMap = params // no conversion -- expect only Long id
                    entity = delegate."$serviceName".read( paramsMap.id )
                    def successReturnMap
//                  if - else .... the delegate doesn't specify a custom success return map for 'delete' .... TODO: implement!
                        successReturnMap = [ success: true, 
                                             data: entity, 
                                             message:  delegate.message( code: 'default.show.message',
                                                                         args: [ delegate.message( code: "${domainSimpleName}.label", default: "${domainSimpleName}" ) ] ) ]
                    renderResult( delegate, successReturnMap )
                } 
                catch (ApplicationException e) {
                    delegate.response.setStatus( e.httpStatusCode ) 
                    renderResult( delegate, (e.returnMap( localizer ) + [ data: paramsMap ]) )
                } 
                catch (e) { // CI logging
                    delegate.response.setStatus( 500 ) 
                    log.error "Caught unexpected exception ${e.class.simpleName} which may be a candidate for wrapping in a ApplicationException, message: ${e.message}", e
                    renderResult( delegate, [ data: entity,
                                              success: false, 
                                              message: delegate.message( code: 'default.not.shown.message', 
                                                                         args: [ delegate.message( code: "${domainSimpleName}.label", default: "${domainSimpleName}" ) ] ),
                                              errors: (e.hasProperty( 'errors' ) ? e.errors?.allErrors?.collect { delegate.message( error: it ) } : null),
                                              underlyingErrorMessage: e.message ]  )
                }               
            } // metaClass.show
        } // if show()
        
        if (!controllerClass.metaClass.respondsTo( controllerClass, "list" )) {

            controllerClass.metaClass.list = { 

                log.trace "${controllerClass.simpleName}.list invoked with ${delegate.params}"
                def paramsMap
                def entities
                try {
                    paramsMap = extractParams( delegate )
//                  if-else ... the delegate may substitute it's own implementation versus the default service.read (e.g., to use a query instead)
                    entities = delegate."$serviceName".list( paramsMap )
                    def successReturnMap
//                  if - else .... the delegate doesn't specify a custom success return map for 'list' .... TODO: implement!
                        successReturnMap = [ success: true, 
                                             data: entities, 
                                             message:  delegate.message( code: 'default.list.message',
                                                                         args: [ delegate.message( code: "${domainSimpleName}.label", default: "${domainSimpleName}" ) ] ) ]
                    renderResult( delegate, successReturnMap )
                } 
                catch (ApplicationException e) {
                    delegate.response.setStatus( e.httpStatusCode ) 
                    renderResult( delegate, (e.returnMap( localizer ) + [ data: paramsMap ]) )
                } 
                catch (e) { // CI logging
                    delegate.response.setStatus( 500 ) 
                    log.error "Caught unexpected exception ${e.class.simpleName} which may be a candidate for wrapping in a ApplicationException, message: ${e.message}", e
                    renderResult( delegate, [ data: entities,
                                              success: false, 
                                              message: delegate.message( code: 'default.not.listed.message', 
                                                                         args: [ delegate.message( code: "${domainSimpleName}.label", default: "${domainSimpleName}" ) ] ),
                                              errors: (e.hasProperty( 'errors' ) ? e.errors?.allErrors?.collect { delegate.message( error: it ) } : null),
                                              underlyingErrorMessage: e.message ]  )
                }               
            } // metaClass.list
        } // if list()

    }

/*    
    
    
    // If an exception occurs here, we won't really know how to handle... we'll let the errors controller deal with it.
    def list = { 
        params.max = Math.min( params.max ? params.max.toInteger() : 10, 100 ) 
        def results
        def totalCount
        if (params.query != null) {
            def q = "%" + params.query + "%"
            results = Foo.findAllByDescriptionIlikeOrCodeIlike( q, q )
            totalCount = Foo.countByDescriptionIlikeOrCodeIlike( q, q )
        } 
        else {
            results = fooService.list( params )
            totalCount = fooService.count()
        }
         
        withFormat {
            
            json {
                render( [ success: true, data: results, totalCount: totalCount ] as JSON )
            }
            
            xml {
                def refBase = "${request.scheme}://${request.serverName}:${request.serverPort}/${grailsApplication.metadata.'app.name'}/$controllerName"
                switch (request.getHeader( 'Content-Type' )) {
                    case 'application/vnd.sungardhe.student.v0.01+xml' :
                         response.setHeader( 'Content-Type', request.getHeader( 'Content-Type' ) )
                         render """<?xml version="1.0" encoding="UTF-8"?>
                                   <TestResponse>This pretend old version does not have any useful data!</TestResponse>
                                """
                         return
                    default :                
                         response.setHeader( "Content-Type", "text/xml" )
                         render( template:"list.v1.0.xml", model: [ fooList: results, totalCount: totalCount, refBase: refBase ] )
                }
            }
        } 
    }
*/                    
}

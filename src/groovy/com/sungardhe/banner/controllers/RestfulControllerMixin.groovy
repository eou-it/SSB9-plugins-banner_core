/** *****************************************************************************
 © 2010 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
package com.sungardhe.banner.controllers

import com.sungardhe.banner.exceptions.*

import grails.converters.JSON
import grails.converters.XML
import grails.util.GrailsNameUtils

import org.apache.log4j.Logger


/**
 * A mixin for controllers that provides actions needed for a full RESTful API supporting XML and JSON
 * representations.
 * Specifically, 'create', 'update', 'destroy' 'list', and 'show' actions are provided by this mixin.
 *
 * This class is mixed into controllers that contain this line:
 *     static List mixInRestActions = [ 'show', 'list', 'create', 'update', 'destroy' ]
 * during bootstrap (see BannerCoreGrailsPlugin.groovy).
 * The BannerCoreGrailsPlugin will also register the identified actions so that Grails URI mapping
 * recognizes these actions. Actions not identified in the above line contained in this class
 * will still be mixed-in to the controller, but they will not be accessible since they are
 * not registered.
 *
 * The standard REST API is:
 * http://the_host/the_app_name/the_controller       GET     --> 'list' action
 * http://the_host/the_app_name/the_controller/id    GET     --> 'show' action
 * http://the_host/the_app_name/the_controller       POST    --> 'create' action
 * http://the_host/the_app_name/the_controller/id    PUT     --> 'update' action
 * http://the_host/the_app_name/the_controller/id    DELETE  --> 'destroy' action
 *
 * Controllers that also render GSP/Zul pages are responsible for implementing separate 
 * actions that do not conflict with the RESTful ones. (In general, this is expected 
 * to be limited to a 'view' action, as well as an index action that redirects to the view action.)
 * It is possible, however, to use the mixed-in actions non-RESTfully (by identifying the action
 * within the URL).
 *
 * NOTE: URI's containing 'api' will be directed to the appropriate controller 
 * action based upon HTTP method (see UriMappings.groovy).
 * These URIs will also be authenticated using BasicAuthentication.
 *
 * URIs that do not have 'api' in their name will be directed to the appropriate action
 * using non-RESTful conventions (i.e., the action name must be part of the URI).
 * 
 * Any of the default RESTful API actions may be overridden by implementing the action
 * within the specific controller.  They will not be mixed-in if they are already present.
 *
 * Controllers for which this class is mixed-in may implement methods that override the default
 * behavior for request params extraction and rendering as follows:
 *
 * "public Closure getCustomRenderer( String actionName )"
 * If implemented, this method should return a Closure that provides custom rendering appropriate for the
 * request.  If there is no Closure that provides appropriate rendering for the current request, the method
 * should return null.
 *
 * Pleae note, this method must 'NOT' actually perform any rendering, but simply return a Closure that
 * 'can' perform appropriate rendering for the current request (i.e., a specific action and HTTP format).
 * Controllers may need to provide closures that provide rendering for a custom MIME type (that indictaes
 * that a particular XML Schema constrained body be rendered.
 *
 * The reason this method returns a Closure is so the controller is not forced to implement custom support
 * across the board, but instead can indicate (i.e., by returning a closure) specific individual rendering
 * it supports (e.g., perhaps only a custom MIME type is supported, and everything else is handled by
 * the default rendering provided by the injected action).
 *
 * Custom renderers are needed primarily for two situations:
 *    a) The default Grails Converters (for XML or JSON) cannot properly handle a particular complex object.
 *    b) A custom MIME type and versioned XML Schema is employed to provide a stable representation, allowing for
 *       simultaneous support of older and newer versions.
 * It is important to note, the default JSON and XML rendering is simply based upon the Grails Converters,
 * and the JSON and XML structure is subject to change as the domain model classes are modified over time.
 *
 * Note that the returned closure MUST accept a single argument, which is the map that would have been used for
 * the default rendering.  Please see the actions contained in this class for examples.
 *
 * public Closure getParamsExtractor()
 * If implemented, this method should return a closure that can extract request data into a map (that will be added
 * to the Grails params map) for the specific request.
 * This method, like the method discussed above, should return null if it cannot handle the current request.
 * This method is not needed if the default params extraction suffices, and will usually be necessary when using
 * a 'custom' representation (i.e., a new MIME type) or when the default converters simply cannot handle a complex model.
 */
class RestfulControllerMixin {

    static allowedMethods = [ show: "GET", list: "GET", create: "POST", update: "PUT", destroy: "DELETE" ]  // ensure RESTful

    String domainSimpleName
    String serviceName

    def log = Logger.getLogger( "REST API" ) // This may be overridden by controllers, so the logger is specific to that controller.
    

    // wrap the 'message' invocation within a closure, so it can be passed into an ApplicationException to localize error messages
    def localizer = { mapToLocalize -> 
        this.message( mapToLocalize ) 
    }


    // if a controller doesn't explicitly set this, we'll determine the domain name based upon normal naming conventions
    String getDomainSimpleName() {
        domainSimpleName = domainSimpleName ?: GrailsNameUtils.getLogicalName( this.class, "Controller" )
        domainSimpleName
    }


    // if a controller doesn't explicitly set this, we'll determine the service name based upon normal naming conventions
    String getServiceName() {
        serviceName ?: GrailsNameUtils.getPropertyNameRepresentation( "${getDomainSimpleName()}Service" )
    }

    
// ------------------------------------------- Controller Actions -------------------------------------


    def create = {
        
        log.trace "${this.class.simpleName}.save invoked with params $params and format $request.format"
        def extractedParams = extractParams()
        def entity
        try {
            entity = this."$serviceName".create( extractedParams )
            def successReturnMap = [ success: true, 
                                     data: entity, 
                                     message:  localizer( code: 'default.created.message',
                                                          args: [ localizer( code: "${domainSimpleName}.label", default: "${domainSimpleName}" ), 
                                                                  entity.id ] ) ]
            this.response.status = 201 // the 'created' code
            getRenderer( "create" ).call( successReturnMap )
        }
        catch (ApplicationException e) {
            this.response.setStatus( e.httpStatusCode ) 
            getRenderer( "create" ).call( (e.returnMap( localizer ) + [ data: entity ]) )
        } 
        catch (e) { // CI logging
            this.response.setStatus( 500 ) 
            log.error "Caught unexpected exception ${e.class.simpleName} which may be a candidate for wrapping in a ApplicationException, message: ${e.message}", e
            getRenderer( "create" ).call( defaultErrorRenderMap( e, entity, 'default.not.created.message' ) )
        }               
    }             
    
            
    def update = { 
        
        log.trace "${this.class.simpleName}.update invoked with params $params and format $request.format"
        def extractedParams = extractParams()
        def entity
        try {
            entity = this."$serviceName".update( extractedParams )
            def successReturnMap = [ success: true, 
                                     data: entity, 
                                     message:  localizer( code: 'default.updated.message',
                                                          args: [ localizer( code: "${domainSimpleName}.label", default: "${domainSimpleName}" ), 
                                                                  entity.id ] ) ]
            this.response.status = 200 // the 'created' code
            getRenderer( "update" ).call( successReturnMap )
        }
        catch (ApplicationException e) {
            this.response.setStatus( e.httpStatusCode ) 
            getRenderer( "update" ).call( (e.returnMap( localizer ) + [ data: entity ]) )
        } 
        catch (e) { // CI logging
            this.response.setStatus( 500 ) 
            log.error "Caught unexpected exception ${e.class.simpleName} which may be a candidate for wrapping in a ApplicationException, message: ${e.message}", e
            getRenderer( "update" ).call( defaultErrorRenderMap( e, entity, 'default.not.updated.message' ) )
        }
    } 
        

    def destroy = { 

        log.trace "${this.class.simpleName}.destroy invoked with params $params and format $request.format"
        if (params?.size() < 1) {
            extractParams()
            log.warn "destroy() required explicit extraction of params -- this is normally not needed as the id is provided within the URI"
        }

        // Note that a HTTP DELETE will not have a body, and we should not attempt to extract JSON out of the request.
        // Instead, we should expect the 'id' to be mapped for us, as it is provided as part of the URI.
        try {
            this."$serviceName".delete( params )
            def successReturnMap = [ success: true, 
                                     data: null, 
                                     message:  localizer( code: 'default.deleted.message',
                                                          args: [ localizer( code: "${domainSimpleName}.label", default: "${domainSimpleName}" ), 
                                                                  params.id ] ) ]
            this.response.status = 200 // the 'created' code
            getRenderer( "destroy" ).call( successReturnMap )
        }
        catch (ApplicationException e) {
            this.response.setStatus( e.httpStatusCode ) 
            getRenderer( "destroy" ).call( (e.returnMap( localizer ) + [ data: params ]) )
        } 
        catch (e) { // CI logging
            this.response.setStatus( 500 ) 
            log.error "Caught unexpected exception ${e.class.simpleName} which may be a candidate for wrapping in a ApplicationException, message: ${e.message}", e
            getRenderer( "destroy" ).call( defaultErrorRenderMap( e, entity, 'default.not.deleted.message' ) )
        }
    } 
        
        
    def show = { 

        log.trace "${this.class.simpleName}.show invoked with params $params and format $request.format"
        def entity

        if (params?.size() < 1) {
            extractParams()
            log.warn "show() required explicit extraction of params -- this is normally not needed as the id is provided within the URI"
        }
        try {
            entity = this."$serviceName".read( params.id )
            def successReturnMap = [ success: true, 
                                     data: entity, 
                                     message:  localizer( code: 'default.show.message',
                                                          args: [ localizer( code: "${domainSimpleName}.label", default: "${domainSimpleName}" ) ] ) ]
            this.response.status = 200 // the 'created' code
            getRenderer( "show" ).call( successReturnMap )
        }
        catch (ApplicationException e) {
            this.response.setStatus( e.httpStatusCode ) 
            getRenderer( "show" ).call( (e.returnMap( localizer ) + [ data: params ]) )
        } 
        catch (e) { // CI logging
            this.response.setStatus( 500 ) 
            log.error "Caught unexpected exception ${e.class.simpleName} which may be a candidate for wrapping in a ApplicationException, message: ${e.message}", e
            getRenderer( "show" ).call( defaultErrorRenderMap( e, entity, 'default.not.shown.message' ) )
        }
    } 
        
        
    def list = { 

        log.trace "${this.class.simpleName}.list invoked with params $params and format $request.format"
        if (params?.size() < 1) {
            extractParams()
            log.warn "list() required explicit extraction of params -- this is normally not needed as the id is provided within the URI"
        }

        def entities
        def totalCount
        try {
            entities = this."$serviceName".list( params )
            totalCount = this."$serviceName".count( params )
            def successReturnMap = [ success: true, 
                                     data: entities,
                                     totalCount: totalCount, 
                                     message: localizer( code: 'default.list.message',
                                                         args: [ localizer( code: "${domainSimpleName}.label", default: "${domainSimpleName}" ) ] ) ]
            this.response.status = 200 // the 'created' code
            getRenderer( "list" ).call( successReturnMap )
        }
        catch (ApplicationException e) {
            this.response.setStatus( e.httpStatusCode ) 
            getRenderer( "list" ).call( (e.returnMap( localizer ) + [ data: params ]) )
        } 
        catch (e) { // CI logging
            this.response.setStatus( 500 ) 
            log.error "Caught unexpected exception ${e.class.simpleName} which may be a candidate for wrapping in a ApplicationException, message: ${e.message}", e
            getRenderer( "list" ).call( defaultErrorRenderMap( e, entities, 'default.not.listed.message' ) )
        }
    }


// ----------------------------------- Helper Methods -----------------------------------    


    private Map defaultErrorRenderMap( e, data, messageCode ) {
        [ data: data,
          success: false,
          message: localizer( code: messageCode,
                              args: [ localizer( code: "${domainSimpleName}.label", default: "${domainSimpleName}" ) ] ),
          errors: (e.hasProperty( 'errors' ) ? e.errors?.allErrors?.collect { localizer( error: it ) } : null),
          underlyingErrorMessage: e.message ]
    }
    

    private Map extractParams() {
        Closure extractor
        if (this.class.metaClass.respondsTo( this.class, "getParamsExtractor" )) {
            extractor = this.getParamsExtractor()
        }
        if (!extractor) {
        // TODO: Insert else-if to call an injected CustomRepresentationSupportService
        //       to ask for an extractor. This would preclude the need for controllers to
        //       implement a getParamsExtractor() if the developer instead registers the
        //       custom extractors with this common registry service.  This service would
        //       facilitate supporting new representations without having to modify the controller.
            extractor = defaultParamsExtractor
        }
        params << extractor.call()
        params
    }


    // A default params extractor that uses built-in Grails support for parsing JSON and XML.
    private Closure defaultParamsExtractor = { ->
        
        Map paramsContent = [:]
        if (request.format ==~ /.*html.*/) {
            log.debug "${this.class.simpleName} HTML request format will use pre-populated params $params"
            paramsContent
        }
        else if (request.format ==~ /.*json.*/) {
            request.JSON.entrySet().each {
                paramsContent.put it.key, it.value
            }
            log.debug "${this.class.simpleName} has extracted JSON content from the request and populated params $paramsContent"
            paramsContent
        }
        else if (request.format ==~ /.*xml.*/) {
            request.XML.children().each {
                paramsContent.put it.name(), it.text()
            }
            log.debug "${this.class.simpleName} has extracted XML content from the request and populated params $paramsContent"
            paramsContent
        }
        else {
            throw new RuntimeException( "@@r1:com.sungardhe.framework.unsupported_content_type:${request.format}" )
        }
    }


    private Closure getRenderer( String rendererName ) {
        Closure renderer
        if (this.class.metaClass.respondsTo( this.class, "getCustomRenderer" )) {
            renderer = this.getCustomRenderer( rendererName )
        }
        // TODO: Insert else-if to call an injected CustomRepresentationSupportService
        //       that can be asked for an appropriate renderer. This would preclude the need for controllers
        //       to implement a getCustomRenderer() if the developer instead registers the
        //       custom renderer with this common registry service. This service would
        //       facilitate supporting new representations without having to modify the controller.

        renderer ?: defaultRenderer
    }
                 
    
    private Closure defaultRenderer = { responseMap ->
        if (request.format ==~ /.*html.*/) {
            response.setHeader( "Content-Type", "application/html" ) 
            render( responseMap )
        } 
        else if (request.format ==~ /.*json.*/) {
            response.setHeader( "Content-Type", "application/json" ) 
            def json = responseMap as JSON
            render( json )
        } 
        else if (request.format ==~ /.*xml.*/) {
            response.setHeader( "Content-Type", "application/xml" ) 
            render( responseMap as XML )
        } 
        else {
            throw new RuntimeException( "@@r1:com.sungardhe.framework.unsupported_content_type:${request.format}" )
        }        
    }

}

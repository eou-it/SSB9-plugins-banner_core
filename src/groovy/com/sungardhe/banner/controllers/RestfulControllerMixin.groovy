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
 * A mixin for controllers that provides them a full RESTful API supporting XML and JSON representations.
 * Specifically, 'create', 'update', 'destroy' 'list', and 'show' actions are provided by this mixin.
 *
 * When a controller 'mixes in' this class AND includes a 'boolean hasRestMixin = true' property,
 * the BannerCoreGrailsPlugin will register these mixed-in actions such that the Grails URI mapping
 * recognizes these new actions.
 *
 * The standard REST API is:
 * http://the_host/the_app_name/the_controller       GET     --> 'list' action
 * http://the_host/the_app_name/the_controller/id    GET     --> 'show' action
 * http://the_host/the_app_name/the_controller       POST    --> 'create' action
 * http://the_host/the_app_name/the_controller/id    PUT     --> 'update' action
 * http://the_host/the_app_name/the_controller/id    DELETE  --> 'destroy' action
 *
 * If this mixin is modified to add/remove/rename the actions, the BannerCoreGrailsPlugin
 * class will require a corresponding update (as it specifically registers these mixed in actions
 * so the URI mapping is successful). Since doing so is not likely common, this coupling is
 * likely not a concern.
 *
 * Controllers that also render GSP/Zul pages are responsible for implementing separate 
 * actions that do not conflict with the RESTful ones. (In general, this is expected 
 * to be limited to a 'view' action, as well as an index action that redirects to the view action.)
 *
 * NOTE: URI's containing 'api' will be directed to the appropriate controller 
 * action based upon HTTP method (see UriMappings.groovy).  These URIs will also 
 * be authenticated using BasicAuthentication. URIs that do not have 'api' in their name 
 * will be directed to the appropriate action using non-RESTful conventions
 * (i.e., the action name must be part of the URI). 
 * 
 * Any of the default RESTful API actions may be overridden by implementing the action
 * within the specific controller.
 *
 * When using the default RESTful API actions, additional information must be specified in the 
 * controller when conventions cannot sufficiently define the desired behavior.
 *
 * Specifically, controllers extending this class and using it's RESTful API methods may:
 *
 * 1) Provide a method that returns a Closure that implements custom rendering or null if no custom renderer is available.
 *    The signature of this method must be:  public Closure getCustomRenderer( String actionName )
 *
 *    The method must 'NOT' actually perform any rendering, but instead must simply return a Closure that
 *    'can' perform the rendering for the current request (i.e., a specific action and HTTP format).  The need
 *    to support a custom MIME type specifying a particular XML Schema version, etc. is a primary need for having
 *    such a method.
 *
 *    The reason the method returns a closure is so that the controller is not forced to implement custom support
 *    across the board, but can indicate (i.e., by returning a closure) specific individual rendering
 *    it supports (e.g., perhaps only a custom MIME type is supported, and everything else is handled by  
 *    the default rendering provided by the injected action).
 *
 *    Custom renderers are needed primarily for two situations: 
 *       a) Grails cannot parse or write the needed format (e.g., the default Grails Converters for XML or JSON 
 *          cannot properly handle a particular complex object correctly).  In this case, the renderAction methods
 *          would really have to provide full support (as none of the default rendering would be functional).  
 *       b) A Custom type is needed. For example, a custom MIME type may have been requested, as would be the case 
 *          when asking for a particular XML version (based upon a versioned XML Schema).  In this situation, 
 *          a renderAction method may check the 'request' and only return a Closure if the request.format was 
 *          a supported custom MIME type. Otherwise, it would return null and the injected action would use it's 
 *          own Grails Converter based rendering for XML.
 *
 *    The returned closure must accept the map that would have been used for default rendering.
 *
 *    It is important to note, the default JSON and XML rendering is simply based upon the Grails Converters,
 *    and the JSON and XML structure is subject to change as the domain model classes are modified over time.
 * *
 * 2) Expose a method that returns a closure that can extract request data into params for the specific request.
 *    This method, like the method discussed above, should return null if it cannot handle the current request.
 *    This method is not needed if the default params extraction suffices.  
 *
 *    The signature of this method is: public Closure getParamsExtractor()
 *
 *    A controller will normally need to return a closure used to extract params from a custom XML format.
 **/ 
class RestfulControllerMixin {

    // DEVELOPER: Grails URI mapping must be made aware of the actions being mixed-in via this class.
    // That is, we explicitly need to register these actions during bootstrap.
    // Please update BannerCoreGrailsPlugin when adding, re-naming or removing mixed-in actions. 

    static allowedMethods = [ show: "GET", list: "GET", create: "POST", update: "PUT", destroy: "DELETE" ]  // --> ensure RESTful

    String domainSimpleName
    String serviceName

    def log = Logger.getLogger( "REST API" ) // This may be overridden by controllers, so the logger is specific to that controller.
    

    // wrap the 'message' invocation within a closure, so it can be passed into an ApplicationException to localize error messages
    def localizer = { mapToLocalize -> 
        this.message( mapToLocalize ) 
    }


    // if we didn't explicitly set this, we'll determine the domain name based upon normal naming conventions
    String getDomainSimpleName() {
        domainSimpleName = domainSimpleName ?: GrailsNameUtils.getLogicalName( this.class, "Controller" )
        domainSimpleName
    }

    
    // if we didn't explicitly set this, we'll determine the service name based upon normal naming conventions
    String getServiceName() {
        serviceName ?: GrailsNameUtils.getPropertyNameRepresentation( "${domainSimpleName}Service" )
    }

    
// ------------------------------------------- Controller Actions -------------------------------------


    def create = {
        log.trace "${this.class.simpleName}.save invoked with params $params and request $request"
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
        
        log.trace "${this.class.simpleName}.update invoked with params $params and request $request"
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

        log.trace "${this.class.simpleName}.destroy invoked with params $params and request $request"

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

        log.trace "${this.class.simpleName}.show invoked with params $params and request $request"
        def entity
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

        log.trace "${this.class.simpleName}.list invoked with params $params and request $request"
        def entities
        def totalCount
        try {
            // TODO if-else ... allow the delegate may substitute it's own implementation versus the default service.list (e.g., to use a query instead)
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
        } else {
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
        //       to ask for a renderer. This would preclude the need for controllers to
        //       implement a getCustomRenderer() if the developer instead registers the
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

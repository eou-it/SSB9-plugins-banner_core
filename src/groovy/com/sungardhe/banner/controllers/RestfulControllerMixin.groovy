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
import com.sungardhe.banner.representations.ResourceRepresentationRegistry

import grails.converters.JSON
import grails.converters.XML
import grails.util.GrailsNameUtils

import org.apache.log4j.Logger

import org.codehaus.groovy.grails.commons.ApplicationHolder as AH


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
 *      **** Optional Method: "public Closure getCustomRepresentationBuilder( String actionName )"****
 *
 * If implemented, this method should return a Closure that can create an appropriate representation for the
 * current request.  If there is no Closure that can create an appropriate representation for the current request,
 * the method should return null.
 *
 * Please note, this method must 'NOT' actually perform any rendering, but simply return a Closure that
 * 'can' return an appropriate representation when called, which is ready for rendering.
 *
 * Controllers will usually need to provide closures that are able to return representations that correspond to a
 * a custom MIME type identified in the request. That is, clients may indicate they need a specific representation
 * by using a custom MIME type.
 *
 * The reason this method returns a Closure is so the controller is not forced to implement custom support
 * across the board, but instead can indicate (i.e., by returning a closure) specific individual representations
 * it supports (e.g., perhaps only one custom MIME type is supported, and everything else is handled by
 * the default rendering provided by the injected action).
 *
 * Custom representations are needed primarily for two situations:
 *    a) The default Grails Converters (for XML or JSON) cannot properly handle a particular complex object.
 *    b) A custom MIME type and versioned XML Schema is employed to provide a stable representation, allowing for
 *       simultaneous support of older and newer versions.
 * It is important to note, the default JSON and XML rendering is simply based upon the Grails Converters,
 * and the JSON and XML structure is subject to change as the domain model classes are modified over time.
 *
 * Note that the returned closure MUST accept a single argument, which is the map that would have been used for
 * the default rendering.  Please see the actions contained in this class for examples.
 *
 * IF the controller does not return a closure for a needed representation, the ResourceRepresentationRegistry will
 * be asked for one.  Registering builders within this registry may preclude the need for a controller to implement
 * the 'getCustomRepresentationBuilder' method.  Using the registry is recommended particularly for on-going support,
 * by allowing additional representations to be added without requiring code changes to the controller.
 *
 *                 **** Optional Method: "public Closure getParamsExtractor()" ****
 *
 * If implemented, this method should return a closure that can extract request data into a map (that will subsequently be added
 * to the Grails params map) for the specific request.
 * This method, like the method discussed above, should return null if it cannot handle the current request.
 * This method is not needed if the default params extraction suffices, and will usually be necessary when using
 * a 'custom' representation (i.e., a new MIME type) or when the default converters simply cannot handle a complex model.
 *
 * Closures to perform custom params extraction may be registered within the ResourceRepresentationRegistry along with the
 * closures that perform custom rendering.
 *
 * Using the registry is recommended over implementing these methods in your controller.
 */
class RestfulControllerMixin {

    static allowedMethods = [ show: "GET", list: "GET", create: "POST", update: "PUT", destroy: "DELETE" ]  // ensure RESTful

    String domainSimpleName
    String serviceName

    def ctx = AH.application.mainContext
    def resourceRepresentationRegistry // note: the 'get' method will retrieve this when needed, as we cannot inject directly into a mixin

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


    // refBase is a URL embedded in responses that can be used to access the resource
    String refBase( request ) {
        "${request.scheme}://${request.serverName}:${request.serverPort}/${grailsApplication.metadata.'app.name'}/${GrailsNameUtils.getPropertyNameRepresentation( domainSimpleName )}"
    }


    ResourceRepresentationRegistry getResourceRepresentationRegistry() {
        if (!resourceRepresentationRegistry) {
            resourceRepresentationRegistry = ctx.resourceRepresentationRegistry
        }
        resourceRepresentationRegistry
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
                                     refBase: refBase( request ),
                                     message:  localizer( code: 'default.created.message',
                                                          args: [ localizer( code: "${domainSimpleName}.label", default: "${domainSimpleName}" ), 
                                                                  entity.id ] ) ]
            this.response.status = 201 // the 'created' code
            def result = representationiBuilderFor( "create" ).call( successReturnMap )
            log.debug  "${this.class.simpleName}.create will render $result"
            render result
        }
        catch (ApplicationException e) {
            this.response.setStatus( e.httpStatusCode ) 
            def result = defaultRepresentationBuilder( e.returnMap( localizer ) + [ data: entity ] )
            log.debug "${this.class.simpleName}.create caught ApplicationException and will render $result"
            render result
        }
        catch (e) { // CI logging
            this.response.setStatus( 500 ) 
            log.error "Caught unexpected exception ${e.class.simpleName} which may be a candidate for wrapping in a ApplicationException, message: ${e.message}", e
            render( defaultErrorRenderMap( e, entity, 'default.not.created.message' ) )
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
                                     refBase: refBase( request ),
                                     message:  localizer( code: 'default.updated.message',
                                                          args: [ localizer( code: "${domainSimpleName}.label", default: "${domainSimpleName}" ),
                                                                  entity.id ] ) ]
            this.response.status = 200
            def result = representationiBuilderFor( "update" ).call( successReturnMap )
            log.debug "${this.class.simpleName}.update will render $result"
            render result
        }
        catch (ApplicationException e) {
            this.response.setStatus( e.httpStatusCode ) 
            def result = defaultRepresentationBuilder( e.returnMap( localizer ) + [ data: entity ] )
            log.debug "${this.class.simpleName}.update caught ApplicationException and will render $result"
            render result
        }
        catch (e) { // CI logging
            this.response.setStatus( 500 ) 
            log.error "Caught unexpected exception ${e.class.simpleName} which may be a candidate for wrapping in a ApplicationException, message: ${e.message}", e
            render( defaultErrorRenderMap( e, entity, 'default.not.updated.message' ) )
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
            this.response.status = 200 
            def result = representationiBuilderFor( "destroy" ).call( successReturnMap )
            log.debug "${this.class.simpleName}.destroy will render $result"
            render result
        }
        catch (ApplicationException e) {
            this.response.setStatus( e.httpStatusCode ) 
            def result = defaultRepresentationBuilder( e.returnMap( localizer ) )
            log.debug "${this.class.simpleName}.destroy caught ApplicationException and will render $result"
            render result
        } 
        catch (e) { // CI logging
            this.response.setStatus( 500 ) 
            log.error "Caught unexpected exception ${e.class.simpleName} which may be a candidate for wrapping in a ApplicationException, message: ${e.message}", e
            render( defaultErrorRenderMap( e, entity, 'default.not.deleted.message' ) )
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
                                     refBase: refBase( request ),
                                     message:  localizer( code: 'default.show.message',
                                                          args: [ localizer( code: "${domainSimpleName}.label", default: "${domainSimpleName}" ) ] ) ]
            this.response.status = 200
            def result = representationiBuilderFor( "show" ).call( successReturnMap )
            log.debug "${this.class.simpleName}.show will render $result"
            render result
        }
        catch (ApplicationException e) {
            this.response.setStatus( e.httpStatusCode ) 
            def result = defaultRepresentationBuilder( e.returnMap( localizer ) + [ data: params ] )
            log.debug "${this.class.simpleName}.show caught ApplicationException and will render $result"
            render result
        } 
        catch (e) { // CI logging
            this.response.setStatus( 500 ) 
            log.error "Caught unexpected exception ${e.class.simpleName} which may be a candidate for wrapping in a ApplicationException, message: ${e.message}", e
            render( defaultErrorRenderMap( e, entity, 'default.not.shown.message' ) )
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
                                     refBase: refBase( request ),
                                     message: localizer( code: 'default.list.message',
                                                         args: [ localizer( code: "${domainSimpleName}.label", default: "${domainSimpleName}" ) ] ) ]
            this.response.status = 200
            def result = representationiBuilderFor( "list" ).call( successReturnMap )
            log.debug "${this.class.simpleName}.list will render $result"
            render result
        }
        catch (ApplicationException e) {
            this.response.setStatus( e.httpStatusCode )
            def result = defaultRepresentationBuilder( e.returnMap( localizer ) + [ data: params ] )
            log.debug "${this.class.simpleName}.list caught ApplicationException and will render $result"
            render result
        } 
        catch (e) { // CI logging
            this.response.setStatus( 500 )
            log.error "Caught unexpected exception ${e.class.simpleName} which may be a candidate for wrapping in a ApplicationException, message: ${e.message}", e
            render( defaultErrorRenderMap( e, entities, 'default.not.listed.message' ) )
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

    
    /**
     * Returns a populated params map.
     * If the controller has a 'getParamsExtractor()' method, that extractor will be used. If not,
     * the ResourceRepresentationRegistery will be checked. If still no extractor, the default extractor will be used.
     * @return Map a populated params map
     */
    private Map extractParams() {
        Closure extractor
        if (this.class.metaClass.respondsTo( this.class, "getParamsExtractor" )) {
            log.debug "extractParams() will first try to use ${this.class}.getParamsExtractor()"
            extractor = this.getParamsExtractor()
        }
        if (!extractor) {
            log.debug "extractParams() will now try to find an extractor within the registry"
            def handler = getResourceRepresentationRegistry().get( request?.getHeader( 'Content-Type' ), getDomainSimpleName() )
            extractor = (Closure) handler?.paramsParser
            log.debug "extractParams() found an extractor within the registry"
        }
        if (!extractor) {
            log.debug "extractParams() will use a default extractor"
            extractor = defaultParamsExtractor
        }
        params << extractor?.call( request )
        params
    }


    // A default params extractor that uses built-in Grails support for parsing JSON and XML.
    private Closure defaultParamsExtractor = { request ->
        
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


    private Closure representationiBuilderFor( String actionName ) {
        Closure representationBuilder
        if (this.class.metaClass.respondsTo( this.class, "getCustomRepresentationBuilder" )) {
            representationBuilder = this.getCustomRepresentationBuilder( actionName )
        }
        if (!representationBuilder) {
            log.debug "formatForRendering will now try to find an representationBuilder within the registry"
            def handler = getResourceRepresentationRegistry().get( request?.getHeader( 'Content-Type' ), getDomainSimpleName() )
            switch( actionName ) {
                case 'list'   : representationBuilder = (Closure) handler?.listRenderer; break
                case 'show'   : representationBuilder = (Closure) handler?.singleRenderer; break
                case 'create' : representationBuilder = (Closure) handler?.singleRenderer; break
                case 'update' : representationBuilder = (Closure) handler?.singleRenderer; break
                // note: currently 'delete' is not supported with custom rendering..
            }
        }
        if (representationBuilder) {
            // we found support for the current request, so we'll set the response content type before returning the representationBuilder
            response.setHeader( "Content-Type", request?.getHeader( 'Content-Type' ) )
        } else {
            log.debug "formatForRendering() will use a default representationBuilder"
            // note the defaultrepresentationBuilder will set the appropriate content type
            representationBuilder = defaultRepresentationBuilder
        }
        log.debug "going to return representationBuilder -- ${representationBuilder != null}"
        representationBuilder
    }
                 
    
    private Closure defaultRepresentationBuilder = { responseMap ->
        if (request.format ==~ /.*html.*/) {
            response.setHeader( "Content-Type", "application/html" ) 
            return responseMap
        } 
        else if (request.format ==~ /.*json.*/) {
            response.setHeader( "Content-Type", "application/json" ) 
            return responseMap as JSON
        } 
        else if (request.format ==~ /.*xml.*/) {
            response.setHeader( "Content-Type", "application/xml" ) 
            return responseMap as XML
        } 
        else {
            throw new RuntimeException( "@@r1:com.sungardhe.framework.unsupported_content_type:${request.format}" )
        }        
    }

}

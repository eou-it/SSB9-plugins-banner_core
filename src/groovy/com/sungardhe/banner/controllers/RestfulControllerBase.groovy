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
 * A base class for controllers that provides them a full RESTful API 
 * supporting XML and JSON representations.  
 *
 * Controllers that also render GSP/Zul pages are responsible for implementing separate 
 * actions that do not conflict with the RESTful ones. (In general, this is expected 
 * to be limited to a 'view' action, as well as an index action that redirects to the view action.)
 *
 * NOTE: URI's containing 'api' will be directed to the appropriate controller 
 * action based upon HTTP method (see UriMappings.groovy).  These URIs will also 
 * be authenticated using BasicAuthentication. URIs that do not have 'api' in their name 
 * will be directed to the appropriate action using non-REST conventions 
 * (i.e., the action name must be part of the URI). 
 * 
 * Any of the default RESTful API actions may be overriden by implementing the action 
 * within the concrete controller.
 *
 * When using the default RESTful API actions, additional information must be specified in the 
 * controller when conventions cannot sufficiently define the desired behavior.
 *
 * Specifically, controllers extending this class and using it's RESTful API methods may:
 *
 * 1) Expose custom methods that perform custom rendering. These methods must accept a map that would 
 *    have been used for default rendering. They must 'NOT' actually perform the rendering, but instead 
 *    must simply return a Closure that 'can' perform the rendering if one is available that can  
 *    handle the current request (i.e., HTTP type - a custom MIME type specifying a particular XML Schema version, etc.).
 *    The reason these methods return closures is so that they do not have to implement custom support 
 *    across the board, but can indicate (i.e., by returning a closure) specific individual responses 
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
 * It is important to note, the default JSON and XML rendering is simply based upon the Grails Converters, 
 * and the JSON and XML structure is subject to change as the domain model classes are modified over time. 
 * 
 *         Closure 'renderSave( returnMap )   // the returnMap that 'would' have been rendered as default
 *         Closure 'renderUpdate( returnMap ) // also note returnMap could reflect either a success or an error case. 
 *         Closure 'renderDelete( returnMap )
 *         Closure 'renderShow( returnMap )
 *         Closure 'renderList( returnMap )
 *
 * 2) Expose an 'extractParams' closure IF the default extraction of params does not suffice. If 
 * a controller provides an implementation, it should return an appropriate params map as extracted 
 * from the Grails Controller 'params' object (e.g., for a given request format, parsing may be required).
 * The 'default' params extraction implemented here simply parses the 'params' object with the normal XML 
 * and JSON Grails converters when supporting those requested formats. 
 **/
abstract 
class RestfulControllerBase { 
            

    def log = Logger.getLogger( this.class.name )  
    
    // Set this explicitly via the constructor if not using normal naming conventions
    String domainSimpleName 
    
    String serviceName = GrailsNameUtils.getPropertyNameRepresentation( "${domainSimpleName}Service" )


    // wrap the 'message' invocation within a closure, so it can be passed into an ApplicationException to localize error messages
    def localizer = { mapToLocalize -> 
        this.message( mapToLocalize ) 
    }

    
    // Constructor allows overriding the 'domainSimpleName' for this controller
    public RestfulControllerBase( String domainSimpleName = null ) { 
        this.domainSimpleName = domainSimpleName ?: this.class.simpleName.substring( 0, this.class.simpleName.indexOf( "Controller" ) )
    }
    
    
    // ------------------------------------------- Controller Actions -------------------------------------
    // Note that Grails requires these as 'closures' versus methods (and hence normal method injection cannot be employed here.
    //      Injecting a 'property' versus a method also is not functional, hence the 'old fashioned' way of inheritance...)
        
    def save = { 
        log.trace "${this.class.simpleName}.save invoked with ${this.params}"
        def entity
        try {
            entity = this."$serviceName".create( params )
            def successReturnMap = [ success: true, 
                                     data: entity, 
                                     message:  localizer( code: 'default.created.message',
                                                          args: [ localizer( code: "${domainSimpleName}.label", default: "${domainSimpleName}" ), 
                                                                  entity.id ] ) ]
            def customRenderer
            if (this.class.metaClass.respondsTo( this.class, "renderSave" )) {
                customRenderer = this.renderSave( successReturnMap )
            } 
            if (customRenderer) {
                customRenderer( successReturnMap ) 
            } else {
                // the controller didn't specify a closure that should be used to render the result 
                this.response.status = 201 // the 'created' code
                renderResult( successReturnMap )
            }
        } 
        catch (ApplicationException e) {
            this.response.setStatus( e.httpStatusCode ) 
            renderResult( (e.returnMap( localizer ) + [ data: entity ]) )
        } 
        catch (e) { // CI logging
            this.response.setStatus( 500 ) 
            log.error "Caught unexpected exception ${e.class.simpleName} which may be a candidate for wrapping in a ApplicationException, message: ${e.message}", e
            renderResult( [ data: entity,
                            success: false, 
                            message: localizer( code: 'default.not.created.message', 
                                                         args: [ localizer( code: "${domainSimpleName}.label", default: "${domainSimpleName}" ) ] ),
                            errors: (e.hasProperty( 'errors' ) ? e.errors?.allErrors?.collect { localizer( error: it ) } : null),
                            underlyingErrorMessage: e.message ]  )
        }               
    }             
    
            
    def update = { 
        
        log.trace "${this.class.simpleName}.update invoked with ${this.params}"
        def entity
        try {
            entity = this."$serviceName".update( params )
            def successReturnMap = [ success: true, 
                                     data: entity, 
                                     message:  localizer( code: 'default.updated.message',
                                                          args: [ localizer( code: "${domainSimpleName}.label", default: "${domainSimpleName}" ), 
                                                                  entity.id ] ) ]
            def customRenderer
            if (this.class.metaClass.respondsTo( this.class, "renderUpdate" )) {
                customRenderer = this.renderUpdate( successReturnMap )
            } 
            if (customRenderer) {
                customRenderer( successReturnMap ) 
            } else {
                // the controller didn't specify a closure that should be used to render the result 
                renderResult( successReturnMap )
            }
        } 
        catch (ApplicationException e) {
            this.response.setStatus( e.httpStatusCode ) 
            renderResult( (e.returnMap( localizer ) + [ data: entity ]) )
        } 
        catch (e) { // CI logging
            this.response.setStatus( 500 ) 
            log.error "Caught unexpected exception ${e.class.simpleName} which may be a candidate for wrapping in a ApplicationException, message: ${e.message}", e
            renderResult( [ data: entity,
                            success: false, 
                            message: localizer( code: 'default.not.updated.message', 
                                                       args: [ localizer( code: "${domainSimpleName}.label", default: "${domainSimpleName}" ) ] ),
                            errors: (e.hasProperty( 'errors' ) ? e.errors?.allErrors?.collect { localizer( error: it ) } : null),
                            underlyingErrorMessage: e.message ]  )
        }                    
    } 
        

    def delete = { 

        log.trace "${this.class.simpleName}.delete invoked with ${this.params}"
        try {
            this."$serviceName".delete( params )
            def successReturnMap = [ success: true, 
                                     data: null, 
                                     message:  localizer( code: 'default.deleted.message',
                                                          args: [ localizer( code: "${domainSimpleName}.label", default: "${domainSimpleName}" ), 
                                                                  params.id ] ) ]
            def customRenderer
            if (this.class.metaClass.respondsTo( this.class, "renderDelete" )) {
                customRenderer = this.renderDelete( successReturnMap )
            } 
            if (customRenderer) {
                customRenderer( successReturnMap ) 
            } else {
                // the delegate didn't specify a closure that should be used to render the result 
                renderResult( successReturnMap )
            }
        } 
        catch (ApplicationException e) {
            this.response.setStatus( e.httpStatusCode ) 
            renderResult( (e.returnMap( localizer ) + [ data: params ]) )
        } 
        catch (e) { // CI logging
            this.response.setStatus( 500 ) 
            log.error "Caught unexpected exception ${e.class.simpleName} which may be a candidate for wrapping in a ApplicationException, message: ${e.message}", e
            renderResult( [ data: null,
                            success: false, 
                            message: localizer( code: 'default.not.deleted.message', 
                                                       args: [ localizer( code: "${domainSimpleName}.label", default: "${domainSimpleName}" ) ] ),
                            errors: (e.hasProperty( 'errors' ) ? e.errors?.allErrors?.collect { localizer( error: it ) } : null),
                            underlyingErrorMessage: e.message ] )
        }               
    } 
        
        
    def show = { 

        log.trace "${this.class.simpleName}.show invoked with ${this.params}"
        def entity
        try {
            entity = this."$serviceName".read( params.id )
            def successReturnMap = [ success: true, 
                                     data: entity, 
                                     message:  localizer( code: 'default.show.message',
                                                          args: [ localizer( code: "${domainSimpleName}.label", default: "${domainSimpleName}" ) ] ) ]
            def customRenderer
            if (this.class.metaClass.respondsTo( this.class, "renderShow" )) {
                customRenderer = this.renderShow( successReturnMap )
            } 
            if (customRenderer) {
                customRenderer( successReturnMap ) 
            } else {
                // the delegate didn't specify a closure that should be used to render the result 
                renderResult( successReturnMap )
            }
        } 
        catch (ApplicationException e) {
            this.response.setStatus( e.httpStatusCode ) 
            renderResult( (e.returnMap( localizer ) + [ data: params ]) )
        } 
        catch (e) { // CI logging
            this.response.setStatus( 500 ) 
            log.error "Caught unexpected exception ${e.class.simpleName} which may be a candidate for wrapping in a ApplicationException, message: ${e.message}", e
            renderResult( [ data: entity,
                            success: false, 
                            message: localizer( code: 'default.not.shown.message', 
                                                args: [ localizer( code: "${domainSimpleName}.label", default: "${domainSimpleName}" ) ] ),
                            errors: (e.hasProperty( 'errors' ) ? e.errors?.allErrors?.collect { localizer( error: it ) } : null),
                            underlyingErrorMessage: e.message ]  )
        }               
    } 
        
        
    def list = { 

        log.trace "${this.class.simpleName}.list invoked with ${this.params}"
        def entities
        def totalCount
        try {
//          if-else ... the delegate may substitute it's own implementation versus the default service.read (e.g., to use a query instead)
            entities = this."$serviceName".list( params )
            totalCount = this."$serviceName".count( params )
            def successReturnMap = [ success: true, 
                                     data: entities,
                                     totalCount: totalCount, 
                                     message: localizer( code: 'default.list.message',
                                                         args: [ localizer( code: "${domainSimpleName}.label", default: "${domainSimpleName}" ) ] ) ]
            def customRenderer
            if (this.class.metaClass.respondsTo( this.class, "renderList" )) {
                customRenderer = this.renderList( successReturnMap )
            } 
            if (customRenderer) {
                customRenderer( successReturnMap ) 
            } else {
                 // the delegate didn't specify a closure that should be used to render the result 
                    renderResult( successReturnMap )
            }
        } 
        catch (ApplicationException e) {
            this.response.setStatus( e.httpStatusCode ) 
            renderResult( (e.returnMap( localizer ) + [ data: params ]) )
        } 
        catch (e) { // CI logging
            this.response.setStatus( 500 ) 
            log.error "Caught unexpected exception ${e.class.simpleName} which may be a candidate for wrapping in a ApplicationException, message: ${e.message}", e
            renderResult( [ data: entities,
                            success: false, 
                            message: localizer( code: 'default.not.listed.message', 
                                                       args: [ localizer( code: "${domainSimpleName}.label", default: "${domainSimpleName}" ) ] ),
                            errors: (e.hasProperty( 'errors' ) ? e.errors?.allErrors?.collect { localizer( error: it ) } : null),
                            underlyingErrorMessage: e.message ]  )
        }               
    } 
                 
    
    private void renderResult( responseMap ) {
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
            // TODO: Need to support multiple approaches / formats / versions / etc.
            response.setHeader( "Content-Type", "application/xml" ) 
            render( responseMap as XML )
        } 
        else {
            throw new RuntimeException( "@@r1:com.sungardhe.framework.unsupported_content_type:${request.format}" )
        }        
    }

}
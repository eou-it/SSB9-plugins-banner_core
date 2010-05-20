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
 * Methods that may be injected into controllers, giving them a default RESTful API 
 * supporting XML and JSON representations.  Controllers can have these methods 
 * injected automatically simply by including the following line in their implementation:
 * <code>static defaultCrudActions = true</code>
 *
 * Controllers that also render GSP/Zul pages are responsible for implementing separate 
 * actions that do not conflict with the default ones. (In general, this is expected 
 * to be limited to a 'view' action, as well as an index action that redirects to the view action.)
 *
 * NOTE: URI's containing 'api' will be directed to the appropriate controller 
 * action based upon HTTP method (see UriMappings.groovy).  These URIs will also 
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
 * Specifically, controllers using injected RESTful API methods may:
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
class DefaultRestfulControllerMethods { 
            
        
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
        
        // And now we'll inject the CRUD actions        

        if (!controllerClass.metaClass.respondsTo( controllerClass, "save" )) {

            controllerClass.metaClass.save = { 
                log.trace "${controllerClass.simpleName}.save invoked with ${delegate.params}"
                def localizer = { mapToLocalize -> delegate.message( mapToLocalize ) }
                def entity
                try {
                    entity = delegate."$serviceName".create( params )
                    def successReturnMap = [ success: true, 
                                             data: entity, 
                                             message:  localizer( code: 'default.created.message',
                                                                  args: [ localizer( code: "${domainSimpleName}.label", default: "${domainSimpleName}" ), 
                                                                          entity.id ] ) ]
                    def customRenderer
                    if (controllerClass.metaClass.respondsTo( controllerClass, "renderSave" )) {
                        customRenderer = delegate.renderSave( successReturnMap )
                    } 
                    if (customRenderer) {
                        customRenderer( successReturnMap ) 
                    } else {
                        // the delegate didn't specify a closure that should be used to render the result 
                        delegate.response.status = 201 // the 'created' code
                        renderResult( delegate, successReturnMap )
                    }
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
                                              message: localizer( code: 'default.not.created.message', 
                                                                           args: [ localizer( code: "${domainSimpleName}.label", default: "${domainSimpleName}" ) ] ),
                                              errors: (e.hasProperty( 'errors' ) ? e.errors?.allErrors?.collect { localizer( error: it ) } : null),
                                              underlyingErrorMessage: e.message ]  )
                }               
            }             
        } 
        
        if (!controllerClass.metaClass.respondsTo( controllerClass, "update" )) {

            controllerClass.metaClass.update = { 
        
                log.trace "${controllerClass.simpleName}.update invoked with ${delegate.params}"
                def localizer = { mapToLocalize -> delegate.message( mapToLocalize ) }
                def entity
                try {
                    entity = delegate."$serviceName".update( params )
                    def successReturnMap = [ success: true, 
                                             data: entity, 
                                             message:  localizer( code: 'default.updated.message',
                                                                  args: [ localizer( code: "${domainSimpleName}.label", default: "${domainSimpleName}" ), 
                                                                          entity.id ] ) ]
                    def customRenderer
                    if (controllerClass.metaClass.respondsTo( controllerClass, "renderUpdate" )) {
                        customRenderer = delegate.renderUpdate( successReturnMap )
                    } 
                    if (customRenderer) {
                        customRenderer( successReturnMap ) 
                    } else {
                        // the delegate didn't specify a closure that should be used to render the result 
                        renderResult( delegate, successReturnMap )
                    }
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
                                              message: localizer( code: 'default.not.updated.message', 
                                                                         args: [ localizer( code: "${domainSimpleName}.label", default: "${domainSimpleName}" ) ] ),
                                              errors: (e.hasProperty( 'errors' ) ? e.errors?.allErrors?.collect { localizer( error: it ) } : null),
                                              underlyingErrorMessage: e.message ]  )
                }               
        
            } 
        } 

        if (!controllerClass.metaClass.respondsTo( controllerClass, "delete" )) {

            controllerClass.metaClass.delete = { 

                log.trace "${controllerClass.simpleName}.delete invoked with ${delegate.params}"
                def localizer = { mapToLocalize -> delegate.message( mapToLocalize ) }
                try {
                    delegate."$serviceName".delete( params )
                    def successReturnMap = [ success: true, 
                                             data: null, 
                                             message:  localizer( code: 'default.deleted.message',
                                                                  args: [ localizer( code: "${domainSimpleName}.label", default: "${domainSimpleName}" ), 
                                                                          params.id ] ) ]
                    def customRenderer
                    if (controllerClass.metaClass.respondsTo( controllerClass, "renderDelete" )) {
                        customRenderer = delegate.renderDelete( successReturnMap )
                    } 
                    if (customRenderer) {
                        customRenderer( successReturnMap ) 
                    } else {
                        // the delegate didn't specify a closure that should be used to render the result 
                        renderResult( delegate, successReturnMap )
                    }
                } 
                catch (ApplicationException e) {
                    delegate.response.setStatus( e.httpStatusCode ) 
                    renderResult( delegate, (e.returnMap( localizer ) + [ data: params ]) )
                } 
                catch (e) { // CI logging
                    delegate.response.setStatus( 500 ) 
                    log.error "Caught unexpected exception ${e.class.simpleName} which may be a candidate for wrapping in a ApplicationException, message: ${e.message}", e
                    renderResult( delegate, [ data: null,
                                              success: false, 
                                              message: localizer( code: 'default.not.deleted.message', 
                                                                         args: [ localizer( code: "${domainSimpleName}.label", default: "${domainSimpleName}" ) ] ),
                                              errors: (e.hasProperty( 'errors' ) ? e.errors?.allErrors?.collect { localizer( error: it ) } : null),
                                              underlyingErrorMessage: e.message ]  )
                }               
            } 
        } 
        
        if (!controllerClass.metaClass.respondsTo( controllerClass, "show" )) {

            controllerClass.metaClass.show = { 

                log.trace "${controllerClass.simpleName}.show invoked with ${delegate.params}"
                def localizer = { mapToLocalize -> delegate.message( mapToLocalize ) }
                def entity
                try {
                    entity = delegate."$serviceName".read( params.id )
                    def successReturnMap = [ success: true, 
                                             data: entity, 
                                             message:  localizer( code: 'default.show.message',
                                                                  args: [ localizer( code: "${domainSimpleName}.label", default: "${domainSimpleName}" ) ] ) ]
                    def customRenderer
                    if (controllerClass.metaClass.respondsTo( controllerClass, "renderShow" )) {
                        customRenderer = delegate.renderShow( successReturnMap )
                    } 
                    if (customRenderer) {
                        customRenderer( successReturnMap ) 
                    } else {
                        // the delegate didn't specify a closure that should be used to render the result 
                        renderResult( delegate, successReturnMap )
                    }
                } 
                catch (ApplicationException e) {
                    delegate.response.setStatus( e.httpStatusCode ) 
                    renderResult( delegate, (e.returnMap( localizer ) + [ data: params ]) )
                } 
                catch (e) { // CI logging
                    delegate.response.setStatus( 500 ) 
                    log.error "Caught unexpected exception ${e.class.simpleName} which may be a candidate for wrapping in a ApplicationException, message: ${e.message}", e
                    renderResult( delegate, [ data: entity,
                                              success: false, 
                                              message: localizer( code: 'default.not.shown.message', 
                                                                  args: [ localizer( code: "${domainSimpleName}.label", default: "${domainSimpleName}" ) ] ),
                                              errors: (e.hasProperty( 'errors' ) ? e.errors?.allErrors?.collect { localizer( error: it ) } : null),
                                              underlyingErrorMessage: e.message ]  )
                }               
            } 
        } 
        
        if (!controllerClass.metaClass.respondsTo( controllerClass, "list" )) {

            controllerClass.metaClass.list = { 

                log.trace "${controllerClass.simpleName}.list invoked with ${delegate.params}"
                def localizer = { mapToLocalize -> delegate.message( mapToLocalize ) }

                def entities
                def totalCount
                try {
//                  if-else ... the delegate may substitute it's own implementation versus the default service.read (e.g., to use a query instead)
                    entities = delegate."$serviceName".list( params )
                    totalCount = delegate."$serviceName".count( params )
                    def successReturnMap = [ success: true, 
                                             data: entities,
                                             totalCount: totalCount, 
                                             message: localizer( code: 'default.list.message',
                                                                 args: [ localizer( code: "${domainSimpleName}.label", default: "${domainSimpleName}" ) ] ) ]
                    def customRenderer
                    if (controllerClass.metaClass.respondsTo( controllerClass, "renderList" )) {
                        customRenderer = delegate.renderList( successReturnMap )
                    } 
                    if (customRenderer) {
                        customRenderer( successReturnMap ) 
                    } else {
                        // the delegate didn't specify a closure that should be used to render the result 
                        renderResult( delegate, successReturnMap )
                    }
                } 
                catch (ApplicationException e) {
                    delegate.response.setStatus( e.httpStatusCode ) 
                    renderResult( delegate, (e.returnMap( localizer ) + [ data: params ]) )
                } 
                catch (e) { // CI logging
                    delegate.response.setStatus( 500 ) 
                    log.error "Caught unexpected exception ${e.class.simpleName} which may be a candidate for wrapping in a ApplicationException, message: ${e.message}", e
                    renderResult( delegate, [ data: entities,
                                              success: false, 
                                              message: localizer( code: 'default.not.listed.message', 
                                                                         args: [ localizer( code: "${domainSimpleName}.label", default: "${domainSimpleName}" ) ] ),
                                              errors: (e.hasProperty( 'errors' ) ? e.errors?.allErrors?.collect { localizer( error: it ) } : null),
                                              underlyingErrorMessage: e.message ]  )
                }               
            } 
        } 
    }
             
    
    private static void renderResult( delegate, responseMap ) {
        
        if (delegate.request.format ==~ /.*html.*/) {
            delegate.response.setHeader( "Content-Type", "application/html" ) 
            delegate.render( responseMap )
        } 
        else if (delegate.request.format ==~ /.*json.*/) {
            delegate.response.setHeader( "Content-Type", "application/json" ) 
            delegate?.renderResult( responseMap as JSON )
        } 
        else if (delegate.request.format ==~ /.*xml.*/) {
            // TODO: Need to support multiple approaches / formats / versions / etc.
            delegate.response.setHeader( "Content-Type", "application/xml" ) 
            delegate.render( responseMap as XML )
        } 
        else {
            throw new RuntimeException( "@@r1:com.sungardhe.framework.unsupported_content_type:${delegate.request.format}" )
        }        
    }

}

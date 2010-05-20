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
 * 1) Define a map that specifies appropriate returnMap maps IF the default ones do not suffice:
 *
 * static def overrides = [
 *     save: [ success: [ success: true, 
 *                        data: foo, 
 *                        someExtraInfoNotAvailableInTheDefaultReturnMap: "my extra, very important information"
 *                        message: "${message( code: 'foo.some_really_special.message', \
 *                        args: [ message( code: 'foo.label', default: 'Foo'), foo.code ] )}" ]}
 *           ],
 *     update: [ success: [ success: true, 
 *                          data: foo, 
 *                          message: "${message( code: 'foo.some_really_special.message', \
 *                          args: [ message( code: 'foo.label', default: 'Foo'), foo.code ] )}" ]}
 *           ]
 *     delete: etc...
 * ]
 * 
 * 2) Define an 'extractParams' closure IF the default extraction of params does not suffice. If 
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
                    def successReturnMap
//                  if - else .... the delegate doesn't specify a custom success return map for 'save'.... TODO: implement!
                        successReturnMap = [ success: true, 
                                             data: entity, 
                                             message:  localizer( code: 'default.created.message',
                                                                         args: [ localizer( code: "${domainSimpleName}.label", default: "${domainSimpleName}" ), 
                                                                                 entity.id ] ) ]
                    delegate.response.status = 201
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
                    def successReturnMap
//                  if - else .... the delegate doesn't specify a custom success return map for 'update' .... TODO: implement!
                        successReturnMap = [ success: true, 
                                             data: entity, 
                                             message:  localizer( code: 'default.updated.message',
                                                                  args: [ localizer( code: "${domainSimpleName}.label", default: "${domainSimpleName}" ), 
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
                    def successReturnMap
//                  if - else .... the delegate doesn't specify a custom success return map for 'delete' .... TODO: implement!
                        successReturnMap = [ success: true, 
                                             data: null, 
                                             message:  localizer( code: 'default.deleted.message',
                                                                         args: [ localizer( code: "${domainSimpleName}.label", default: "${domainSimpleName}" ) ] ) ]
                    renderResult( delegate, successReturnMap )
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
                    def successReturnMap
//                  if - else .... the delegate doesn't specify a custom success return map for 'delete' .... TODO: implement!
                        successReturnMap = [ success: true, 
                                             data: entity, 
                                             message:  localizer( code: 'default.show.message',
                                                                  args: [ localizer( code: "${domainSimpleName}.label", default: "${domainSimpleName}" ) ] ) ]
                    renderResult( delegate, successReturnMap )
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
                    def successReturnMap
//                  if - else .... the delegate doesn't specify a custom success return map for 'list' .... TODO: implement!
                        successReturnMap = [ success: true, 
                                             data: entities,
                                             totalCount: totalCount, 
                                             message: localizer( code: 'default.list.message',
                                                                 args: [ localizer( code: "${domainSimpleName}.label", default: "${domainSimpleName}" ) ] ) ]
                    renderResult( delegate, successReturnMap )
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

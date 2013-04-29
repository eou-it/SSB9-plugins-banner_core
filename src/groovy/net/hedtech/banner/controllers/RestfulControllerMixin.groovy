/*******************************************************************************
Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
*******************************************************************************/
package net.hedtech.banner.controllers

import net.hedtech.banner.exceptions.*
import net.hedtech.banner.representations.RepresentationBuilder
import net.hedtech.banner.representations.ResourceRepresentationHandler
import net.hedtech.banner.representations.ResourceRepresentationRegistry
import net.hedtech.banner.service.KeyBlockHolder as KBH

import grails.converters.JSON
import grails.converters.XML
import grails.util.GrailsNameUtils

import org.apache.log4j.Logger

import org.codehaus.groovy.grails.commons.ApplicationHolder as AH
import org.codehaus.groovy.grails.commons.ApplicationHolder
import net.hedtech.banner.representations.ParamsExtractor

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
 * actions that do not conflict with the RESTful ones.
 * Note that it is also possible to use the mixed-in actions non-RESTfully (by identifying the action
 * within the URL), although that is not a recommend usage.
 *
 * URI's containing 'api' will be directed to the appropriate controller
 * action based upon HTTP method (as specified in UriMappings.groovy).
 * These URIs will also be authenticated using BasicAuthentication.
 *
 * URIs that do not have 'api' in their name will be directed to the appropriate action
 * using non-RESTful conventions (i.e., the action name must be part of the URI).
 *
 * Any of the default RESTful API actions may be overridden by implementing the action
 * within the specific controller.  They will not be mixed-in if they are already present.
 *
 * Custom representations and custom 'params extraction' may be implemented within the controller,
 * although this custom support may be configured separately (to avoid having to touch the controller
 * when changing representation support).
 *
 * Custom representations are needed primarily for two situations:
 *    a) The default Grails Converters (for XML or JSON) cannot properly handle a particular complex object.
 *       This mixin will use default Grails converters by default.
 *    b) A custom MIME type and corresponding XML Schema is employed to provide a stable representation, allowing
 *       for simultaneous support of older and newer representations (versions).
 *
 * The ResourceRepresentationRegistry is used to externalize custom representation support from controllers.
 * The resource representation support may be configured via the bannerRepresentationHandlerMap contained
 * within Config.groovy (for SunGardHE built-in representations) and via the customRepresentationHandlerMap
 * contained within an external configuration (see CustomRepresentationConfig.groovy).
 *
 * Registering custom representation support within this registry should preclude the need for implementing
 * custom representation support directly within a controller.  However, custom representation support may
 * be 'hardcoded' within a controller if that controller exposes the following two methods:
 *
 *  1)    public RepresentationBuilder getCustomRepresentationBuilder( String actionName ) {...}
 *
 * If implemented, this method should return a RepresentationBuilder that can create an appropriate representation
 * for the current request.  If there is no RepresentationBuilder that can create an appropriate representation
 * for the current request, the method should return null.  Returning 'null' indicates that the controller doesn't
 * explicitly support the representation needed to satisfy the current request.
 *
 *  2)   "public ParamsExtractor getParamsExtractor() {...}
 *
 * If implemented, this method should return a ParamsExtractor that can extract request data into a map (that will
 * subsequently be added to the Grails params map by this mixin) for the specific request.
 *
 * This method, like the method discussed above, should return null if it cannot handle the current request.
 * This method is not needed if the default params extraction suffices, and will usually be necessary when using
 * a 'custom' representation (i.e., a new MIME type) or when the default converters simply cannot handle a complex model.
 *
 * It is recommended that both the getParamsExtractor() and getCustomRepresentationBuilder(actionName) methods be
 * thought of as a 'pair' when implementing custom representation support.  That is, it is recommended that if rendering
 * is configured within the ResourceRepresentationRegistry for a specific representation, the corresponding params
 * extraction would also be configured within the ResourceRepresentationRegistry.
 *
 * Using the ResourceRepresentationRegistry is recommended over implementing these two methods in your controller.
 */
class RestfulControllerMixin {

    static allowedMethods = [ show: "GET", list: "GET", create: "POST", update: "PUT", destroy: "DELETE" ]  // ensure RESTful

    Class  domainClass      // controllers may explicitly set, otherwise we'll derive based on naming conventions
    String domainSimpleName // determined from the domainClass, simply cached here for convenience
    String serviceName      // controllers may explicitly set, otherwise we'll derive based on naming conventions

    def ctx = AH.application.mainContext // The Spring ApplicationContext
    def resourceRepresentationRegistry   // note: the actions will set this when needed, as we cannot inject directly into a mixin

    @Lazy // note: Lazy needed here to ensure 'this' refers to the controller we're mixed into
    def log = Logger.getLogger( this.getClass() )


    // wrap the 'message' invocation within a closure, so it can be passed into an ApplicationException to localize error messages
    def localizer = { mapToLocalize ->
        this.message( mapToLocalize )
    }


    Class getDomainClass() {
        def domainClassName = this.class.name.substring( 0, this.class.name.length() - "Controller".length() )
        domainClass = domainClass ?: Class.forName( domainClassName, true, Thread.currentThread().getContextClassLoader() )
        domainClass
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
        "${request.scheme}://${request.serverName}:${request.serverPort}/${grailsApplication.metadata.'app.name'}/api/${GrailsNameUtils.getPropertyNameRepresentation( domainSimpleName )}"
    }


    ResourceRepresentationRegistry getResourceRepresentationRegistry() {
        if (!resourceRepresentationRegistry) {
            resourceRepresentationRegistry = ctx.resourceRepresentationRegistry
        }
        resourceRepresentationRegistry
    }


    /**
     * Retrieves a params extraction Closure from the ResourceRegistrationRegistry, or null if none is found.
     * @param representationName the name of the representation, preferrably a custom MIME type
     * @param modelClass the Class of the model (aka 'resource')
     * @return Closure a closure that can perform the necessary params extraction
     */
    public ParamsExtractor retrieveParamsExtractorFromRegistry( String representationName, modelClass ) {
        ResourceRepresentationHandler handler = getResourceRepresentationRegistry().get( representationName, modelClass )
        handler?.paramsExtractor()
    }


    /**
     * Retrieves a Representation handler from the ResourceRegistrationRegistry, or null if none is found.
     * @param representatinName the name of the representation, preferably a custom MIME type
     * @param actionName the string name of the action (one of 'list', 'show', 'create', or 'update'
     * @param modelClass the Class of the model (aka 'resource')
     * @return Closure a closure that can perform the necessary formatting
     */
    public RepresentationBuilder retrieveRepresentationBuilderFromRegistry( String representationName, String actionName, modelClass ) { // note: this method cannot be found if the 3rd argument is typed 'Class'
        log.debug "RestfulControllerMixin.representationBuilderFor will try to find a custom representationBuilder within the registry"
        ResourceRepresentationHandler handler = getResourceRepresentationRegistry().get( representationName, modelClass )
        switch( actionName ) {
            case 'list'   : return handler?.collectionBuilder()  as RepresentationBuilder
            case 'show'   : return handler?.singleBuilder()      as RepresentationBuilder
            case 'create' : return handler?.singleBuilder()      as RepresentationBuilder
            case 'update' : return handler?.singleBuilder()      as RepresentationBuilder
            default       : return null
            // note: currently 'delete' is not supported with custom rendering..
        }
    }


// ------------------------------------------- Controller Actions -------------------------------------


    def create = {

        log.trace "${this.class.simpleName}.create invoked with params $params and format $request.format"
        
        KBH.markAsOptional() // REST APIs do not expose or use a KeyBlock
        
        def extractedParams = extractParams()
        def entity
        log.trace "${this.class.simpleName}.create will invoke ${serviceName}.create( $extractedParams ) - service"
        try {
            entity = this."${getServiceName()}".create( extractedParams )
	        log.trace "${this.class.simpleName}.create has created entity $entity?.class (id - $entity?.id) and will prepare the response"
            def successReturnMap = [ success:          true,
                                     data:             entity,
                                     refBase:          refBase( request ),
                                     supplementalData: entity.hasSupplementalProperties() ? entity.supplementalProperties : null,
                                     message:          localizer( code: 'default.created.message',
                                                                  args: [ localizer( code: "${domainSimpleName}.label", default: "${domainSimpleName}" ),
                                                                          entity.id ] ) ]
			log.debug  "${this.class.simpleName}.create will create response from map: $successReturnMap"
            this.response.status = 201 // the 'created' code
            def result = representationBuilderFor( "create" ).buildRepresentation( successReturnMap )
            log.debug  "${this.class.simpleName}.create will render $result"
            render result
        }
        catch (ApplicationException e) {
            this.response.setStatus( e.httpStatusCode )
            def result = defaultRepresentationBuilder.buildRepresentation( e.returnMap( localizer ) + [ data: entity ] )
            log.debug "${this.class.simpleName}.create caught ApplicationException and will render $result"
            render result
        }
        catch (e) { // CI logging
            this.response.setStatus( 500 )
            log.error "Caught unexpected exception ${e.class.simpleName} which may be a candidate for wrapping in a ApplicationException, message: ${e.message}", e
            render( defaultErrorRenderMap( e, entity, 'default.not.created.message' ) )
        }
        finally {
            KBH.clear()    
        }
    }


    def update = {

        log.trace "${this.class.simpleName}.update invoked with params $params and format $request.format"
        
        KBH.markAsOptional() // REST APIs do not expose or use a KeyBlock
        
        def extractedParams = extractParams()
        def entity
        try {
            entity = this."${getServiceName()}".update( extractedParams )
            def successReturnMap = [ success:          true,
                                     data:             entity,
                                     refBase:          refBase( request ),
                                     supplementalData: entity.hasSupplementalProperties() ? entity.supplementalProperties : null,
                                     message:          localizer( code: 'default.updated.message',
                                                                  args: [ localizer( code: "${domainSimpleName}.label", default: "${domainSimpleName}" ),
                                                                          entity.id ] ) ]
            this.response.status = 200
            def result = representationBuilderFor( "update" ).buildRepresentation( successReturnMap )
            log.debug "${this.class.simpleName}.update will render $result"
            render result
        }
        catch (ApplicationException e) {
            this.response.setStatus( e.httpStatusCode )
            def result = defaultRepresentationBuilder.buildRepresentation( e.returnMap( localizer ) + [ data: entity ] )
            log.debug "${this.class.simpleName}.update caught ApplicationException and will render $result"
            render result
        }
        catch (e) { // CI logging
            this.response.setStatus( 500 )
            log.error "Caught unexpected exception ${e.class.simpleName} which may be a candidate for wrapping in a ApplicationException, message: ${e.message}", e
            render( defaultErrorRenderMap( e, entity, 'default.not.updated.message' ) )
        }
        finally {
            KBH.clear()    
        }
    }


    def destroy = {

        log.trace "${this.class.simpleName}.destroy invoked with params $params and format $request.format"
        
        KBH.markAsOptional() // REST APIs do not expose or use a KeyBlock
        
        if (params?.size() < 1) {
            extractParams()
            log.warn "destroy() required explicit extraction of params -- this is normally not needed as the id is provided within the URI"
        }

        // Note that a HTTP DELETE will not have a body, and we should not attempt to extract JSON out of the request.
        // Instead, we should expect the 'id' to be mapped for us, as it is provided as part of the URI.
        try {
            this."${getServiceName()}".delete( params )
            def successReturnMap = [ success:  true,
                                     data:     null,
                                     message:  localizer( code: 'default.deleted.message',
                                                          args: [ localizer( code: "${domainSimpleName}.label", default: "${domainSimpleName}" ),
                                                                  params.id ] ) ]
            this.response.status = 200
            def result = representationBuilderFor( "destroy" ).buildRepresentation( successReturnMap )
            log.debug "${this.class.simpleName}.destroy will render $result"
            render result
        }
        catch (ApplicationException e) {
            this.response.setStatus( e.httpStatusCode )
            def result = defaultRepresentationBuilder.buildRepresentation( e.returnMap( localizer ) )
            log.debug "${this.class.simpleName}.destroy caught ApplicationException and will render $result"
            render result
        }
        catch (e) { // CI logging
            this.response.setStatus( 500 )
            log.error "Caught unexpected exception ${e.class.simpleName} which may be a candidate for wrapping in a ApplicationException, message: ${e.message}", e
            render( defaultErrorRenderMap( e, entity, 'default.not.deleted.message' ) )
        }
        finally {
            KBH.clear()    
        }
    }


    def show = {
        
        log.trace "${this.class.simpleName}.show invoked with params $params and format $request.format"
        
        KBH.markAsOptional() // REST APIs do not expose or use a KeyBlock
        def entity

        if (params?.size() < 1) {
            extractParams()
            log.warn "show() required explicit extraction of params -- this is normally not needed as the id is provided within the URI"
        }
        try {
            entity = this."${getServiceName()}".read( params.id )
            def successReturnMap = [ success:         true,
                                     data:             entity,
                                     refBase:          refBase( request ),
                                     supplementalData: entity.hasSupplementalProperties() ? entity.supplementalProperties : null,
                                     message:          localizer( code: 'default.show.message',
                                                                  args: [ localizer( code: "${domainSimpleName}.label", default: "${domainSimpleName}" ) ] ) ]
            this.response.status = 200
            def result = representationBuilderFor( "show" ).buildRepresentation( successReturnMap )
            log.debug "${this.class.simpleName}.show will render $result"
            render result
        }
        catch (ApplicationException e) {
            this.response.setStatus( e.httpStatusCode )
            def result = defaultRepresentationBuilder.buildRepresentation( e.returnMap( localizer ) + [ data: params ] )
            log.debug "${this.class.simpleName}.show caught ApplicationException and will render $result"
            render result
        }
        catch (e) { // CI logging
            log.error "Caught unexpected exception ${e.class.simpleName} which may be a candidate for wrapping in a ApplicationException, message: ${e.message}", e
            e.printStackTrace()
            this.response.setStatus( 500 )
            render( defaultErrorRenderMap( e, entity, 'default.not.shown.message' ) )
        }
        finally {
            KBH.clear()    
        }
    }


    def list = {

        log.trace "${this.class.simpleName}.list invoked with params $params and format $request.format"
        
        KBH.markAsOptional() // REST APIs do not expose or use a KeyBlock

        if (params?.size() < 1) {
            extractParams()
            log.warn "list() required explicit extraction of params -- this is normally not needed as the id is provided within the URI"
        }

        def entities
        def totalCount
        try {
            entities = this."${getServiceName()}".list( params )
            totalCount = this."${getServiceName()}".count( params )
            def successReturnMap = [ success:     true,
                                     data:        entities,
                                     totalCount:  totalCount,
                                     pageOffset:  params.offset ? params?.offset : 0,
                                     pageMaxSize: params.max ? params?.max : totalCount,
                                     refBase:     refBase( request ),
                                     message:     localizer( code: 'default.list.message',
                                                             args: [ localizer( code: "${domainSimpleName}.label", default: "${domainSimpleName}" ) ] ) ]
            this.response.status = 200
            def result = representationBuilderFor( "list" ).buildRepresentation( successReturnMap )
            log.debug "${this.class.simpleName}.list will render $result"
            render result
        }
        catch (ApplicationException e) {
            this.response.setStatus( e.httpStatusCode )
            def result = defaultRepresentationBuilder.buildRepresentation( e.returnMap( localizer ) + [ data: params ] )
            log.debug "${this.class.simpleName}.list caught ApplicationException and will render $result"
            render result
        }
        catch (e) { // CI logging
            this.response.setStatus( 500 )
            log.error "Caught unexpected exception ${e.class.simpleName} which may be a candidate for wrapping in a ApplicationException, message: ${e.message}", e
            render( defaultErrorRenderMap( e, entities, 'default.not.listed.message' ) )
        }
        finally {
            KBH.clear()    
        }
    }


// ----------------------------------- Helper Methods -----------------------------------


    private Map defaultErrorRenderMap( e, data, messageCode ) {
        [ data:                   data,
          success:                false,
          message:                localizer( code: messageCode,
                                             args: [ localizer( code: "${domainSimpleName}.label", default: "${domainSimpleName}" ) ] ),
          errors:                 (e.hasProperty( 'errors' ) ? e.errors?.allErrors?.collect { localizer( error: it ) } : null),
          underlyingErrorMessage: e.message ]
    }


    /**
     * Returns a populated params map.
     * If the controller has a 'getParamsExtractor()' method, that extractor will be used. If not,
     * the ResourceRepresentationRegistery will be checked. If still no extractor, the default extractor will be used.
     * @return Map a populated params map
     */
    private Map extractParams() {

        log.debug "extractParams() will ask the registry for a params extractor supporting content type ${request?.getHeader( 'Content-Type' )} and domain class ${getDomainClass()}"
        ParamsExtractor extractor = retrieveParamsExtractorFromRegistry( request?.getHeader( 'Content-Type' ), getDomainClass() )

        if (extractor) {
	        log.debug "extractParams() found an extractor within the registry"
        } else if (this.class.metaClass.respondsTo( this.class, "getParamsExtractor" )) {
            log.debug "extractParams() will now see if the controller provides support, by calling ${this.class}.getParamsExtractor()"
            extractor = this.getParamsExtractor()
        }

        if (!extractor) {
            log.debug "extractParams() did not find a custom params extractor and will attempt to use a default extractor"
            extractor = defaultParamsExtractor
        }
        params << extractor?.extractParams( request )
        params
    }


    // A default params extractor that uses built-in Grails support for parsing JSON and XML.
    private ParamsExtractor defaultParamsExtractor = { request ->

        Map paramsContent = [:]
        if (request.format ==~ /.*html.*/) {
            log.debug "${this.class.simpleName} HTML request format will use pre-populated params $params"
            paramsContent
        }
        else if (request.format ==~ /.*json.*/) {
            request.JSON.entrySet()?.each {
	            log.trace "${this.class.simpleName} has extracted property $it.key with value ${it.value}"
                paramsContent.put it.key, it.value
            }
            log.debug "${this.class.simpleName} has extracted JSON content from the request and populated params $paramsContent"
            paramsContent
        }
        else if (request.format ==~ /.*xml.*/) {
            request.XML.children().each {
	            log.trace "${this.class.simpleName} has extracted property $it.name with value ${it.text()}"
                paramsContent.put it.name(), it.text()
            }
            log.debug "${this.class.simpleName} has extracted XML content from the request and populated params $paramsContent"
            paramsContent
        }
        else {
            throw new RuntimeException( "@@r1:net.hedtech.framework.unsupported_content_type:${request.format}" )
        }
    } as ParamsExtractor


    private  RepresentationBuilder representationBuilderFor( String actionName ) {
        RepresentationBuilder representationBuilder = retrieveRepresentationBuilderFromRegistry( request?.getHeader( 'Accept' ), actionName, getDomainClass() )
        if (representationBuilder) {
            log.debug "RestfulControllerMixin.representationBuilderFor found a custom representationBuilder within the registry"
        } else {
            log.debug "RestfulControllerMixin.representationBuilderFor will try to find a custom representationBuilder within the controller itself"
            if (this.class.metaClass.respondsTo( this.class, "getCustomRepresentationBuilder" )) {
                representationBuilder = this.getCustomRepresentationBuilder( actionName )
            }
        }
        if (representationBuilder) {
            // we found support for the current request, so we'll set the response content type before returning the representationBuilder
            response.setHeader( "Content-Type", request?.getHeader( 'Accept' ) )
        } else {
            log.debug "RestfulControllerMixin.representationBuilderFor did not find any custom representation support, and will use a default representationBuilder"
            // note the defaultrepresentationBuilder will set the appropriate content type
            representationBuilder = defaultRepresentationBuilder
        }
        representationBuilder
    }


    // Note that since custom content-types may not have been registered in Config.groovy as an 'xml' format,
    // we cannot rely on solely on 'request.format' (as the format is 'html' by default, and must be set to
    // another type).
    private RepresentationBuilder defaultRepresentationBuilder = { Map responseMap ->
        // We'll first try to use the Accept header
        if (request.getHeader( 'Accept' ) ==~ /.*html.*/) {
            response.setHeader( "Content-Type", "application/html" )
            return responseMap.toString()
        }
        else if (request.getHeader( 'Accept' ) ==~ /.*json.*/) {
            response.setHeader( "Content-Type", "application/json" )
            return (responseMap as JSON).toString()
        }
        else if (request.getHeader( 'Accept' ) ==~ /.*xml.*/) {
            response.setHeader( "Content-Type", "application/xml" )
            return (responseMap as XML).toString()
        }
        // but if that doesn't work, we'll fall back to the format determined by grails
        else if (request.format  ==~ /.*json.*/) {
            response.setHeader( "Content-Type", "application/json" )
            return (responseMap as JSON).toString()
        }
        else if (request.format ==~ /.*xml.*/) {
            response.setHeader( "Content-Type", "application/xml" )
            return (responseMap as XML).toString()
        }
        else {
            throw new RuntimeException( "@@r1:net.hedtech.framework.unsupported_content_type:${request.format}" )
        }
    } as RepresentationBuilder

}

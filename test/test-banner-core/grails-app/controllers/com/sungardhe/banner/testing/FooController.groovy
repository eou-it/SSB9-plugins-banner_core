/** *****************************************************************************
 © 2010 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
package com.sungardhe.banner.testing

import org.apache.log4j.Logger
import grails.util.GrailsNameUtils
import com.sungardhe.banner.representations.RepresentationBuilder
import com.sungardhe.banner.representations.ParamsExtractor

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
class FooController  {

    // The mixInRestActions lists actions that will be mixed into this class and that will be registered with the corresponding Grails 'artefact'.
    // Note that all REST actions implemented by RestfulControllerMixin will be mixed-in, only those specified here will
    // be registered with the artefact (so that URL mapping will be allowed only to these actions).
    static List mixInRestActions = [ 'show', 'list', 'create', 'update', 'destroy' ]

    def fooService  // injected by Spring

    def invokedRenderCallbacks = [] // this is used for testing the framework -- it is NOT something controllers would normally have


    public FooRestfulController() {
        // domainClass = "Foo"        // explicitly set only when not following normal naming conventions for your controller
        // serviceName = "fooService" // explicitly set only when not following normal grails naming conventions
    }


    // ---------------------------- User Interface Actions (non-RESTful) ----------------------------------
    // The following actions are here solely to illustrate that additional actions can be implemented here.
    // In general, our controllers will support only RESTful clients for integration purposes, and will
    // not have non-RESTful actions like 'index' or 'view'.  It IS possible to invoke the mixed-in actions
    // non-restfully if necessary (i.e., by using a URL that specifies the action name).


    // in case someone uses a URI explicitly indicating 'index' or our URI mapping includes a non-RESTful mapping to 'index'
    def index = {
        redirect( action: "view", params: params )
    }


    // Render main User Interface page -- note that ALL other actions are provided by the base class. :-)
    def view = {
        render "If I had a UI, I'd render it now!"
        // Render a UI supporting this model.  All subsequent requests from this UI will be
        // handled by the corresponding 'Composer'.  This is the +only+ action supporting the ZK based user interface.
        // The other actions in this controller support RESTful clients.
    }


    // ------------------------------------ Custom Representation Support Methods --------------------------------------


    // Developer note:
    //
    // Support for custom resources may be implemented explicitly within your controller by implementing 'getCustomRepresentationBuilder'
    // and 'getParamsExtractor' methods that return Closures, as shown below.
    //
    // Note that generally, it will be best to implement custom representation support outside of this controller, by specifying
    // the custom handling within the Config.groovy representationHandlerMap map.  The representationHandlerMap map allows for support
    // to be implemented using either in-line groovy or separate classes.
    //
    // The ResourceRepresentationRegistry is used by the mixed-in RESTful actions to attain custom representation support, and
    // only if it cannot be found will this controller be given the opportunity to provide the support. This allows externalized
    // configuration to 'override' any hardcoded implementation contained in a controller.
    //
    // The following discusses how to expose custom representation support within a controller.  The 'getCustomRepresentationBuilder'
    // and 'getParamsExtractor' methods must each return a Closure when one is available that can be subsequently used to provide
    // the needed handling. 
    //
    // Closures that are returned which can create a custom representation must be applicable for the current request content type.
    //
    // The closure returned by this method may be able to support multiple content types or just a single content type, but
    // it is the responsibility of this method to return a closure that can create a representation applicable for the current request.
    //
    // If the getCustomRepresentationBuilder() returns null, the mixed-in action will attempt to retrieve custom representation support
    // from the ResourceRepresentationRegistry. If the registry cannot provide a closure appropriate for the current request, the
    // mixed-in action will simply render a default representation (using standard Grails Converters), for XML or JSON.
    //
    // Note that registering custom support within the ResourceRepresentationRegistry (via Spring configuration) may likely preclude
    // any need to implement this method or to modify this method as new representations are needed.
    // If neither the registry nor this controller can provide support for a needed representation, the mixed-in action will
    // use default Grails rendering.
    //
    /**
     * Returns a Closure that can create an appropriate representation for a resource, or null if no support for a needed
     * representation is available.
     * @param actionName the controller action for which a custom representation is needed for rendering
     * @return Closure a closure that can create an appropriate representation of a resource, so that it can be subsequently rendered
     */
    public RepresentationBuilder getCustomRepresentationBuilder( String actionName ) {
        log.trace "getCustomRepresentationBuilder() invoked with actionName $actionName, and the request format is ${request.getHeader( 'Content-Type' )}"

        // not normally present, this is used solely for testing the framework.  Custom renderers for Foo are registered
        // in the ResourceRepresentationRegistry instead of here, so this method will return null.  If we didn't need
        // the 'invokedRenderCallbacks' list for testing, we would have ommitted this method completely.
        invokedRenderCallbacks << actionName

        // Now we'll return a custom representation builder directly from this class. We'll simply reuse the
        // support within the ResourceRepresentationRegistry, but that'll be our secret -- as far as the mixed-in RESTful
        // actions know, we've implemented the support here. (We'll 'support' v0_01 but return support for v0_02)
        RepresentationBuilder builder = null
        if (request.getHeader( 'Content-Type' ) == "application/vnd.sungardhe.student.v0.01+xml") {
            // 'retrieveRendererFromRegistry' method is provided by the RestfulControllerMixin class
            builder = retrieveRepresentationBuilderFromRegistry( "application/vnd.sungardhe.student.v0.02+xml", actionName, Foo )
        }
        builder
    }


    // --------------------------- Special 'params' handling  ---------------------------


    // We'll return a closure for any specific content type formats that we explicitly support.
    // If we don't return a closure, or if we don't even expose an 'extractParams' method at all,
    // the base class will check Spring for a registered handler class, and lastly will fall back to
    // it's default param extraction.
    //
    public ParamsExtractor getParamsExtractor() {
        // For demonstration and testing purposes, we'll hardcode support for 'application/vnd.sungardhe.student.v0.01+xml'
        // (but this will really leverage the support for v01_02 from the registry, as a convenience)
        ParamsExtractor extractor = null
        if (request.getHeader( 'Content-Type' ) == "application/vnd.sungardhe.student.v0.01+xml") {
            // 'retrieveParamsExtractorFromRegistry' method is provided by the RestfulControllerMixin class
            extractor = retrieveParamsExtractorFromRegistry( "application/vnd.sungardhe.student.v0.02+xml", Foo )
        }
        extractor
    }

}

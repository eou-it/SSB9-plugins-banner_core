/** *****************************************************************************
 © 2010 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
package com.sungardhe.banner.testing

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


    // A constructor is only needed when you need to explicitly set a domainClass or serviceName (see comments below).
    public FooRestfulController() {
        // domainClass = "Foo"        // explicitly set only when not following normal naming conventions for your controller
        // serviceName = "fooService" // explicitly set only when not following normal grails naming conventions
    }


    // ---------------------------- User Interface Actions (non-RESTful) ----------------------------------
    // The following actions are here solely to illustrate that additional actions can be implemented here.
    // In general, our controllers will support RESTful clients for integration purposes, and will
    // not usually have non-RESTful actions like 'index' or 'view' below.  It IS possible to invoke the mixed-in
    // actions non-restfully if necessary (i.e., by using a URL that specifies the action name).


    // in case someone uses a URI explicitly indicating 'index' or our URI mapping includes a non-RESTful mapping to 'index'
    // Otherwise, this would never be called (as a GET using a URI without explicitly specifying 'index' would result in
    // a RESTful 'GET' of the resource using a 'show' mixed-in action.
    def index = {
        redirect( action: "view", params: params )
    }


    // Render content (e.g., HTML)  -- note that ALL other actions are provided by the base class. :-)
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
    // and 'getParamsExtractor' methods as shown below.
    //
    // Note that generally, it will be best to implement custom representation support outside of this controller, by specifying
    // the custom handling within the representationHandlerMap map contained in Config.groovy or the customRepresentationHandlerMap
    // map contained in the CustomRepresentationConfig.groovy file.  Configuring custom representation support within these
    // configuration files will ensure this support is registered within the ResourceRepresentationRegistry registry.
    //
    // The ResourceRepresentationRegistry is used by the mixed-in RESTful actions to attain custom representation support, and
    // only if it cannot be found will this controller be given the opportunity to provide the support. This allows externalized
    // configuration to 'override' any hardcoded implementation contained in a controller.
    //
    // A controller may provide explicit (hardcoded) support for a custom representation by implementing the two methods below.
    // Please read the RestfulControllerMixin.groovy class javadoc for details regarding the use of these methods.
    //
    /**
     * Returns a RepresentationBuilder that can create an appropriate representation for a resource, or null if no support for a needed
     * representation is available.
     * @param actionName the controller action for which a custom representation is needed for rendering
     * @return RepresentationBuilder a representation builder that can create an appropriate representation of a resource so that it can be subsequently rendered
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


    /* --------------------------- Special 'params' handling  ---------------------------


    /**
     * Returns a ParamsExtractor if one is available that can support the current request, or null if we cannot support the current request.
     * @return ParamsExtractor a params extractor that can extract params for the current request, or null if one isn't available
     */
    public ParamsExtractor getParamsExtractor() {

        // For demonstration and testing purposes, we'll expose support for 'application/vnd.sungardhe.student.v0.01+xml'
        // (but this will really leverage the support for v01_02 that is registered within the ResourceRepresentationRegistry
        // registry, so we don't have to implement it here. These 'test' MIME types all use the same XML Schema for convenience.)
        ParamsExtractor extractor = null
        if (request.getHeader( 'Content-Type' ) == "application/vnd.sungardhe.student.v0.01+xml") {
            // note: the 'retrieveParamsExtractorFromRegistry' method is provided by the RestfulControllerMixin class
            extractor = retrieveParamsExtractorFromRegistry( "application/vnd.sungardhe.student.v0.02+xml", Foo )
        }
        extractor
    }

}

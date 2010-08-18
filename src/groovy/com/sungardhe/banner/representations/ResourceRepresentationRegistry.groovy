/** *****************************************************************************
 © 2010 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
package com.sungardhe.banner.representations

import org.apache.log4j.Logger
import com.sungardhe.banner.configuration.ConfigurationUtils
import org.codehaus.groovy.grails.commons.ApplicationHolder as AH
import com.sungardhe.banner.exceptions.ApplicationException

/**
 * A registry that contains 'handlers' that support specific representations of resources.
 * When available, a MIME type should be used as the key to the map of handlers supporting
 * a specific representation. It's configuration must be found in the Grails configuration
 * (i.e., either in Config.groovy or another configuration file imported into Config.groovy).
 *
 * The configuration must look like:
 * 
 */
class ResourceRepresentationRegistry {

    private final Logger log = Logger.getLogger( getClass() )

    // Loaded from configuration during initialization, this map contains representation support
    // The key must be a MIME type and the value must be a Map, which in turn has a key of the fully qualified
    // class name of the model (or other business object that requires support).  Since a MIME type 'may' support
    // multiple models, a second map is needed to resolve specific support for a model.
    // e.g., [ "application/vnd.sungardhe.student.v0.01+xml": [ "com.sungardhe.banner.testing.Foo": support ]
    // where support may be either:
    //     a) a Map with 'paramsParser', 'singleRenderer', and 'listRenderer' keys, with in-line Closures as the values
    //     b) a string that resolves a Class, which when instantiated will expose the same 'keys' as if it were the map discussed above
    def representationHandlerMap 


    public void init() {
        representationHandlerMap = ConfigurationUtils.getConfiguration().representationHandlerMap
        if (!representationHandlerMap) {
            log.warn "ResourceRepresentationRegistry is empty, thus custom resource representations are not being supported."
        }
        log.info  "ResourceRepresentationRegistry initialization is complete."
    }


    /**
     * Returns a Map or object that contains the following handlers if registered.  Keys will only be populated when an appropriate value
     * needs to be returned.  Note that the return value may be either a Map or an object, but the programming model is the same.
     *     [ paramsParser: myParamsParserClosure,
     *       singleRenderer: mySingleRendererClosure,
     *       listRenderer: myListRendererClosure,
     *       representationName: String,
     *       modelClass: Class ]
     * 
     * Here is an example of rendering a single model (e.g., from a 'show' action). Note that
     *     def renderer = registry.get( "application/vnd.sungardhe.student.v0.01+xml" )?.singleRenderer
     *     if (renderer) {
     *         // Note that we pass both the MIME type (in case this renderer was implemented to support multiple representations) as
     *         // well as the 'renderMap' that would have been renderered using the default rendering. See RestfulControllerMixin for
     *         // details regarding the 'renderMap' convention.
     *         renderer?.call( "application/vnd.sungardhe.student.v0.01+xml", myRenderMap )
     *     } else {
     *         // use another renderer or try default rendering
     *     }
     * @param key the key used to register the handlers, preferably a MIME type
     * @param modelClass the class of the model, as the key (MIME type) may not be model-specific but a general indicator of 'api version' across many models
     * @return a map containing a paramsParser, singleRenderer, and listRenderer if available
     */
    ResourceRepresentationHandler get( String key, Class modelClass ) {
        def supportForKey = representationHandlerMap?.get( key )
        if (supportForKey) {
            def modelSupport = supportForKey.get( modelClass.name )
            if (!modelSupport) {
                modelSupport = supportForKey.get( modelClass.simpleName )                
            }
            if (modelSupport) {
                log.debug "Found support for model $modelClass.simpleName and MIME type $key:  $modelSupport"
                if (modelSupport instanceof Map) {
                    def handler = [
                            paramsExtractor:     modelSupport.paramsExtractor,
                            singleBuilder:       modelSupport.singleBuilder,
                            collectionBuilder:   modelSupport.collectionBuilder,
                            representationName:  key,
                            modelClass:          modelClass
                        ]
                    return new ResourceRepresentationHandlerImpl( handler )
                }
                else if (modelSupport instanceof String) {
                    log.debug "Will load a handler named $modelSupport from the class loader..."
                    return handlerNamed( modelClass, modelSupport )
                }
                return null
            }
        }
        log.debug "No custom representation support found for model ${modelClass.simpleName} and MIME type $key:"
        null
    }


    // TODO: Cache the handler for later use, so we don't keep loading the class and instantiating a new object each time...
    ResourceRepresentationHandler handlerNamed( Class modelClass, String name ) {
        try {
            Class handler = AH.getApplication().getClassForName( name )
            handler.newInstance()
        } catch (e) {
            log.error "Could not create a RepresentationHandler named $name due to exception $e"
            // user's should see a generic 'Ooops, a server error occurred' type message...
            throw new ApplicationException( modelClass, "@@r1:unknown.banner.api.exception" )
        }
    }

}


// We need an explicit class versus using groovy ability to 'implement' an interface using a map or closure,
// as our methods need to return closures that implement other interfaces.  This does not seem to work, so
// we'll use this concrete implementation instead.
class ResourceRepresentationHandlerImpl implements ResourceRepresentationHandler  {

    private Closure paramsExtractor
    private Closure singleBuilder
    private Closure collectionBuilder
    private String  representationName
    private Class   modelClass

    public String getRepresentationName() {
        representationName
    }


    public Class getModelClass() {
        modelClass
    }


    public ParamsExtractor paramsExtractor() {
        paramsExtractor as ParamsExtractor
    }


    public RepresentationBuilder singleBuilder() {
        singleBuilder as RepresentationBuilder
    }


    public RepresentationBuilder collectionBuilder() {
        collectionBuilder as RepresentationBuilder
    }
}

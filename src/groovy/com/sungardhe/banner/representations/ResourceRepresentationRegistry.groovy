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

/**
 * A registry that contains 'handlers' that support specific representations of resources.
 * When available, a MIME type should be used as the key to the map of handlers supporting
 * a specific representation.
 */
class ResourceRepresentationRegistry {

    private final Logger log = Logger.getLogger( getClass() )

    def registryMap // injected by Spring (and subsequently added to during init()), this map contains representation support
                    // The key must be a MIME type and the value must be a Map, which in turn has a key of the fully qualified
                    // class name of the model (or other business object that requires support).  Since a MIME type 'may' support
                    // multiple models, a second map is needed to resolve specific support for a model.
                    // e.g., [ "application/vnd.sungardhe.student.v0.01+xml": [ "com.sungardhe.banner.testing.Foo": support ]
                    // where support may be either:
                    //     a) a Map with 'paramsParser', 'singleRenderer', and 'listRenderer' keys, with in-line Closures as the values
                    //     b) a string that resolves a Class, which when instantiated will expose the same 'keys' as if it were the map discussed above


    public void init() {
        println "ResourceRepresentationRegistry.init() will initialize the internal registryMap of representation parsers and renderers"
        // TODO: Query the database for additional representation support (i.e., that was registered in the database versus spring configuration)
    }


    /**
     * Returns a Map or object that contains the following handlers if registered.  Keys will only be populated when an appropriate value
     * needs to be returned.  Note that the return value may be either a Map or an object, but the programming model is the same.
     *     [ paramsParser: myParamsParserClosure,
     *       singleRenderer: mySingleRendererClosure,
     *       listRenderer: myListRendererClosure,
     *       mimeType: String,
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
    def get( String key, String modelClassSimpleName ) {
        assert registryMap != null
        def supportForKey = registryMap.get( key )
        if (supportForKey) {
            def modelSupport = supportForKey.get( modelClassSimpleName )
            if (modelSupport) {
                log.debug "Found support for model $modelClassSimpleName and MIME type $key:  $modelSupport"
                if (modelSupport instanceof Map) {
                    modelSupport.put("mimeType", key)
                    modelSupport.put("modelClassSimpleName", modelClassSimpleName)
                }
                else if (modelSupport instanceof String) {
                    log.warn "Have support but it's a String and I'm not yet implemented to support that!"
                    return null // TODO: Instantiate class and return closure
                }
                return modelSupport
            }
        }
        log.debug "No custom representation support found for model ${modelClassSimpleName} and MIME type $key:"
        null
    }


}

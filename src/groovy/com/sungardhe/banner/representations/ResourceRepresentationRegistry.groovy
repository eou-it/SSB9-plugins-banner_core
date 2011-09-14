/*********************************************************************************
 Copyright 2009-2011 SunGard Higher Education. All Rights Reserved.
 This copyrighted software contains confidential and proprietary information of 
 SunGard Higher Education and its subsidiaries. Any use of this software is limited 
 solely to SunGard Higher Education licensees, and is further subject to the terms 
 and conditions of one or more written license agreements between SunGard Higher 
 Education and the licensee in question. SunGard is either a registered trademark or
 trademark of SunGard Data Systems in the U.S.A. and/or other regions and/or countries.
 Banner and Luminis are either registered trademarks or trademarks of SunGard Higher 
 Education in the U.S.A. and/or other regions and/or countries.
 **********************************************************************************/
package com.sungardhe.banner.representations

import org.apache.log4j.Logger
import com.sungardhe.banner.configuration.ConfigurationUtils
import org.codehaus.groovy.grails.commons.ApplicationHolder as AH
import com.sungardhe.banner.exceptions.ApplicationException

/**
 * A registry that contains 'handlers' that support specific representations of resources.
 * When available, a MIME type should be used as the key to the map of handlers supporting
 * a specific representation.
 *
 * Resource representation support may be configured within the bannerRepresentationHandlerMap map
 * within Config.groovy (for SunGardHE 'standard' representations) and within a customRepresentationHandlerMap
 * map contained within a specified configuration file. Please see Config.groovy with respect
 * to support of externalized configuration.
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
    //     b) a string that is the fully qualified class name of a Class that implements the ResourceRepresentationHandler interface
    def representationHandlerMap = [:]


    public void init() {
        def bannerRepresentationHandlerMap = ConfigurationUtils.getConfiguration().bannerRepresentationHandlerMap
        if (bannerRepresentationHandlerMap) {
            representationHandlerMap << bannerRepresentationHandlerMap
        } else {
            log.error "No externalized Banner resource representations are specified."
        }

        def customRepresentationHandlerMap = ConfigurationUtils.getConfiguration().customRepresentationHandlerMap
        if (customRepresentationHandlerMap) {
            representationHandlerMap << customRepresentationHandlerMap
        } else {
            log.warn "No custom resource representations are specified."
        }
        log.info  "ResourceRepresentationRegistry initialization is complete."
    }


    /**
     * Returns ResourceRepresentationHandler that can provide a ParamsExtractor and a RepresentationBuilder.
     * @param key the key used to register the handlers, preferably a MIME type
     * @param modelClass the class of the model, as the key (MIME type) may not be model-specific but a general indicator of 'api version' across many models
     * @return ResourceRepresentationHandler a handler that is able to provide a ParamsExtractor and a RepresentationBuilder
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
            Class handler = AH.getApplication().classLoader.loadClass( name )
            handler.newInstance() as ResourceRepresentationHandler
        } catch (e) {
            log.error "Could not create a RepresentationHandler named $name due to exception $e"
            // user's should see a generic 'Ooops, a server error occurred' type message...
            throw new ApplicationException( modelClass, "@@r1:unknown.banner.api.exception@@" )
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

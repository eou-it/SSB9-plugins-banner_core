/*******************************************************************************
Copyright 2009-2018 Ellucian Company L.P. and its affiliates.
*******************************************************************************/
package net.hedtech.banner.json

import groovy.util.logging.Slf4j
import org.grails.web.json.JSONObject
import grails.converters.JSON


/**
 * A helper class that assists in using JSON representations of models.
 **/
@Slf4j
class JsonHelper {


    /**
     * Provides a minimal workaround for Jira Grails-5585 -- once the JSON converter is 'fixed', this should be removed.
     * Note that the supplied JSON should be for a single model (so if you have a collection, iterate over it calling this
     * helper method for each.  See 'CollegeControllerIntegrationTests as an example.
     * Since this is such a trivial implementation, you may find it not sufficient when dealing with complex JSON.
     * In such cases, you may need to handle the JSONObject.NULL issue explicitly, add another helper method here,
     * or refine this method to be more flexible.
     **/
    public static def replaceJSONObjectNULL( json ) {
        try {
            for (entry in json) {
                log.debug "JsonHelper.replaceJSONObjectNULL will, if needed, replace JSONObject.NULL instance with null for $entry"
                if (entry.getValue() == JSON.parse('{ "a": null }').a) entry.setValue( null )
            }
        } catch (e) {
            log.error "JsonHelper.replaceJSONObjectNULL caught (and will re-throw) unexpected exception $e", e
            throw e
        }
    }

}

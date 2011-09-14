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
package com.sungardhe.banner.json

import grails.converters.JSON

import org.apache.log4j.Logger


/**
 * A helper class that assists in using JSON representations of models.  
 **/
class JsonHelper {
    
    static def log = Logger.getLogger( JsonHelper.name )
    
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
                if (entry.getValue() == org.codehaus.groovy.grails.web.json.JSONObject.NULL) entry.setValue( null )
            }
        } catch (e) {
            log.error "JsonHelper.replaceJSONObjectNULL caught (and will re-throw) unexpected exception $e", e
            throw e
        } 
    }
    
}

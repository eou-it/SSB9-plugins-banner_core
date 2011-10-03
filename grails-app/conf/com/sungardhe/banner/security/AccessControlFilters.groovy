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
package com.sungardhe.banner.security

import org.apache.commons.logging.LogFactory

import org.springframework.web.context.request.RequestContextHolder as RCH


/**
 * A grails filter used to establish a 'Form Context'.  
 * A FormContext is used to represent the Banner classic Oracle Form for which 
 * a request corresponds. By establishing a relationship between pages and 
 * Banner classic Oracle Forms, we can continue to leverage existing 
 * Banner security configuration.  Note this is only effective for URLs mapped to 
 * Controllers -- not Composers.  The FormContext for the ZK user interface 
 * is set by the sghe zk plugin. 
 **/
class AccessControlFilters {

    def dlog = LogFactory.getLog( getClass() ) // workaround for logging issues when using grails injected log

    def filters = {

        /**
         * This filter sets a 'FormContext' (thread local) based upon which controller is being accessed.
         * It uses a configuration map that associates grails controllers to one or more Banner forms,
         * so that existing Banner Security roles may be employed when using this grails application.
         */
        setFormContext( controller:'*', action:'*' ) {

            def theUrl
                        
            before = {
                
                theUrl = RCH.currentRequestAttributes().request.forwardURI  // This shows the 'real' URL versus request.getRequestURI(), which shows the '.dispatch'
                if ("$theUrl" =~ /ssb/) {
                    dlog.debug "AccessControlFilters.setFormContext 'before filter' for URL $theUrl will set a FormContext to '[SELFSERVICE] (it was '${FormContext.get()}), controller=$controllerName and action=$actionName). "              
                    FormContext.set( [ "SELFSERVICE" ] )
                }
                else {
                    Map formControllerMap = grailsApplication.config.formControllerMap
                    def associatedFormsList = formControllerMap[ controllerName?.toLowerCase() ]
                    dlog.debug "AccessControlFilters.setFormContext 'before filter' for URL $theUrl will set a FormContext with ${associatedFormsList?.size()} forms. (controller=$controllerName and action=$actionName). "              
                    FormContext.set( associatedFormsList )
                }  
            }

            after = { }

            afterView = { }
        }
    }

}

/*******************************************************************************
Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
*******************************************************************************/ 
package net.hedtech.banner.security

import org.apache.commons.logging.LogFactory

import org.springframework.web.context.request.RequestContextHolder as RCH
import org.springframework.web.context.request.RequestContextHolder


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

                if (params?.mepCode){
                 RequestContextHolder.currentRequestAttributes()?.request?.session?.setAttribute("mep",params?.mepCode)
                }
                
                Map formControllerMap = grailsApplication.config.formControllerMap
                def associatedFormsList = formControllerMap[ controllerName?.toLowerCase() ]
                
                if (!associatedFormsList?.contains( "SELFSERVICE" )) {
                    theUrl = RCH.currentRequestAttributes().request.forwardURI  // This shows the 'real' URL versus request.getRequestURI(), which shows the '.dispatch'
                    if ("$theUrl" =~ /ssb/) {
                        associatedFormsList?.add( 0, "SELFSERVICE" )
                    }
                }
                
                dlog.debug "AccessControlFilters.setFormContext 'before filter' for URL $theUrl will set a FormContext with ${associatedFormsList?.size()} forms. (controller=$controllerName and action=$actionName). "              
                FormContext.set( associatedFormsList )
            }

            after = { }

            afterView = { }
        }
    }

}

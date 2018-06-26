/* ****************************************************************************
Copyright 2009-2013 Ellucian Company L.P. and its affiliates.
*******************************************************************************/
package net.hedtech.banner.security

import org.springframework.web.context.request.RequestContextHolder
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
class AccessControlInterceptor {
    //TODO check the precedence using constants
    int order = 50
    //def dlog = LogFactory.getLog( getClass() ) // workaround for logging issues when using grails injected log

    /**
     * Executed before a matched action
     *
     * @return Whether the action should continue and execute
     */

    AccessControlInterceptor() {
        match controller: '*', action: '*'
    }

    boolean before() {
        def theUrl
        if (params?.mepCode){
            RequestContextHolder.currentRequestAttributes()?.request?.session?.setAttribute("mep",params?.mepCode?.toUpperCase())
        }

        Map formControllerMap = grailsApplication.config.formControllerMap
        def associatedFormsList = formControllerMap[ controllerName?.toLowerCase() ]

        if (!associatedFormsList?.contains( "SELFSERVICE" )) {
            // Get the 'real' URL (versus request.getRequestURI() which shows the '.dispatch')
            theUrl = RCH.currentRequestAttributes().request.forwardURI
            if ("$theUrl" =~ /ssb/) {
                associatedFormsList?.add( 0, "SELFSERVICE" )
            }
        }

        log.debug "AccessControlInterceptorInteceptor.setFormContext 'before filter' for URL $theUrl will set a FormContext with ${associatedFormsList?.size()} forms. (controller=$controllerName and action=$actionName). "
        FormContext.set( associatedFormsList )
    }



/** * Executed after the action executes but prior to view rendering
 *
 * @return True if view rendering should continue, false otherwise
 */
    boolean after() { true }

/**
 * Executed after view rendering completes
 */
    void afterView() {}
}

/** *****************************************************************************
 © 2011 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
package com.sungardhe.banner.security

import org.springframework.web.context.request.RequestContextHolder as RCH


/**
 * A grails filter used to establish a 'Form Context'.  
 * A FormContext is used to represent the Banner classic Oracle Form for which 
 * a request corresponds. By establishing a relationship between pages and 
 * Banner classic Oracle Forms, we can continue to leverage existing 
 * Banner security configuration. 
 **/
class AccessControlFilters {


    def filters = {

        /**
         * This filter sets a 'FormContext' (thread local) based upon which controller is being accessed.
         * It uses a configuration map that associates grails controllers to one or more Banner forms,
         * so that existing Banner Security roles may be employed when using this grails application.
         */
        setFormContext( controller:'*', action:'*' ) {

            before = {
                
                if (RCH.currentRequestAttributes().request.getRequestURI() ==~ /ssb/) {
                    FormContext.set( [ "SELFSERVICE" ] )
                }
                else {
                    Map formControllerMap = grailsApplication.config.formControllerMap
                    def associatedFormsList = formControllerMap[ controllerName?.toLowerCase() ]

                    FormContext.set( associatedFormsList )
                    log.debug "The AccessControl 'setFormContext' before filter has set a form context for controller $controllerName of: $associatedFormsList"
                }                
            }

            after = {
                FormContext.clear()
                log.debug "The AccessControl 'setFormContext' after filter has cleared the form context"
            }

            afterView = { }
        }
    }

}

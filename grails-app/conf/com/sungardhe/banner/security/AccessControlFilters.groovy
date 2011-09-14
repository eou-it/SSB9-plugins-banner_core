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

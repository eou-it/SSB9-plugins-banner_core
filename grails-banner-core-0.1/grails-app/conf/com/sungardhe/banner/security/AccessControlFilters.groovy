/** *****************************************************************************

 © 2010 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
package com.sungardhe.banner.security

class AccessControlFilters {


    def filters = {


        /**
         * This filter sets a 'FormContext' (thread local) based upon which controller is being accessed.
         * It uses a configuration map that associates grails controllers to one or more Banner forms,
         * so that existing Banner Security roles may be employed when using this grails application.
         */
        setFormContext( controller:'*', action:'*' ) {

            before = {
                Map formControllerMap = grailsApplication.config.formControllerMap
                def associatedFormsList = formControllerMap[ controllerName?.toLowerCase() ]

                FormContext.set( associatedFormsList )
                log.debug "The AccessControl 'setFormContext' before filter has set a form context for controller $controllerName of: $associatedFormsList"
            }

            after = {
                FormContext.clear()
                log.debug "The AccessControl 'setFormContext' after filter has cleared the form context"
            }

            afterView = {

            }
        }
    }

}

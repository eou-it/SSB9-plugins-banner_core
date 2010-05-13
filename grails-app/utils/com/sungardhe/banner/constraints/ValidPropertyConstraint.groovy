/** *****************************************************************************
 © 2010 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
package com.sungardhe.banner.constraints

/**
 * This constraint will allow us to validate a property on a domain class by calling back
 * on a finder and ensuring that upon validation that the property is valid.
 *
 * This should only be used when a foreign key constraint does not exist.
 */
class ValidPropertyConstraint {

    static name = "validProperty"
    static expectsParams = true

    def validate = { val, target ->

        if (!val) {
            return
        }

        String property = "code"
        String method = "findByCode"

        if ((params instanceof Boolean)) {
            // The default method is "findByCode".  If the params are true, this is what is used.  Params can be false
            // and in that case we are just not going to do any validation.

            if (Boolean.valueOf( params ) == false) {
                return
            }
        } else {
            // We assume we are using a finder method and that the params is a string representing the property
            // we are searching for off the domain.
            method = "findBy${params[0].toUpperCase()}${params[1..-1]}"
            property = params
        }

        return val.metaClass.theClass?."$method"( val?."$property" ) != null
    }
}
/** *****************************************************************************
 © 2010 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
package com.sungardhe.banner.controllers

/**
 * Utilities for controllers.
 */
class ControllerUtils {


    public static def keyblock = { controller ->
        if (controller.request[ "keyblock" ] == null) {
            controller.request[ "keyblock" ] = ParamsUtils.namedParams( controller.params, "keyblock." )
        }
        return controller.request[ "keyblock" ]
    }


    public static def buildModel = { keyblock, blocks ->
        def model = [ keyblock: keyblock ]

        if (blocks) {
            blocks.each { block ->
                block.each {
                    model.put( it.key, it.value )
                }
            }
        }
        return model
    }

}

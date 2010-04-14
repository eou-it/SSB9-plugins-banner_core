package com.sungardhe.banner.controllers

/**
 * Created by IntelliJ IDEA.
 * User: rrullo
 * Date: Mar 14, 2010
 * Time: 9:27:34 AM
 * To change this template use File | Settings | File Templates.
 */
class ControllerUtils {

    public static def keyblock = { controller ->
        if (controller.request[ "keyblock" ] == null) {
            controller.request[ "keyblock" ] = ParamsUtils.namedParams( controller.params, "keyblock." )
        }

        return controller.request[ "keyblock" ]
    }
    

    public static def buildModel = { keyblock, blocks ->
        def model = [keyblock: keyblock]

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

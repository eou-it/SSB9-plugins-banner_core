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
package com.sungardhe.banner.controllers

import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils
import org.springframework.web.context.request.RequestContextHolder

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



    public static def buildLogoutRedirectURI() {
        def uri = SpringSecurityUtils.securityConfig.logout.filterProcessesUrl //'/j_spring_security_logout'

        def mep = RequestContextHolder?.currentRequestAttributes()?.request?.session?.getAttribute("mep")
        if (mep) {
            uri += "?spring-security-redirect=?mepCode=${mep}"
        }

        uri
    }
}

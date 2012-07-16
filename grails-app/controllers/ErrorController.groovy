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

import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils
import org.springframework.web.context.request.RequestContextHolder
import net.hedtech.banner.controllers.ControllerUtils

class ErrorController {

    static defaultAction = "internalServerError"
    
    def internalServerError = {
        def model = [
            exception: request.exception,
            request:   request
        ]

        render view: "error", model: model
    }

    def accessForbidden = {
        def uri = ControllerUtils.buildLogoutRedirectURI()

        render view: "forbidden", model: [uri: uri]
    }
}

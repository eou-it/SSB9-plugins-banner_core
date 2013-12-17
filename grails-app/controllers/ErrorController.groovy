/*******************************************************************************
Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
*******************************************************************************/

import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils
import org.springframework.web.context.request.RequestContextHolder
import net.hedtech.banner.controllers.ControllerUtils

class ErrorController {

    static defaultAction = "internalServerError"

    def internalServerError = {
        def model = [
            exception: request.exception,
            request:   request,
            returnHomeLinkAddress : SpringSecurityUtils.securityConfig.successHandler.defaultTargetUrl
        ]

        render view: "error", model: model
    }

    def pageNotFoundError = {
        def model = [
                exception: request.exception,
                request:   request,
                returnHomeLinkAddress : SpringSecurityUtils.securityConfig.successHandler.defaultTargetUrl
        ]

        render view: "pageNotFound", model: model
    }


    def accessForbidden = {
        def uri = ControllerUtils.buildLogoutRedirectURI()

        render view: "forbidden", model: [uri: uri]
    }
}

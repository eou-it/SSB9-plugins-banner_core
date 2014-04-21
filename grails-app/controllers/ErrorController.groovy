/*******************************************************************************
Copyright 2009-2014 Ellucian Company L.P. and its affiliates.
*******************************************************************************/

import net.hedtech.banner.controllers.ControllerUtils
import net.hedtech.banner.exceptions.MepCodeNotFoundException
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils

class ErrorController {

    static defaultAction = "internalServerError"
    public String returnHomeLinkAddress = SpringSecurityUtils.securityConfig.successHandler.defaultTargetUrl
    public String VIEW_LOGOUT_PAGE = SpringSecurityUtils.securityConfig.logout.mepErrorLogoutUrl

    public static final String VIEW_ERROR_PAGE = "error"
    public static final String VIEW_PAGE_NOT_FOUND = "pageNotFound"
    public static final String VIEW_FORBIDDEN = "forbidden"


    def internalServerError = {
        def exception = request.exception
        if (exception.cause instanceof MepCodeNotFoundException) {
            returnHomeLinkAddress = VIEW_LOGOUT_PAGE
        }
        def model = [
            exception: exception,
            request:   request,
            returnHomeLinkAddress : returnHomeLinkAddress
        ]

        render view: VIEW_ERROR_PAGE, model: model
    }

    def pageNotFoundError = {
        def model = [
                exception: request.exception,
                request:   request,
                returnHomeLinkAddress : returnHomeLinkAddress
        ]

        render view: VIEW_PAGE_NOT_FOUND, model: model
    }


    def accessForbidden = {
        def uri = ControllerUtils.buildLogoutRedirectURI()

        render view: VIEW_FORBIDDEN, model: [uri: uri]
    }
}

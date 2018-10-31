/*******************************************************************************
 Copyright 2009-2018 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/


import grails.util.Environment
import net.hedtech.banner.controllers.ControllerUtils
import net.hedtech.banner.exceptions.MepCodeNotFoundException
import grails.plugin.springsecurity.SpringSecurityUtils

import org.springframework.security.core.context.SecurityContextHolder as SCH
import org.springframework.security.web.authentication.logout.LogoutHandler
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler

class ErrorController {

    static defaultAction = "internalServerError"
    public String returnHomeLinkAddress = SpringSecurityUtils.securityConfig.successHandler.defaultTargetUrl
    public String VIEW_LOGOUT_PAGE = SpringSecurityUtils.securityConfig.logout.mepErrorLogoutUrl

    public static final String VIEW_ERROR_PAGE = "error"
    public static final String VIEW_PAGE_NOT_FOUND = "pageNotFound"
    public static final String VIEW_FORBIDDEN = "forbidden"
    def logoutHandlers

    def internalServerError () {
        def exception = request.exception
        def targetException
        def mepCodeException

        if (Environment.current == Environment.PRODUCTION || Environment.current == Environment.TEST) {
            targetException = exception
            mepCodeException = exception?.cause
        } else if (Environment.current == Environment.DEVELOPMENT) {
            targetException=exception?.cause?.target
            mepCodeException = exception?.cause?.target
        }
        if (mepCodeException instanceof MepCodeNotFoundException) {
            returnHomeLinkAddress = VIEW_LOGOUT_PAGE
        }
        //SCH.context?.authentication is passed and logout is fired on the logout handlers registered
        logoutHandlers.each { handler ->
            if(handler instanceof LogoutHandler) {
                handler.logout(request, response, SCH.context?.authentication)
            } else if (handler instanceof LogoutSuccessHandler) {
                handler.onLogoutSuccess(request, response, SCH.context?.authentication)
            }
        }
        def model = [
                exception: targetException,
                returnHomeLinkAddress : returnHomeLinkAddress
        ]

        render view: VIEW_ERROR_PAGE, model: model
    }

    def pageNotFoundError() {
        def model = [
                exception: request.exception,
                request:   request,
                returnHomeLinkAddress : returnHomeLinkAddress
        ]

        render view: VIEW_PAGE_NOT_FOUND, model: model
    }


    def accessForbidden(){
        def uri = ControllerUtils.buildLogoutRedirectURI()

        render view: VIEW_FORBIDDEN, model: [uri: uri]
    }
}
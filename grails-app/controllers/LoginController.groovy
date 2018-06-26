/*******************************************************************************
 Copyright 2009-2018 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

import grails.converters.JSON
import net.hedtech.banner.controllers.ControllerUtils
import net.hedtech.banner.exceptions.AuthorizationException
import grails.plugin.springsecurity.SpringSecurityUtils
import org.springframework.security.authentication.*
import org.springframework.security.core.context.SecurityContextHolder as SCH
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.context.request.RequestContextHolder

import org.springframework.security.web.WebAttributes

class LoginController {

    /**
     * Dependency injection for the authenticationTrustResolver.
     */
    def authenticationTrustResolver

    /**
     * Dependency injection for the springSecurityService.
     */
    def springSecurityService

    /**
     * Default action; redirects to 'defaultTargetUrl' if logged in, /login/auth otherwise.
     */
    def index = {
        if (springSecurityService.isLoggedIn()) {
            redirect uri: SpringSecurityUtils.securityConfig.successHandler.defaultTargetUrl
        }
        else {
            redirect action: auth, params: params
        }
    }

    /**
     * Show the login page.
     */
    def auth = {

        def config = SpringSecurityUtils.securityConfig
        String forgotPasswordUrl =  "${request.contextPath}/login/resetPassword"
        if (springSecurityService.isLoggedIn()) {
            redirect uri: config.successHandler.defaultTargetUrl
            return
        }

        String view = 'auth'
        String postUrl = "${request.contextPath}${config.apf.filterProcessesUrl}"

        /*
        render view: view, plugin: "bannerCore", model: [postUrl: postUrl, forgotPasswordUrl: forgotPasswordUrl,
                rememberMeParameter: config.rememberMe.parameter]
        */

        render view: view,  model: [postUrl: postUrl, forgotPasswordUrl: forgotPasswordUrl,
                                    rememberMeParameter: config.rememberMe.parameter]


    }

    /**
     * Called when making ajax request and being redirected to authenticate.  We are informing the client that the user is not authenticated
     * and sending a request for a login page by sending a response header down under the key 'X-Login-Page'.
     */
    def authAjax = {

        def config = SpringSecurityUtils.securityConfig

        if (springSecurityService.isLoggedIn()) {
            redirect uri: config.successHandler.defaultTargetUrl
            return
        }

        // Add a custom response header.
        response.setHeader( "X-Login-Page", true.toString() )

        render "userNotLoggedIn"
    }


    /**
     * Show denied page.
     */
    def denied = {
        if (springSecurityService.isLoggedIn() &&
                authenticationTrustResolver.isRememberMe(SCH.context?.authentication)) {
            // have cookie but the page is guarded with IS_AUTHENTICATED_FULLY
            redirect action: full, params: params
        }

        render view: "denied", plugin: "bannerCore", model: [uri: ControllerUtils.buildLogoutRedirectURI()]
    }

    /**
     * Login page for users with a remember-me cookie but accessing a IS_AUTHENTICATED_FULLY page.
     */
    def full = {
        def config = SpringSecurityUtils.securityConfig
        render view: 'auth', plugin: "bannerCore", params: params,
                model: [hasCookie: authenticationTrustResolver.isRememberMe(SCH.context?.authentication),
                        postUrl: "${request.contextPath}${config.apf.filterProcessesUrl}"]
    }

    /**
     * Callback after a failed login. forwards to the auth page with a warning message.
     */
    def authfail = {

        //def username = session[UsernamePasswordAuthenticationFilter.SPRING_SECURITY_LAST_USERNAME_KEY]
        def username = session[UsernamePasswordAuthenticationFilter.SPRING_SECURITY_FORM_USERNAME_KEY]

        //def exception = session[AbstractAuthenticationProcessingFilter.SPRING_SECURITY_LAST_EXCEPTION_KEY]
        def exception = session[WebAttributes.AUTHENTICATION_EXCEPTION]

        if(exception instanceof CredentialsExpiredException){
            forward controller : "resetPassword", action: "changePassword", params : params
        }
        else {
            String msg = getMessageFor(exception)

            if (springSecurityService.isAjax(request)) {
                render([error: msg] as JSON)
            } else {
                flash.message = msg
                forward action: "auth", params: params
            }
        }
    }


    /**
     * Returns a localized message appropriate for the supplied exception.
     **/
    private def getMessageFor( Throwable exception ) {
        def msg = ''
        if (exception) {
            if (exception instanceof AccountExpiredException) {
                msg = SpringSecurityUtils.securityConfig.errors.login.expired
            }
            else if (exception instanceof CredentialsExpiredException) {
                msg = message( code:"net.hedtech.banner.errors.login.expired" )
            }
            else if (exception instanceof DisabledException) {
                msg = message( code:"net.hedtech.banner.errors.login.disabled" )
            }
            else if (exception instanceof LockedException) {
                msg = message( code:"net.hedtech.banner.errors.login.locked" )
            }
            else if(exception instanceof AuthorizationException) {
                msg = message( code: "net.hedtech.banner.access.denied.message" )
            }
            else if (exception instanceof BadCredentialsException) {
                msg = message( code:"net.hedtech.banner.errors.login.fail" )
            }
            else if (exception instanceof UsernameNotFoundException) {
                msg = message( code: "net.hedtech.banner.errors.login.credentialnotfound" )
            }
            else {
                msg = message( code:"net.hedtech.banner.errors.login.fail" )
            }
        }
        msg
    }


    /**
     * The Ajax success redirect url.
     */
    def ajaxSuccess = {
        render([success: true, username: springSecurityService.authentication.name] as JSON)
    }


    /**
     * The Ajax denied redirect url.
     */
    def ajaxDenied = {
        render([error: 'access denied'] as JSON)
    }

    /**
     * When user clicks on forgot password URL.
     */
    def forgotpassword ={
        def config = SpringSecurityUtils.securityConfig

        String userName = request.getParameter("j_username")
        if(userName == null || userName.trim().length() == 0){
            flash.message =  message( code: "net.hedtech.banner.resetpassword.username.required.error")
            String view = 'auth'
            String postUrl = "${request.contextPath}${config.apf.filterProcessesUrl}"
            String forgotPasswordUrl =  "${request.contextPath}/login/resetPassword";
            render view: view, plugin: "bannerCore", model: [postUrl: postUrl, forgotPasswordUrl: forgotPasswordUrl, userNameRequired: true,
                                                             rememberMeParameter: config.rememberMe.parameter]
        }
        else{
            session.setAttribute("requestPage", "questans")
            forward controller : "resetPassword", action: "questans", params : params
        }
    }

    def error = {
        def exception = session[AbstractAuthenticationProcessingFilter.SPRING_SECURITY_LAST_EXCEPTION_KEY]
        render view: "customerror", plugin: "bannerCore", model: [msg: getMessageFor( exception ), uri: buildLogout()]
    }

    private def buildLogout() {
        def uri = '/logout/customLogout?error=true'
        def mep = RequestContextHolder?.currentRequestAttributes()?.request?.session?.getAttribute("mep")
        if (mep) {
            uri += "&mepCode=${mep}"
        }
        uri
    }
}
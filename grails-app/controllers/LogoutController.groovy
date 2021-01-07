/*******************************************************************************
 Copyright 2009-2021 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

import grails.util.Holders
import net.hedtech.banner.controllers.ControllerUtils
import net.hedtech.banner.general.audit.LoginAuditService
import net.hedtech.banner.security.AuthenticationProviderUtility
import net.hedtech.banner.security.BannerUser
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import net.hedtech.banner.exceptions.MepCodeNotFoundException
import javax.servlet.http.Cookie

/**
 * Controller called to prepare logouts.
 */
class LogoutController {

    static scope = "singleton"
    static defaultAction = "index"
    public static final String VIEW_LOGOUT_PAGE = "logoutPage"
    public static final String VIEW_TIMEOUT = "timeout"
    public static final String VIEW_CUSTOM_LOGOUT = "customLogout"
    public static final String HTTP_REQUEST_REFERER_STRING = "referer"
    public static final String LOGIN_AUTH_ACTION_URI = "login/auth"
    public static final String LOGIN_CONTROLLER = "login"
    public static final String ACTION_TIMEOUT_PAGE = 'timeoutPage'
    public static final String JSESSIONID_COOKIE_NAME = "JSESSIONID"
    private boolean GUEST_USER = false
    def multiEntityProcessingService
    /*
     * Index action. Redirects to the Spring security logout uri.
     */

    def index() {
        AuthenticationProviderUtility.captureLogoutInformation(response?.authBeforeExecution?.user?.username, response?.authBeforeExecution?.user?.pidm)
        boolean isGuestUser = response?.authBeforeExecution?.user?.gidm && !response?.authBeforeExecution?.user?.pidm
        if (!ControllerUtils.isSamlEnabled()) {
            invalidateSession(response)
        }
        if (isGuestUser && (ControllerUtils.isCasEnabled())) {
            GUEST_USER = true
            redirect(uri: '/logout/customLogout')
        } else {
            GUEST_USER = false
            redirect uri: ControllerUtils.buildLogoutRedirectURI()
        }
    }

    def timeout() {
        log.debug("Timeout is invoked for = {}", SecurityContextHolder.context?.authentication?.principal)
        if (request?.getHeader(HTTP_REQUEST_REFERER_STRING)?.endsWith(LOGIN_AUTH_ACTION_URI)) {
            forward(controller: LOGIN_CONTROLLER)
        } else {
            def mepCode = session.mep
            def uri = createLink([uri: '/ssb/logout/timeoutPage', action: ACTION_TIMEOUT_PAGE, absolute: true])
            String username = 'ANONYMOUS'
            String pidm = null
            def authentication = response?.authBeforeExecution
            if (authentication && authentication.hasProperty('user') && authentication.user instanceof BannerUser) {
                username = authentication.user.username
                pidm = authentication.user.pidm
            }
            AuthenticationProviderUtility.captureLogoutInformation(username, pidm)
            invalidateSession(response)
            ControllerUtils.clearUserContext()
            redirect uri: uri, params: mepCode ? [mepCode: mepCode] : []
        }
    }

    def timeoutPage() {
        def mep = params.mep
        render view: VIEW_TIMEOUT, model: [uri: ControllerUtils.buildLogoutRedirectURI(), mepCode: mep]
    }

    def logoutPage() {
        render view: VIEW_LOGOUT_PAGE
    }

    private void invalidateSession(response) {
        session.invalidate()
        Cookie cookie = new Cookie(JSESSIONID_COOKIE_NAME, null)
        cookie.setPath(request.getContextPath())
        cookie.setMaxAge(0)
        response.addCookie(cookie)
    }

    def customLogout() {
        boolean show = true
        if (request.getParameter("error") || ControllerUtils.isCasEnabled()) {
            show = false
        }
        if (ControllerUtils.isCasEnabled() && !GUEST_USER) {
            render view: VIEW_CUSTOM_LOGOUT, model: [logoutUri: ControllerUtils.getAfterLogoutRedirectURI(), uri: ControllerUtils.getHomePageURL(), show: show]
        } else if (ControllerUtils.isCasEnabled() && GUEST_USER) {
            render view: VIEW_CUSTOM_LOGOUT, model: [uri: ControllerUtils.getHomePageURL(), show: true]
        } else {
            render view: VIEW_CUSTOM_LOGOUT, model: [uri: ControllerUtils.getHomePageURL(), show: show]
        }
    }


    def redirect() {
        String newMepCode = request.getParameter('mepCode')
        String newApplicationURL = request.getRequestURL()
        if (isValidateMepCode(newMepCode)) {
            newApplicationURL = (request.getRequestURL() - request.getServletPath()) + "?mepCode=${newMepCode}"
        }
        log.debug("Redirect is invoked for = {}", newApplicationURL)
        session.invalidate()
        redirect(url: "${newApplicationURL}")
    }


    private boolean isValidateMepCode(String newMepCode) {
        String mepDescription = multiEntityProcessingService?.getMepDescription(newMepCode)
        boolean isValidateMepCode = false
        if(mepDescription){
            isValidateMepCode = true
        } else {
            log.error("Redirect is invoked with invalid MepCode= {}", newMepCode)
            isValidateMepCode = false
        }
        return isValidateMepCode
    }
}

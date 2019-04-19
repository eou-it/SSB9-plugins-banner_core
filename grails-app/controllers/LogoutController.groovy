/*******************************************************************************
 Copyright 2009-2018 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

import grails.util.Holders
import net.hedtech.banner.controllers.ControllerUtils
import net.hedtech.banner.general.audit.LoginAuditService

import javax.servlet.http.Cookie

/**
 * Controller called to prepare logouts.
 */
class LogoutController {

    static defaultAction = "index"
    public static final String VIEW_LOGOUT_PAGE = "logoutPage"
    public static final String VIEW_TIMEOUT = "timeout"
    public static final String VIEW_CUSTOM_LOGOUT = "customLogout"
    public static final String HTTP_REQUEST_REFERER_STRING = "referer"
    public static final String LOGIN_AUTH_ACTION_URI = "login/auth"
    public static final String LOGIN_CONTROLLER = "login"
    public static final String ACTION_TIMEOUT_PAGE = 'timeoutPage'
    public static final String JSESSIONID_COOKIE_NAME = "JSESSIONID"




    /*
     * Index action. Redirects to the Spring security logout uri.
     */
    def index() {

        if (!ControllerUtils.isSamlEnabled()) {
            invalidateSession(response)
        }
        redirect uri: ControllerUtils.buildLogoutRedirectURI()
    }

    def timeout() {

        if (request?.getHeader(HTTP_REQUEST_REFERER_STRING)?.endsWith(LOGIN_AUTH_ACTION_URI)) {
            forward(controller: LOGIN_CONTROLLER)
        } else {
            def mepCode = session.mep
            def uri = createLink([uri: '/ssb/logout/timeoutPage', action: ACTION_TIMEOUT_PAGE, absolute: true])
            invalidateSession(response)
            redirect uri: uri, params: mepCode?[ mep: mepCode]:[]
        }
    }

    def timeoutPage() {
        def mep = params.mep
        render view: VIEW_TIMEOUT, model: [uri: ControllerUtils.buildLogoutRedirectURI(), mep: mep]
    }

    def logoutPage() {
        render view: VIEW_LOGOUT_PAGE
    }

    private void invalidateSession(response) {
        captureLogoutInformation(response)
        session.invalidate()
        Cookie cookie = new Cookie(JSESSIONID_COOKIE_NAME, null);
        cookie.setPath(request.getContextPath());
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    def customLogout() {
        boolean show = true
        if (request.getParameter("error") || ControllerUtils.isCasEnabled()) {
            show = false
        }

        if (ControllerUtils.isCasEnabled()) {
            render view: VIEW_CUSTOM_LOGOUT, model: [logoutUri: ControllerUtils.getAfterLogoutRedirectURI(), uri: ControllerUtils.getHomePageURL(), show: show]
        } else {
            render view: VIEW_CUSTOM_LOGOUT, model: [uri: ControllerUtils.getHomePageURL(), show: show]
        }

    }

    def captureLogoutInformation(response){
        def userInfo = response?.authBeforeExecution?.user
        def loginAuditService =  new LoginAuditService()
        /*user = BannerGrantedAuthorityService.getUser()*/
        if(userInfo!= null && Holders.config.EnableLoginAudit == "Y"){
            String logoutComment  = "Logout successful."
            loginAuditService.createLoginLogoutAudit(userInfo,logoutComment)
        }else{
            log.debug "User Information Not Present."
        }
    }
}

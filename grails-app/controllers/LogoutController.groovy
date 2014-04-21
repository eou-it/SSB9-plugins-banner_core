/*******************************************************************************
 Copyright 2009-2014 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
import net.hedtech.banner.controllers.ControllerUtils
import javax.servlet.http.Cookie

/**
 * Controller called to prepare logouts.
 */
class LogoutController {

    static defaultAction = "index"
    public static final String VIEW_LOGOUT_PAGE = "logoutPage"
    public static final String VIEW_TIMEOUT = "timeout"
    public static final String HTTP_REQUEST_REFERER_STRING = "referer"
    public static final String LOGIN_AUTH_ACTION_URI = "login/auth"
    public static final String LOGIN_CONTROLLER = "login"
    public static final String ACTION_TIMEOUT_PAGE = 'timeoutPage'
    public static final String JSESSIONID_COOKIE_NAME = "JSESSIONID"

    /**
     * Index action. Redirects to the Spring security logout uri.
     */
    def index = {
        invalidateSession( response )
        redirect uri: ControllerUtils.buildLogoutRedirectURI()
    }

    def timeout = {
        if(request?.getHeader(HTTP_REQUEST_REFERER_STRING)?.endsWith(LOGIN_AUTH_ACTION_URI)){
            forward(controller:LOGIN_CONTROLLER)
        } else {
            def uri = createLink([ action:ACTION_TIMEOUT_PAGE, absolute:true ])
            invalidateSession( response )
            redirect uri: uri
        }
    }

    def timeoutPage = {
        render view: VIEW_TIMEOUT, model: [uri: ControllerUtils.buildLogoutRedirectURI() ]
    }

    def logoutPage = {
        render view: VIEW_LOGOUT_PAGE
    }
    
    private void invalidateSession( response ) {
        session.invalidate()
        Cookie cookie = new Cookie(JSESSIONID_COOKIE_NAME, null);
        cookie.setPath(request.getContextPath());
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}

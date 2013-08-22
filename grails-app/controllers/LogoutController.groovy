import net.hedtech.banner.controllers.ControllerUtils
import javax.servlet.http.Cookie

/*******************************************************************************
Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
*******************************************************************************/ 

/**
 * Controller called to prepare logouts.
 */
class LogoutController {

    static defaultAction = "index"
    def httpSessionService

    /**
     * Index action. Redirects to the Spring security logout uri.
     */
    def index = {
       // httpSessionService.closeDBConnection()
        invalidateSession( response )
        redirect uri: ControllerUtils.buildLogoutRedirectURI()
    }

    def timeout = {
      //  httpSessionService.closeDBConnection()
        if(request?.getHeader("referer")?.endsWith("login/auth")){
            forward(controller:"login")
        } else {
            def uri = createLink([ action:'timeoutPage', absolute:true ])
            invalidateSession( response )
            redirect uri: uri
        }
    }

    def timeoutPage = {
        render view: "timeout", model: [uri: ControllerUtils.buildLogoutRedirectURI() ]
    }
    
    private void invalidateSession( response ) {
        session.invalidate()
        Cookie cookie = new Cookie("JSESSIONID", null);
        cookie.setPath(request.getContextPath());
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}

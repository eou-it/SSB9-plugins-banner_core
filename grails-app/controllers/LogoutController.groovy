import net.hedtech.banner.controllers.ControllerUtils
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler
import org.springframework.security.core.context.SecurityContextHolder
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
        httpSessionService.closeDBConnection()
        session.invalidate()
        Cookie cookie = new Cookie("JSESSIONID", null);
        cookie.setPath(request.getContextPath());
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        redirect uri: ControllerUtils.buildLogoutRedirectURI()
    }


    def timeout = {
        httpSessionService.closeDBConnection()
        if(request?.getHeader("referer")?.endsWith("login/auth")){
            forward(controller:"login")
        } else {
            def uri = ControllerUtils.buildLogoutRedirectURI()
            session.invalidate()
            Cookie cookie = new Cookie("JSESSIONID", null);
            cookie.setPath(request.getContextPath());
            cookie.setMaxAge(0);
            response.addCookie(cookie);
            render view: "timeout", model: [uri: uri]
        }
    }



}

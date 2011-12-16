import com.sungardhe.banner.controllers.ControllerUtils
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler
import org.springframework.security.core.context.SecurityContextHolder
import javax.servlet.http.Cookie

/** *******************************************************************************
 Copyright 2009-2011 SunGard Higher Education. All Rights Reserved.
 This copyrighted software contains confidential and proprietary information of 
 SunGard Higher Education and its subsidiaries. Any use of this software is limited 
 solely to SunGard Higher Education licensees, and is further subject to the terms 
 and conditions of one or more written license agreements between SunGard Higher 
 Education and the licensee in question. SunGard is either a registered trademark or
 trademark of SunGard Data Systems in the U.S.A. and/or other regions and/or countries.
 Banner and Luminis are either registered trademarks or trademarks of SunGard Higher 
 Education in the U.S.A. and/or other regions and/or countries.
 ********************************************************************************* */

/**
 * Controller called to prepare logouts.
 */
class LogoutController {

    static defaultAction = "index"

    /**
     * Index action. Redirects to the Spring security logout uri.
     */
    def index = {
        redirect uri: ControllerUtils.buildLogoutRedirectURI()
    }


    def timeout = {
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
/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.security

import grails.util.Holders
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler

import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * A utility class to handle Authentication failure.
 */

class BannerAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler{


    void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException, ServletException {

        super.onAuthenticationFailure(request, response, e);

        def msg = request.session.getAttribute("msg")
        def module = request.session.getAttribute("module")
        def authName = request.session.getAttribute("auth_name")

        Holders.getApplicationContext().publishEvent(new BannerAuthenticationEvent( authName, false, msg, module, new Date(), 1 ))
        request.session.removeAttribute("msg")
        request.session.removeAttribute("module")
        request.session.removeAttribute("auth_name")
    }
}
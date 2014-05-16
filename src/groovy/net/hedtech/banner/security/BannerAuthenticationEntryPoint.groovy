/* ******************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.security

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.codehaus.groovy.grails.plugins.springsecurity.AjaxAwareAuthenticationEntryPoint

public class BannerAuthenticationEntryPoint extends AjaxAwareAuthenticationEntryPoint {

    @Override
    protected String buildRedirectUrlToLoginPage(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) {

        if (request.getParameter("mepCode")){
            request?.getSession()?.setAttribute("mep",request.getParameter("mepCode").toUpperCase())
        }
        return super.buildRedirectUrlToLoginPage(request, response, authException) 
    }
}

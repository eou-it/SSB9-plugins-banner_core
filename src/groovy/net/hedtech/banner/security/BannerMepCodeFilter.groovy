/*******************************************************************************
Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
*******************************************************************************/ 
package net.hedtech.banner.security

import org.codehaus.groovy.grails.commons.ConfigurationHolder as CH

import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import org.springframework.web.filter.GenericFilterBean

class BannerMepCodeFilter extends GenericFilterBean {


    public void doFilter( ServletRequest request, ServletResponse response,
                          FilterChain chain ) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request
        if (req.getParameter("mepCode")){
            req?.getSession()?.setAttribute("mep",req.getParameter("mepCode").toUpperCase())
        }
        chain.doFilter( request, response );
    }
}

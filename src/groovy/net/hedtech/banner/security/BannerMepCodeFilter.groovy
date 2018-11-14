/*******************************************************************************
 Copyright 2009-2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.security

import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import org.springframework.web.filter.GenericFilterBean

class BannerMepCodeFilter extends GenericFilterBean {


    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request



        if (req.getParameter("mepCode")) {
            if (!mepCodeModified(req)) {
                req?.getSession()?.setAttribute("mep", req.getParameter("mepCode").toUpperCase())
            }
        }

        chain.doFilter(request, response);
    }

    public boolean mepCodeModified(HttpServletRequest request) {
        String sessionMepCode = request?.getSession()?.getAttribute("mep")
        String currentMepCode = request.getParameter("mepCode")
        if (sessionMepCode && currentMepCode) {
            if (sessionMepCode != currentMepCode) {
                request.setAttribute("mepcodeChanged", true)
                return true
            } else {
                request.setAttribute("mepcodeChanged", false)
                return true
            }
        } else {
            request.setAttribute("mepcodeChanged", false)
            false
        }
    }
}

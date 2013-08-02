package net.hedtech.banner.privacy

import org.springframework.context.ApplicationEventPublisherAware
import org.springframework.web.filter.GenericFilterBean

import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession
import org.codehaus.groovy.grails.commons.ConfigurationHolder

class PrivacyPolicyFilter extends GenericFilterBean {
    void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req
		HttpServletResponse response = (HttpServletResponse) res

		// Switch to grails.util.Holders in Grails 2.x
        response.addHeader("P3P", "CP=\"" + ConfigurationHolder?.config?.privacy?.codes + "\"")
        
        chain.doFilter(request, response)
     }
 }
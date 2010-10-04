/** *****************************************************************************

 © 2010 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */

package com.sungardhe.banner.security

import org.codehaus.groovy.grails.commons.ConfigurationHolder as CH

import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import org.apache.log4j.Logger
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter

class BannerPreAuthenticatedFilter extends AbstractPreAuthenticatedProcessingFilter {

    private final Logger log = Logger.getLogger(getClass())

    def dataSource // injected by Spring


    public void doFilter( ServletRequest request, ServletResponse response,
                         FilterChain chain ) throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request
        def assertAttributeValue = req.getHeader( CH?.config?.banner.sso.authenticationAssertionAttribute )

        if (assertAttributeValue) {
            if (!SecurityContextHolder.context?.authentication) {
                log.debug "assertAttributeValue is $assertAttributeValue"
                def dbUser = BannerAuthenticationProvider.getMappedDatabaseUserForUdcId( assertAttributeValue, dataSource )
                log.debug "dbUser for assertAttributeValue $assertAttributeValue is $dbUser"
                Collection<GrantedAuthority> authorities = BannerAuthenticationProvider.determineAuthorities( dbUser, dataSource )
                def user = new BannerUser( dbUser, dbUser,
                        true /*enabled*/, true /*accountNonExpired*/,
                        true /*credentialsNonExpired*/, true /*accountNonLocked*/, authorities)
                def token = new BannerAuthenticationToken( user )
                SecurityContextHolder.context.setAuthentication( token )
            }
        }
        chain.doFilter( request, response );
    }


    protected Object getPreAuthenticatedPrincipal( HttpServletRequest request ) {
       log.info "getPreAuthenticatedPrincipal method called"
    }

    protected Object getPreAuthenticatedCredentials( HttpServletRequest request ) {
       log.info "getPreAuthenticatedCredentials method called"
    }

}
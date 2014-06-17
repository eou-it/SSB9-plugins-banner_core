/*******************************************************************************
Copyright 2009-2014 Ellucian Company L.P. and its affiliates.
*******************************************************************************/ 
package net.hedtech.banner.security

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

                log.debug "BannerPreAuthenticatedFilter.doFilter invoked with request $request"
        HttpServletRequest req = (HttpServletRequest) request
        def assertAttributeValue = req.getHeader( CH?.config?.banner.sso.authenticationAssertionAttribute )

        if (assertAttributeValue) {
            if (!SecurityContextHolder.context?.authentication) {
                log.debug "BannerPreAuthenticatedFilter.doFilter found assertAttributeValue $assertAttributeValue"
                def dbUser = BannerAuthenticationProvider.getMappedDatabaseUserForUdcId( assertAttributeValue, dataSource )
                log.debug "BannerPreAuthenticatedFilter.doFilter found Oracle database user $dbUser for assertAttributeValue $assertAttributeValue"

                def fullName = BannerAuthenticationProvider.getFullName( dbUser['name'].toUpperCase(), dataSource ) as String
                log.debug "BannerPreAuthenticatedFilter.doFilter found full name $fullName"

                Collection<GrantedAuthority> authorities = BannerAuthenticationProvider.determineAuthorities( dbUser, dataSource )
                log.debug "BannerPreAuthenticatedFilter.doFilter found authorities $authorities"

                def user = new BannerUser( dbUser['name'],       // username
                                           'none',       // password
                                           dbUser['oracleUserName'],       // oracle username (note this may be null)
                                           true,         // enabled (account)
                                           true,         // accountNonExpired
                                           true,         // credentialsNonExpired 
                                           true,         // accountNonLocked 
                                           authorities,
                                           fullName )
                log.debug "BannerPreAuthenticatedFilter.doFilter bannerUser created $user"

                def token = new BannerAuthenticationToken( user )
                log.debug "BannerPreAuthenticatedFilter.doFilter BannerAuthenticationToken created $token"

                SecurityContextHolder.context.setAuthentication( token )
            }
        }
        chain.doFilter( request, response );
    }


    protected Object getPreAuthenticatedPrincipal( HttpServletRequest request ) {
       log.info "BannerPreAuthenticatedFilter.getPreAuthenticatedPrincipal method called"
    }

    protected Object getPreAuthenticatedCredentials( HttpServletRequest request ) {
       log.info "BannerPreAuthenticatedFilter.getPreAuthenticatedCredentials method called"
    }

}

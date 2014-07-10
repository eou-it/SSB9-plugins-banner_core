/*******************************************************************************
 Copyright 2009-2014 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.security

import groovy.sql.Sql
import org.codehaus.groovy.grails.commons.ConfigurationHolder as CH
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.web.authentication.AuthenticationFailureHandler
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler

import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import org.apache.log4j.Logger
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter

import javax.servlet.http.HttpServletResponse

class BannerPreAuthenticatedFilter extends AbstractPreAuthenticatedProcessingFilter {

    private final Logger log = Logger.getLogger(getClass())

    def dataSource // injected by Spring

    private AuthenticationFailureHandler failureHandler = new SimpleUrlAuthenticationFailureHandler();

    public void doFilter( ServletRequest request, ServletResponse response,
                          FilterChain chain ) throws IOException, ServletException {

        log.debug "BannerPreAuthenticatedFilter.doFilter invoked with request $request"
        HttpServletRequest req = (HttpServletRequest) request
        def authenticationAssertionAttribute = CH?.config?.banner.sso.authenticationAssertionAttribute
        def assertAttributeValue = req.getHeader( authenticationAssertionAttribute )

        if (assertAttributeValue) {
            if (!SecurityContextHolder.context?.authentication) {
                log.debug "BannerPreAuthenticatedFilter.doFilter found assertAttributeValue $assertAttributeValue"
                def dbUser
                try {
                    dbUser = BannerAuthenticationProvider.getMappedDatabaseUserForUdcId( assertAttributeValue, dataSource )
                    log.debug "BannerPreAuthenticatedFilter.doFilter found Oracle database user $dbUser for assertAttributeValue $assertAttributeValue"
                } catch(UsernameNotFoundException ue) {
                    unsuccessfulAuthentication(request, response, ue);
                    return;
                }

                def fullName = BannerAuthenticationProvider.getFullName( dbUser['name'].toUpperCase(), dataSource ) as String
                log.debug "BannerPreAuthenticatedFilter.doFilter found full name $fullName"

                Collection<GrantedAuthority> authorities
                def conn
                if (isSsbEnabled()) {
                    try {
                        conn = dataSource.getSsbConnection()
                        Sql db = new Sql( conn )
                        authorities = SelfServiceBannerAuthenticationProvider.determineAuthorities( dbUser, db )
                        log.debug "BannerPreAuthenticatedFilter.doFilter found Self Service authorities $authorities"
                    } catch(Exception e) {
                        log.fatal("Error occurred in loading authorities : " + e.localizedMessage())
                        unsuccessfulAuthentication(request, response, new UsernameNotFoundException(e.localizedMessage()));
                    } finally {
                        conn?.close()
                    }

                }
                else {
                    authorities = BannerAuthenticationProvider.determineAuthorities( dbUser, dataSource )
                    log.debug "BannerPreAuthenticatedFilter.doFilter found Banner Admin authorities $authorities"
                }

                dbUser.authorities = authorities
                dbUser.fullName = fullName
                def token = BannerAuthenticationProvider.newAuthenticationToken( this, dbUser )
                log.debug "BannerPreAuthenticatedFilter.doFilter BannerAuthenticationToken created $token"

                SecurityContextHolder.context.setAuthentication( token )
                log.debug "BannerPreAuthenticatedFilter.doFilter $token set in SecurityContextHolder"
            }
        } else {
            if(CH?.config?.banner?.sso?.authenticationProvider.equalsIgnoreCase('external')) {
                log.fatal("System is configured for external authentication and identity assertion $authenticationAssertionAttribute is null")
                unsuccessfulAuthentication(request, response, new UsernameNotFoundException("System is configured for external authentication and identity assertion $authenticationAssertionAttribute is null"));
                return;
            }
        }

        chain.doFilter( request, response );

    }

    private static def isSsbEnabled() {
        SelfServiceBannerAuthenticationProvider.isSsbEnabled()
    }

    protected Object getPreAuthenticatedPrincipal( HttpServletRequest request ) {
        log.info "BannerPreAuthenticatedFilter.getPreAuthenticatedPrincipal method called"
    }

    protected Object getPreAuthenticatedCredentials( HttpServletRequest request ) {
        log.info "BannerPreAuthenticatedFilter.getPreAuthenticatedCredentials method called"
    }

    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) {
        failureHandler.defaultFailureUrl = '/login/error'
        failureHandler.onAuthenticationFailure(request, response, failed)
    }
}

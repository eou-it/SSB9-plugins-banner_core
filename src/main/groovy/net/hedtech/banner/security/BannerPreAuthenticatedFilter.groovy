/*******************************************************************************
 Copyright 2009-2018 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.security

import grails.plugin.springsecurity.SpringSecurityUtils
import grails.util.Holders  as CH
import groovy.util.logging.Slf4j
import org.springframework.security.access.ConfigAttribute
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.web.authentication.AuthenticationFailureHandler
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler
import org.springframework.util.AntPathMatcher

import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter

import javax.servlet.http.HttpServletResponse

@Slf4j
class BannerPreAuthenticatedFilter extends AbstractPreAuthenticatedProcessingFilter {


    def dataSource // injected by Spring

    private AuthenticationFailureHandler failureHandler = new SimpleUrlAuthenticationFailureHandler();

    public void doFilter( ServletRequest request, ServletResponse response,
                          FilterChain chain ) throws IOException, ServletException {

        log.debug "BannerPreAuthenticatedFilter.doFilter invoked with request $request"
        HttpServletRequest req = (HttpServletRequest) request

        if(!requiresAuthentication(req)) {
            chain.doFilter( request, response );
            return
        }

        def authenticationAssertionAttribute = CH?.config?.banner.sso.authenticationAssertionAttribute
        def assertAttributeValue = req.getHeader( authenticationAssertionAttribute )

        if (assertAttributeValue == null) {
            if(CH?.config?.banner?.sso?.authenticationProvider.equalsIgnoreCase('external')) {
                log.error("System is configured for external authentication and identity assertion $authenticationAssertionAttribute is null")
                unsuccessfulAuthentication(request, response, new UsernameNotFoundException("System is configured for external authentication and identity assertion $authenticationAssertionAttribute is null"));
                return;
            }
        }

        log.debug "BannerPreAuthenticatedFilter.doFilter found assertAttributeValue $assertAttributeValue"
        def dbUser
        try {
            dbUser = AuthenticationProviderUtility.getMappedUserForUdcId( assertAttributeValue, dataSource )
            log.debug "BannerPreAuthenticatedFilter.doFilter found database user $dbUser for assertAttributeValue $assertAttributeValue"
            if(dbUser == null)
            {
                throw new BadCredentialsException("System is configured for external authentication, identity assertion $assertAttributeValue does not map to a Banner user")
            }
            // Next, we'll verify the authenticationResults (and throw appropriate exceptions for expired pin, disabled account, etc.)
            AuthenticationProviderUtility.verifyAuthenticationResults dbUser
            log.debug "BannerPreAuthenticatedFilter.doFilter verify authentication results"

            BannerAuthenticationToken token = AuthenticationProviderUtility.createAuthenticationToken(dbUser, dataSource, this)
            log.debug "BannerPreAuthenticatedFilter.doFilter BannerAuthenticationToken created $token"

            SecurityContextHolder.context.setAuthentication( token )
            log.debug "BannerPreAuthenticatedFilter.doFilter $token set in SecurityContextHolder"

        } catch(Exception e) {
            unsuccessfulAuthentication(request, response, e);
            return;
        }

        chain.doFilter( request, response );

    }
    private boolean requiresAuthentication(HttpServletRequest request) {
        if (SecurityContextHolder.context?.authentication){
            log.debug "BannerPreAuthenticatedFilter.requiresAuthentication SecurityContextHolder has the BannerToken"
            return false
        }
        String url = request.getRequestURI().substring(request.getContextPath().length())
        log.debug "BannerPreAuthenticatedFilter.requiresAuthentication url {}", url
        List interceptUrls= SpringSecurityUtils.securityConfig["interceptUrlMap"]
        AntPathMatcher antPathMatcher = new AntPathMatcher()
        log.debug "BannerPreAuthenticatedFilter.requiresAuthentication antUrlPathMatcher $antPathMatcher"
        for ( interceptUrl in interceptUrls ) {
            log.debug "BannerPreAuthenticatedFilter.requiresAuthentication entry : $interceptUrl"
            if (antPathMatcher.match(interceptUrl.pattern, url)) {
                    log.debug "BannerPreAuthenticatedFilter.requiresAuthentication url $url matches $interceptUrl from interceptUrlMap"
                    if(interceptUrl.access?.contains("IS_AUTHENTICATED_ANONYMOUSLY")) {
                        log.debug "BannerPreAuthenticatedFilter.requiresAuthentication url $url is authenticated anonymously"
                        return false
                    }
                    return true
                }
            }
        log.debug "BannerPreAuthenticatedFilter.requiresAuthentication url $url requires authentication"
        return true
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

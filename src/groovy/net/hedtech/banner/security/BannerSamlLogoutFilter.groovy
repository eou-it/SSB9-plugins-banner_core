/*******************************************************************************
 Copyright 2009-2014 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.security

import org.apache.log4j.Logger
import org.opensaml.common.SAMLException
import org.opensaml.saml2.metadata.provider.MetadataProviderException
import org.opensaml.ws.message.encoder.MessageEncodingException
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.saml.SAMLAuthenticationToken
import org.springframework.security.saml.SAMLConstants
import org.springframework.security.saml.SAMLCredential
import org.springframework.security.saml.SAMLLogoutFilter
import org.springframework.security.saml.context.SAMLMessageContext
import org.springframework.security.web.authentication.logout.LogoutHandler
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler
import org.springframework.util.Assert

import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * An Filter added to handle the SAMl logout with token of type BannerAuthenticationToken.
 */
class BannerSamlLogoutFilter extends SAMLLogoutFilter{

    private static final Logger log = Logger.getLogger( "net.hedtech.banner.security.BannerSamlLogoutFilter" )

    BannerSamlLogoutFilter(String successUrl, LogoutHandler[] localHandler, LogoutHandler[] globalHandlers) {
        super(successUrl, localHandler, globalHandlers)
    }

    BannerSamlLogoutFilter(LogoutSuccessHandler logoutSuccessHandler, LogoutHandler[] localHandler, LogoutHandler[] globalHandlers) {
        super(logoutSuccessHandler, localHandler, globalHandlers)
    }

    public void processLogout(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {

        if (requiresLogout(request, response)) {

            try {
               BannerAuthenticationToken auth=SecurityContextHolder.getContext().getAuthentication();
                if (auth != null && isGlobalLogout(request,auth)) {
                    if(!(auth instanceof BannerAuthenticationToken)) {
                        log.fatal("Authentication object doesn't contain SAML credential, cannot perform global logout")
                        throw new ServletException("Authentication object doesn't contain SAML credential, cannot perform global logout")
                    }
                    Assert.isInstanceOf(BannerAuthenticationToken.class,auth,"Authentication object doesn't contain SAML credential, cannot perform global logout")
                    for (        LogoutHandler handler : globalHandlers) {
                        handler.logout(request,response,auth)
                    }
                    SAMLMessageContext context = contextProvider.getLocalEntity(request,response,(SAMLCredential)auth.getSAMLCredential())
                    profile.sendLogoutRequest(context,auth.getSAMLCredential())
                    log.debug("Logout Request initiated with ontext : " + context)
                }
                else {
                    super.doFilter(request,response,chain);
                }
            } catch (SAMLException e1) {
                log.fatal("Error initializing global logout " + e1)
                throw new ServletException("Error initializing global logout", e1)
            } catch (MetadataProviderException e1) {
                log.fatal("Error processing metadata " + e1)
                throw new ServletException("Error processing metadata", e1)
            } catch (MessageEncodingException e1) {
                log.fatal("Error encoding outgoing message " + e1)
                throw new ServletException("Error encoding outgoing message", e1)
            }
        } else {
            chain.doFilter(request, response);
        }

    }


    /**
     * Performs global logout in case current user logged in using SAML and user hasn't selected local logout only
     *
     * @param request request
     * @param auth    currently logged in user
     * @return true if single logout with IDP is required
     */
    protected boolean isGlobalLogout(HttpServletRequest request, Authentication auth) {
        String login = request.getParameter(LOGOUT_PARAMETER);
        return (login == null || !"true".equals(login.toLowerCase().trim())) && (auth instanceof BannerAuthenticationToken);
    }




}

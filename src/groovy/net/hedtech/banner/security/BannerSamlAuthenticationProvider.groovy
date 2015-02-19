package net.hedtech.banner.security

import groovy.sql.Sql
import org.springframework.security.saml.SAMLAuthenticationProvider
import org.springframework.security.saml.SAMLAuthenticationToken
import org.springframework.security.saml.SAMLConstants
import org.springframework.security.saml.SAMLCredential
import org.springframework.security.saml.context.SAMLMessageContext

import org.springframework.security.core.Authentication
import org.springframework.security.authentication.*

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.opensaml.common.SAMLException;
import org.opensaml.common.SAMLRuntimeException;

import org.springframework.security.authentication.AuthenticationServiceException;
import org.opensaml.xml.encryption.DecryptionException;
import org.opensaml.xml.validation.ValidationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.AuthenticationException;

class BannerSamlAuthenticationProvider extends SAMLAuthenticationProvider  {
    def dataSource

    public BannerSamlAuthenticationProvider() {
        super();
    }

    /**
     * Attempts to perform authentication of an Authentication object. The authentication must be of type
     * SAMLAuthenticationToken and must contain filled SAMLMessageContext. If the SAML inbound message
     * in the context is valid, UsernamePasswordAuthenticationToken with name given in the SAML message NameID
     * and assertion used to verify the user as credential (SAMLCredential object) is created and set as authenticated.
     *
     * @param authentication SAMLAuthenticationToken to verify
     * @return UsernamePasswordAuthenticationToken with name as NameID value and SAMLCredential as credential object
     * @throws AuthenticationException user can't be authenticated due to an error
     */
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        if (!supports(authentication.getClass())) {
            throw new IllegalArgumentException("Only SAMLAuthenticationToken is supported, " + authentication.getClass() + " was attempted");
        }

        SAMLAuthenticationToken token = (SAMLAuthenticationToken) authentication;
        SAMLMessageContext context = token.getCredentials();
        SAMLCredential credential;

        try {
            if (SAMLConstants.SAML2_WEBSSO_PROFILE_URI.equals(context.getCommunicationProfileId())) {
                credential = consumer.processAuthenticationResponse(context);
            } else if (SAMLConstants.SAML2_HOK_WEBSSO_PROFILE_URI.equals(context.getCommunicationProfileId())) {
                credential = hokConsumer.processAuthenticationResponse(context);
            } else {
                throw new SAMLException("Unsupported profile encountered in the context " + context.getCommunicationProfileId());
            }
        } catch (SAMLRuntimeException e) {
            samlLogger.log(SAMLConstants.AUTH_N_RESPONSE, SAMLConstants.FAILURE, context, e);
            throw new AuthenticationServiceException("Error validating SAML message", e);
        } catch (SAMLException e) {
            samlLogger.log(SAMLConstants.AUTH_N_RESPONSE, SAMLConstants.FAILURE, context, e);
            throw new AuthenticationServiceException("Error validating SAML message", e);
        } catch (ValidationException e) {
            log.debug("Error validating signature", e);
            samlLogger.log(SAMLConstants.AUTH_N_RESPONSE, SAMLConstants.FAILURE, context);
            throw new AuthenticationServiceException("Error validating SAML message signature", e);
        } catch (org.opensaml.xml.security.SecurityException e) {
            log.debug("Error validating signature", e);
            samlLogger.log(SAMLConstants.AUTH_N_RESPONSE, SAMLConstants.FAILURE, context);
            throw new AuthenticationServiceException("Error validating SAML message signature", e);
        } catch (DecryptionException e) {
            log.debug("Error decrypting SAML message", e);
            samlLogger.log(SAMLConstants.AUTH_N_RESPONSE, SAMLConstants.FAILURE, context);
            throw new AuthenticationServiceException("Error decrypting SAML message", e);
        }

        // TODO:  need to extract UDC_ID from credentials
        def udc_id = '30078' //  credential.nameID?.value gives the name passed from SAML assertion. Need to add additional attributes in SAML assertion
        def dbUser
        try {
            dbUser = BannerAuthenticationProvider.getMappedDatabaseUserForUdcId( udc_id, dataSource )
            log.debug "BannerPreAuthenticatedFilter.doFilter found Oracle database user $dbUser for assertAttributeValue 30078"
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
        BannerAuthenticationToken bannerAuthenticationToken = BannerAuthenticationProvider.newAuthenticationToken( this, dbUser )
        // extract map & store in bannerAuthenticationToken

        log.debug "BannerPreAuthenticatedFilter.doFilter BannerAuthenticationToken created $bannerAuthenticationToken"

        samlLogger.log(SAMLConstants.AUTH_N_RESPONSE, SAMLConstants.SUCCESS, context, bannerAuthenticationToken, null);
        return bannerAuthenticationToken

    }
    private static def isSsbEnabled() {
        SelfServiceBannerAuthenticationProvider.isSsbEnabled()
    }

    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) {
        failureHandler.defaultFailureUrl = '/login/error'
        failureHandler.onAuthenticationFailure(request, response, failed)
    }
}

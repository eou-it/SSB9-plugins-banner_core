/*******************************************************************************
Copyright 2018 Ellucian Company L.P. and its affiliates.
*******************************************************************************/
package net.hedtech.banner.controllers

import grails.plugin.springsecurity.SpringSecurityUtils
import grails.util.Holders
import org.springframework.web.context.request.RequestContextHolder

/**
 * Utilities for controllers.
 */
class ControllerUtils {


    public static def buildLogoutRedirectURI() {

        def uri =  SpringSecurityUtils.securityConfig.logout.filterProcessesUrl //'/j_spring_security_logout'
        if(isSamlEnabled()) {
            uri="/"+Holders.config?.logoutEndpoint
        }
        def mep = RequestContextHolder?.currentRequestAttributes()?.request?.session?.getAttribute("mep") || RequestContextHolder?.currentRequestAttributes()?.params?.mepCode
        if (mep) {
            if(uri.contains("?")){
                uri += "&spring-security-redirect=?mepCode=${mep}"
            }else{
                uri += "?spring-security-redirect=?mepCode=${mep}"
            }
        }

        uri
    }

    public static def getHomePageURL() {
        return SpringSecurityUtils.securityConfig.homePageUrl

    }

    public static def getAfterLogoutRedirectURI() {
        return SpringSecurityUtils.securityConfig.logout.afterLogoutUrl

    }

    public static boolean isSamlEnabled() {
        def samlEnabled = Holders?.config.banner.sso.authenticationProvider
        if(samlEnabled){
            return 'saml'.equalsIgnoreCase( samlEnabled )
        }else{
            return false;
        }
    }

    public static boolean isCasEnabled() {
        def casEnabled = Holders?.config.banner.sso.authenticationProvider
        if(casEnabled){
            return 'cas'.equalsIgnoreCase( casEnabled )
        }else{
            return false;
        }
    }

    public static boolean isLocalLogoutEnabled() {
        def localLogoutEnabled = Holders?.config.banner?.sso?.authentication.saml.localLogout
        if(localLogoutEnabled){
            return 'true'.equalsIgnoreCase( localLogoutEnabled );
        }else {
            return false;
        }
    }
    public static boolean isGuestAuthenticationEnabled() {
        Holders.config.guestAuthenticationEnabled instanceof Boolean ? Holders.config.guestAuthenticationEnabled : false
    }

    public static def aboutServiceUrl() {
        return Holders?.config.banner.about.serviceUrl
    }

}

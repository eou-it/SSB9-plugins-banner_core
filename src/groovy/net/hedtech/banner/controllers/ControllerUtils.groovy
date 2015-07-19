/*******************************************************************************
Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
*******************************************************************************/ 
package net.hedtech.banner.controllers

import grails.util.Holders
import grails.plugin.springsecurity.SpringSecurityUtils


import org.codehaus.groovy.grails.web.servlet.mvc.GrailsWebRequest
import org.springframework.web.context.request.RequestContextHolder

/**
 * Utilities for controllers.
 */
class ControllerUtils {


    public static def keyblock = { controller ->
        if (controller.request[ "keyblock" ] == null) {
            controller.request[ "keyblock" ] = ParamsUtils.namedParams( controller.params, "keyblock." )
        }
        return controller.request[ "keyblock" ]
    }


    public static def buildModel = { keyblock, blocks ->
        def model = [ keyblock: keyblock ]

        if (blocks) {
            blocks.each { block ->
                block.each {
                    model.put( it.key, it.value )
                }
            }
        }
        return model
    }



    public static def buildLogoutRedirectURI() {

        def uri =  SpringSecurityUtils.securityConfig.logout.filterProcessesUrl //'/j_spring_security_logout'
        if(isSamlEnabled()) {
            uri= "/"+RequestContextHolder?.currentRequestAttributes()?.request?.session?.getServletContext().getAttribute("logoutEndpoint")
        }
        def mep = RequestContextHolder?.currentRequestAttributes()?.request?.session?.getAttribute("mep")
        if (mep) {
            uri += "?spring-security-redirect=?mepCode=${mep}"
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
}

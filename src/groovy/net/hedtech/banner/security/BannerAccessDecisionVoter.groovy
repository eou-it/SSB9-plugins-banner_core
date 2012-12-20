/*******************************************************************************
Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
*******************************************************************************/ 
package net.hedtech.banner.security

import org.apache.log4j.Logger

import org.codehaus.groovy.grails.commons.ConfigurationHolder as CH

import org.springframework.security.access.vote.RoleVoter
import org.springframework.security.access.AccessDecisionVoter
import org.springframework.security.access.ConfigAttribute
import org.springframework.security.core.Authentication
import org.springframework.security.web.FilterInvocation
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.context.request.RequestContextHolder
import org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes
import org.codehaus.groovy.grails.web.context.ServletContextHolder
import org.springframework.context.ApplicationContext

/**
 * A Spring Security RoleVoter that authorizes a user by determining if any of
 * the user's authorities pertain to the current request (i.e., pertain to the
 * 'FORM' associated to the request). This approach precludes the need to map
 * roles to each URL.
 *
 * Roles that end with '_CONNECT' will not allow access, but any other role
 * pertaining to the Form(s) associated to the current request will grant access.
 * THe UI and service layers may need to perform additional handling
 * (e.g., using conventions such as '_Q' roles give only read-only access).
 */
class BannerAccessDecisionVoter extends RoleVoter {

    static final String ROLE_PREFIX = "ROLE_"
    static final String ROLE_DETERMINED_DYNAMICALLY = 'ROLE_DETERMINED_DYNAMICALLY'

    private final Logger log = Logger.getLogger( getClass() )


    boolean supports( ConfigAttribute configAttribute ) {
        log.debug "BannerAccessDecisionVoter.supports(ConfigAttribute) invoked with $configAttribute and will return ${configAttribute.attribute.startsWith( ROLE_PREFIX )}"
        configAttribute.attribute.startsWith( ROLE_PREFIX )
    }


    boolean supports( Class<?> clazz ) {
        log.debug "BannerAccessDecisionVoter.supports(Class) invoked with $clazz and will return ${FilterInvocation.class.isAssignableFrom( clazz )}"
        return FilterInvocation.class.isAssignableFrom( clazz )
    }


    int vote( Authentication authentication, Object o, Collection<ConfigAttribute> configAttributes ) {
        log.trace "BannerAccessDecisionVoter.vote() invoked with Authentication $authentication, Object $o, and ConfigAttributes $configAttributes"

        def url = extractUrl( o )
        log.debug "BannerAccessDecisionVoter.vote() will vote on $url"

        // Despite having a supports(Class) method that is called, we're still getting asked to handle non-ROLE attributes.  So, we'll check ourselves...
        if (!configAttributes?.any { it.attribute.startsWith( ROLE_PREFIX ) } )  {
            log.debug "BannerAccessDecisionVoter.vote() did not find any ROLE_ attributes, so will ABSTAIN"
            return AccessDecisionVoter.ACCESS_ABSTAIN
        }

        if (authentication.getDetails() == null) {
            return AccessDecisionVoter.ACCESS_DENIED
        }
        
        if (authentication.principal instanceof String) {
            return AccessDecisionVoter.ACCESS_ABSTAIN
        }

        vote( authentication, url, configAttributes )
    }


// ------------------------------------------- Helper Methods ------------------------------------------------


    private String extractUrl( Object o ) {
        FilterInvocation invocation = (FilterInvocation) o // cast is safe due to 'supports' method
        invocation.requestUrl
    }


    // The FormContext is not yet set (we haven't even reached the Dispatcher yet!), so we'll have to do our own work..
    private List getCorrespondingFormNamesFor( String url ) {

        def base = [ "api", "resource" ]

        String lcUrl = url.toLowerCase()

        def splitIndex
        if (url.contains("banner.zul")){
          splitIndex = 3
        }

        def urlParts = lcUrl.split( /\/|\?|\./ ).toList() // note, first element will be empty string (i.e., representing before the first '/')

        if (splitIndex && !(urlParts[1] in base)) {
            def pageName = RequestContextHolder.currentRequestAttributes().request.getParameter("page")?.toLowerCase()
            if (!pageName) pageName="mainpage"
            return new ArrayList( CH.config.formControllerMap[pageName])
        }

        log.debug "BannerAccessDecisionVoter.vote() has parsed url into: $urlParts"
        def result = CH.config.formControllerMap?.find { k, v ->
            if (splitIndex) {
                k == (urlParts[1] in base ? urlParts[2] : urlParts[urlParts.size() - splitIndex]) // we may have to ignore '/api/' if found within the uri
            }
            else {
                k == (urlParts[1] in base ? urlParts[2] : urlParts[1]) // we may have to ignore '/api/' if found within the uri
            }
        }
        result?.value
    }


    private List getApplicableAuthorities( List forms, Authentication authentication ) {
        if (authentication.principal instanceof String) return []
        List applicableAuthorities = []
        forms?.each { form ->
            def authoritiesForForm = authentication.principal.authorities.findAll { it.authority ==~ /\w+_${form}_\w+/ }
            authoritiesForForm.each { applicableAuthorities << it }
        }
        applicableAuthorities
    }


    private int vote( Authentication authentication, String url, Collection<ConfigAttribute> configAttributes ) {
        
        def useDynamicAuthorization = configAttributes.any { it.attribute == ROLE_DETERMINED_DYNAMICALLY } 
        
        // dynamic form-based authorization due to special ROLE_DETERMINED_DYNAMICALLY role mapped to the url
        if (useDynamicAuthorization) {
	        log.debug "BannerAccessDecisionVoter.vote() will perform dynamic form-based authorization"
	
	        def forms = getCorrespondingFormNamesFor( url )
	        if (forms) {
    	        log.debug "BannerAccessDecisionVoter.vote() found form(s) (${forms}) mapped for URL $url"

    	        List applicableAuthorities = getApplicableAuthorities( forms, authentication )

    	        // Now we'll exclude the special '_CONNECT' roles, as we don't want to give access for those...
    	        applicableAuthorities.removeAll { it ==~ /.*_CONNECT.*/ }
    	        
    	        if (applicableAuthorities.size() > 0) {
    	            log.debug "BannerAccessDecisionVoter.vote() has found an applicable authority and will grant access"
    	            return AccessDecisionVoter.ACCESS_GRANTED
    	        }

                //Since user doesn't have access to url now log it in violation table.
                logViolation( authentication, forms, url )
            }
    	    log.debug "BannerAccessDecisionVoter.vote() did NOT find any applicable authorities, and will DENY access"
    	    return AccessDecisionVoter.ACCESS_DENIED    	        
        }

        // explicit uri-role map based authorization
        log.debug "BannerAccessDecisionVoter.vote() will base authorization on roles explicitly specified for the url"
        def authorityNames = authentication.principal.authorities*.authority
        def hasRole = configAttributes?.any { it.attribute in authorityNames }
        
        if (hasRole) {
	        log.debug "BannerAccessDecisionVoter.vote() found user has a role that was explicitly specified for the url, and will grant access"
	        return AccessDecisionVoter.ACCESS_GRANTED
        } else {
	        log.debug "BannerAccessDecisionVoter.vote() found user does not have a role that was explicitly specified for the url, and will ABSTAIN"
	        return AccessDecisionVoter.ACCESS_ABSTAIN
        }

        // To allow based upon IP address...
        //        WebAuthenticationDetails details = (WebAuthenticationDetails) authentication.getDetails()
        //        String remoteIPAddress = details.getRemoteAddress();
    }

    private logViolation( def authentication, def forms, def url ) {
        def urlParts = url.split(/\/|\?|\./).toList()
        String message = "User ${authentication.name} is not authorized to access "
        if (url.contains("?page=") && urlParts[1] != 'api') {
            def pageName = RequestContextHolder.currentRequestAttributes()?.request?.getParameter( "page" )?.toLowerCase()
            if (pageName)
            message = message + "$pageName(${forms[0]})"
            else
                message = message + "${url}"
        }
        publishViolation(authentication, message, forms)
    }

      public static boolean isUserAuthorized( String pageName ) {
          List formNames = new ArrayList( CH.config.formControllerMap[ pageName.toLowerCase() ] )
          def authentication = SecurityContextHolder.getContext().getAuthentication()

          List applicableAuthorities = []
          formNames?.each { form ->
              def authoritiesForForm = authentication.principal.authorities.findAll { it.authority ==~ /\w+_${form}_\w+/ }
              authoritiesForForm.each { applicableAuthorities << it }
          }
          applicableAuthorities.removeAll { it ==~ /.*_CONNECT.*/ }
          if( ! (applicableAuthorities.size() > 0) ) {
              String message = "User ${authentication.name} is not authorized to access ${pageName}(${formNames[0]})"
              publishViolation( authentication, message, formNames )
              return false
          }
          return true
    }

    private static def publishViolation( def authentication, String message, def forms ) {
        def applicationContext = (ApplicationContext) ServletContextHolder.getServletContext().getAttribute(GrailsApplicationAttributes.APPLICATION_CONTEXT)
        applicationContext.publishEvent( new BannerAuthenticationEvent(authentication.name, false, message, forms[0], new Date(), 1) )
    }
}

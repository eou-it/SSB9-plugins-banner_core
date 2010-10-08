/** *****************************************************************************
 © 2010 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
package com.sungardhe.banner.security

import org.apache.log4j.Logger

import org.codehaus.groovy.grails.commons.ConfigurationHolder as CH

import org.springframework.security.access.vote.RoleVoter
import org.springframework.security.access.AccessDecisionVoter
import org.springframework.security.access.ConfigAttribute
import org.springframework.security.core.Authentication
import org.springframework.security.web.FilterInvocation

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

        vote( url, authentication )
    }


// ------------------------------------------- Helper Methods ------------------------------------------------


    private String extractUrl( Object o ) {
        FilterInvocation invocation = (FilterInvocation) o // cast is safe due to 'supports' method
        invocation.requestUrl
    }


    // The FormContext is not yet set (we haven't even reached the Dispatcher yet!), so we'll have to do our own work..
    private List getCorrespondingFormNamesFor( String url ) {
        String lcUrl = url.toLowerCase()
        def urlParts = lcUrl.split( /\/|\?|\./ ).toList() // note, first element will be empty string (i.e., representing before the first '/')
        log.debug "BannerAccessDecisionVoter.vote() has parsed url into: $urlParts"
        def result = CH.config.formControllerMap?.find { k, v ->
            k == (urlParts[1] == 'api' ? urlParts[2] : urlParts[1]) // we may have to ignore '/api/' if found within the uri
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


    private int vote( String url, Authentication authentication ) {

        def forms = getCorrespondingFormNamesFor( url )
        log.debug "BannerAccessDecisionVoter.vote() has found corresponding Forms: $forms"

        if (!forms) {
            log.debug "BannerAccessDecisionVoter.vote() could not find Forms mapped within Config.formControllerMap -- and will ABSTAIN"
            return AccessDecisionVoter.ACCESS_ABSTAIN
        }
        log.debug "BannerAccessDecisionVoter.vote() found FORM(S) (${forms}) mapped for URL $url"

        List applicableAuthorities = getApplicableAuthorities( forms, authentication )

        // Now we'll exclude the special '_CONNECT' roles, as we don't want to give access for those...
        applicableAuthorities.removeAll { it ==~ /.*_CONNECT.*/ }
        if (applicableAuthorities.size() > 0) {
            log.debug "BannerAccessDecisionVoter.vote() has found APPLICABLE AUTHORITIES: $applicableAuthorities"
            return AccessDecisionVoter.ACCESS_GRANTED
        } else {
            log.debug "BannerAccessDecisionVoter.vote() did NOT find applicable authorities, and will DENY access!"
            return AccessDecisionVoter.ACCESS_DENIED
        }

        // To allow based upon IP address...
        //        WebAuthenticationDetails details = (WebAuthenticationDetails) authentication.getDetails()
        //        String remoteIPAddress = details.getRemoteAddress();
    }
}

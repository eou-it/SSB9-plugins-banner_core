/** *****************************************************************************

 © 2010 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */


/**
 * Spring Security (aka acegi) configuration using Grails DSL for Spring (versus using XML).
 */
security {

	// see DefaultSecurityConfig.groovy for all settable/overridable properties

	active = true

//	loginUserDomainClass = "com.sungardhe.banner.security.User"
//	authorityDomainClass = "com.sungardhe.banner.security.Role"

	useRequestMapDomainClass = false

    requestMapString = """
      CONVERT_URL_TO_LOWERCASE_BEFORE_COMPARISON
      PATTERN_TYPE_APACHE_ANT

      /=IS_AUTHENTICATED_ANONYMOUSLY
      /login/**=IS_AUTHENTICATED_ANONYMOUSLY
      /logout/**=IS_AUTHENTICATED_ANONYMOUSLY      
      /js/**=IS_AUTHENTICATED_ANONYMOUSLY
      /css/**=IS_AUTHENTICATED_ANONYMOUSLY
      /images/**=IS_AUTHENTICATED_ANONYMOUSLY
      /plugins/**=IS_AUTHENTICATED_ANONYMOUSLY
      /errors/**=IS_AUTHENTICATED_ANONYMOUSLY
      /interest/**=ROLE_STVINTS_BAN_DEFAULT_M
      /**=ROLE_ANY_FORM_BAN_DEFAULT_M
    """
    
    providerNames = ['bannerAuthenticationProvider']


}

/** *****************************************************************************

 © 2010 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
package com.sungardhe.banner.security

import org.springframework.security.GrantedAuthority
import org.codehaus.groovy.grails.plugins.springsecurity.GrailsUserImpl

/**
 * A user domain class for use with Spring Security (aka acegi). Note that the plugin's AuthenticateService
 * expects a UserDetail that has a getDomainClass().  Since we don't currently have a domain object for
 * users, the domainClass field is set to null.
 */
public class BannerUser extends GrailsUserImpl {


    public BannerUser( final String username, final String password, final boolean enabled,
			           final boolean accountNonExpired, final boolean credentialsNonExpired,
			           final boolean accountNonLocked, final GrantedAuthority[] authorities ) throws IllegalArgumentException {

		super( username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities, null /*domainClass*/ )
	}


}
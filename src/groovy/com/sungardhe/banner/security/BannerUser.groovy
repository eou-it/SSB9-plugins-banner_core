/*********************************************************************************
 Copyright 2009-2011 SunGard Higher Education. All Rights Reserved.
 This copyrighted software contains confidential and proprietary information of 
 SunGard Higher Education and its subsidiaries. Any use of this software is limited 
 solely to SunGard Higher Education licensees, and is further subject to the terms 
 and conditions of one or more written license agreements between SunGard Higher 
 Education and the licensee in question. SunGard is either a registered trademark or
 trademark of SunGard Data Systems in the U.S.A. and/or other regions and/or countries.
 Banner and Luminis are either registered trademarks or trademarks of SunGard Higher 
 Education in the U.S.A. and/or other regions and/or countries.
 **********************************************************************************/
package com.sungardhe.banner.security

import org.springframework.security.core.GrantedAuthority

import org.codehaus.groovy.grails.plugins.springsecurity.GrailsUser

/**
 * A user domain class for use with Spring Security (aka acegi). Note that the plugin's AuthenticateService
 * expects a UserDetail that has a getDomainClass().  Since we don't currently have a domain object for
 * users, the domainClass field is set to null.
 */
public class BannerUser extends GrailsUser {
    Integer pidm
    Integer webTimeout
    String fullName
    String oracleUserName
    public String mepHomeContext
    public String mepProcessContext
    public String mepHomeContextDescription
    
    public BannerUser( final String username, final String password, 
                       final String oracleUserName, final boolean enabled,
			           final boolean accountNonExpired, final boolean credentialsNonExpired,
			           final boolean accountNonLocked, final Collection<GrantedAuthority> authorities, 
			           final String fullName
                       ) throws IllegalArgumentException {
   
        // Note: The spring-security-core plugin now includes an 'id' property, which is normally used to retrieve the user versus keeping the 
        // user in the session context (as doing so is too heavy).  We do not do this anyway -- as we don't really have a 'User' model at all.
        // Our 'Authentication' object suffices, and we do not need to retrieve the user on each request. Consequently we do not fetch the 
        // user's id (which would require an additional database query) and instead we simply set this 'id' property to null.
		super( username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities as Collection<GrantedAuthority>, null /* id */ )
        this.fullName = fullName
        this.oracleUserName = oracleUserName
	}


     public BannerUser( final String username, final String password,
                       final String oracleUserName, final boolean enabled,
			           final boolean accountNonExpired, final boolean credentialsNonExpired,
			           final boolean accountNonLocked, final Collection<GrantedAuthority> authorities,
			           final String fullName, final Integer pidm, final Integer webTimeout
                       ) throws IllegalArgumentException {

         // Note: The spring-security-core plugin now includes an 'id' property, which is normally used to retrieve the user versus keeping the
        // user in the session context (as doing so is too heavy).  We do not do this anyway -- as we don't really have a 'User' model at all.
        // Our 'Authentication' object suffices, and we do not need to retrieve the user on each request. Consequently we do not fetch the
        // user's id (which would require an additional database query) and instead we simply set this 'id' property to null.
		super( username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities as Collection<GrantedAuthority>, null /* id */ )
        this.fullName = fullName
        this.oracleUserName = oracleUserName
        this.pidm = pidm
        this.webTimeout = webTimeout
	}
	
	
	public String toString() {
	    "BannerUser[${super.toString()}, fullName=$fullName, oracleUserName=$oracleUserName, PIDM=$pidm, webTimeout=$webTimeout "
	}


}
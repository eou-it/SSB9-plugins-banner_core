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
import org.springframework.security.core.authority.GrantedAuthorityImpl


// NOTE: This implementation holds the Banner password that must be used 
// to unlock the role associated to this authority.  This appraoch was 
// taken since the view used to query for a users authorities includes the 
// passwords for those roles.  This appraoch stores role password data redundantly
// for each user, but also has benefits of needing to hold only those passwords 
// actively used by a logged in user. It also doesn't require special cache 
// handling if this data were centralized and cached for the entire application. 
// Regardless, this note is here as an indicator that we may want to revist this appraoch. 
/**
 * An implementation of the Spring Security GrantedAuthority for Banner.
 */
public class BannerGrantedAuthority extends GrantedAuthorityImpl {
	
	String objectName
	String roleName
	String bannerPassword
	
	
	static public BannerGrantedAuthority create( String objectName, String roleName, String bannerPassword ) {
		def authority = "ROLE_${objectName?.toUpperCase()}_${roleName?.toUpperCase()}"
		new BannerGrantedAuthority( authority, objectName, roleName, bannerPassword )
	}
	
	
	private BannerGrantedAuthority( String authority, String objectName, String roleName, String bannerPassword ) {
		super( authority )
		
		this.objectName = objectName?.toUpperCase()
		this.roleName = roleName?.toUpperCase()
		this.bannerPassword = bannerPassword
	}
	
}



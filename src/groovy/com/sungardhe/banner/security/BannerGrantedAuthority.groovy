/*******************************************************************************
 © 2011 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
*******************************************************************************/
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



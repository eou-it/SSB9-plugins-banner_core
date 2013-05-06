/*******************************************************************************
Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
*******************************************************************************/ 
package net.hedtech.banner.security

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

    public boolean isReadOnlyAccess() {
        AccessPrivilegeType.isReadOnlyPattern(this.roleName)
    }

    public boolean isReadWriteAccess() {
        AccessPrivilegeType.isReadWritePattern(this.roleName)
    }

    public def checkIfCompatibleWithACEGIRolePattern(formName) {
        this ==~ getACEGICompatibleRolePattern(formName)
    }

    public AccessPrivilegeType getAccessPrivilegeType() {
        if (isReadOnlyAccess()){
            return AccessPrivilegeType.READONLY
        } else if (isReadWriteAccess()) {
            return AccessPrivilegeType.READWRITE
        }
        return AccessPrivilegeType.UNDEFINED
    }

    /**
     * Get the ACEGI friendly role pattern("ROLE_<formName>_<roleName>") for the form name.
     *
     * @param formName
     * @return
     */
    private static def getACEGICompatibleRolePattern(String formName) {
        /\w+_${formName}_\w+/
    }


}



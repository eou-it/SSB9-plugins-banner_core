/*******************************************************************************
 Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.security

//import org.springframework.security.core.authority.GrantedAuthorityImpl

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

public class BannerGrantedAuthority extends BannerGrantedAuthorityImpl {

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

    public boolean isReadOnly() {
        AccessPrivilege.isReadOnlyPattern(this.roleName)
    }

    public boolean isReadWrite() {
        AccessPrivilege.isReadWritePattern(this.roleName)
    }

    public def checkIfCompatibleWithACEGIRolePattern(formName) {
        this ==~ getACEGICompatibleRolePattern(formName)
    }

    public AccessPrivilege getAccessPrivilege() {
        if (isReadOnly()){
            return AccessPrivilege.READONLY
        } else if (isReadWrite()) {
            return AccessPrivilege.READWRITE
        }
        return AccessPrivilege.UNDEFINED
    }

    /**
     * Get the ACEGI friendly role pattern("ROLE_<formName>_<roleName>") for the form name.
     */
    public static def getACEGICompatibleRolePattern(String formName) {
        /\w+_${formName}_\w+/
    }

    public boolean hasAccessToForm(String formName, List<AccessPrivilege> accessPrivilegeTypeList) {
        this.objectName == formName && accessPrivilegeTypeList.any { it == this.getAccessPrivilege()}
    }
    public String getAssignedSelfServiceRole() {
        String role = this.authority.substring("ROLE_SELFSERVICE".length() + 1)
        return role.split("_")[0]
    }
}



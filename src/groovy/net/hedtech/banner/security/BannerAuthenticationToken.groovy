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
package net.hedtech.banner.security

import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority

/**
 * An authentication token created upon successful authentication.
 */
public class BannerAuthenticationToken implements Authentication {

    private BannerUser user


    BannerAuthenticationToken( BannerUser user ) {
        this.user = user
    }


    public Collection getAuthorities() {
        user?.authorities
    }


    public Object getCredentials() {
        user?.password
    }


    public Object getDetails() {
        user
    }


    public Object getPrincipal() {
        user
    }


    public boolean isAuthenticated() {
        (user && user.enabled && user.accountNonExpired && user.credentialsNonExpired 
         && user.accountNonLocked && user.authorities && user.authorities.size() > 0)
    }


    public void setAuthenticated(boolean b) {
        // noop
    }

    public String getName() {
        user?.username
    }
        
    public String getOracleUserName() {
        user?.oracleUserName
    }

    public Integer getPidm() {
        user?.pidm
    }

    public Integer getGidm() {
        user?.gidm
    }

    public Integer getWebTimeout() {
        user?.webTimeout
    }


     public String getFullName() {
        user?.fullName
    }   
    
    public String toString() {
        "${super.toString()}[isAuthenticated()=${isAuthenticated()}, user=$user]"
    }

}

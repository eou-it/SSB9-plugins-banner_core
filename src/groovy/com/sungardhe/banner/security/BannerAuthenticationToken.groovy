/** *****************************************************************************
 © 2011 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
package com.sungardhe.banner.security

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

}
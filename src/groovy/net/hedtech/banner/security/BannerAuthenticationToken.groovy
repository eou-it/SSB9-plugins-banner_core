/*******************************************************************************
Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
*******************************************************************************/ 
package net.hedtech.banner.security

import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority

/**
 * An authentication token created upon successful authentication.
 */
public class BannerAuthenticationToken implements Authentication {

    private BannerUser user
    private Date tokenExpiration;

    BannerAuthenticationToken( BannerUser user , Date tokenExpiration) {
        this.user = user
        this.tokenExpiration = tokenExpiration;
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

    /**
     * @return null if no expiration is set, expiration date otherwise
     */
    public Date getTokenExpiration() {
        return tokenExpiration;
    }

    /**
     * SAML credentials can be kept without clearing.
     */
    @Override
    public void eraseCredentials() {
    }
}

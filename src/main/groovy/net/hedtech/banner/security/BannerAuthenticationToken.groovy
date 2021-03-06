/*******************************************************************************
 Copyright 2009-2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.security

import org.springframework.security.core.Authentication
/**
 * An authentication token created upon successful authentication.
 */
public class BannerAuthenticationToken implements Authentication {

    private BannerUser user
    // include a map
    private Map claims
    private def SAMLCredential
    private def sessionIndex

    public def getSAMLCredential(){
        SAMLCredential
    }

    public def getsessionIndex(){
        sessionIndex
    }
    BannerAuthenticationToken( BannerUser user , Date tokenExpiration) {
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

    public Map getClaims() {
        claims
    }

    public String toString() {
        "${super.toString()}[isAuthenticated()=${isAuthenticated()}, user=$user]"
    }

}

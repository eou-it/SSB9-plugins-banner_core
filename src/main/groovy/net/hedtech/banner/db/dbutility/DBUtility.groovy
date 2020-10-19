/* *****************************************************************************
 Copyright 2017-2020 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.db.dbutility

import grails.util.Holders
import groovy.util.logging.Slf4j
import net.hedtech.banner.apisupport.ApiUtils
import net.hedtech.banner.security.BannerUser
import net.hedtech.banner.security.FormContext
import org.springframework.security.core.context.SecurityContextHolder

@Slf4j
class DBUtility {


    private static def config = Holders.getConfig()

    public static boolean isNotApiProxiedOrNotOracleMappedSsbOrSsbAnonymous(user) {
        return (ApiUtils.isApiRequest() && isApiProxySupportDisabled()) ||
                (isSelfServiceRequest() &&
                        (isNotOracleUser(user) ||
                                isAnonymousTypeUser(user))
                )
    }

    public static boolean isOracleUser(user) {
        boolean isOracleUser = user instanceof BannerUser && user?.oracleUserName
        isOracleUser
    }

    public static boolean isSSUser() {
        def user = SecurityContextHolder?.context?.authentication?.principal
        def ssRole = false

        if (user instanceof BannerUser) {
            Set authorities = user?.authorities
            if (authorities) {
                authorities.each {
                    if (it.authority.contains("ROLE_SELFSERVICE")) {
                        ssRole = true
                    }
                }
            }
        }

        ssRole
    }


    public static boolean isNotOracleUser(user)   {
        return isOracleUser(user) == false
    }

    public static boolean isAnonymousTypeUser(user)   {
        boolean isAnonymousUser = user instanceof String && user == 'anonymousUser'
        isAnonymousUser
    }

    public static boolean isApiProxySupportDisabled(){
        return (isApiProxySupportEnabled() == false)
    }

    /**
     * Returns true if the current request is for an administrative page or if the solution is configured to proxy connections for SSB users.
     * */
    public static boolean isAdminOrOracleProxyRequired(user) {
        isOracleUser(user) && (isAdministrativeRequest() || isSSBProxySupportEnabled() || isApiProxySupportEnabled())
    }

    private static boolean isAdministrativeRequest() {
        log.trace "BannerDS.isAdministrativeRequest() will return '${!FormContext.isSelfService()}' (FormContext = ${FormContext.get()})"
        return !FormContext.isSelfService()
    }

    /**
     * Returns true if SSB support is enabled and configured to proxy connections for SSB users.
     * */
    public static boolean isSSBProxySupportEnabled() {
        def enabled = config.ssbEnabled instanceof Boolean ? config.ssbEnabled : false
        def proxySsb = config.ssbOracleUsersProxied instanceof Boolean ? config.ssbOracleUsersProxied : false
        log.trace "BannerDS.isSSBProxySupportEnabled() will return '${enabled && proxySsb}' (since SSB is ${enabled ? '' : 'not '} enabled and proxy SSB is $proxySsb)"
        enabled && proxySsb
    }

    public static boolean isApiProxySupportEnabled() {
        def proxyApi  = config.apiOracleUsersProxied instanceof Boolean ? config.apiOracleUsersProxied : false
        log.trace "BannerDS.isApiProxySupportEnabled() will return ${proxyApi} (since apiOracleUsersProxied is ${proxyApi})"
        return proxyApi;
    }

    public static boolean isCommmgrDataSourceEnabled() {
        boolean useCommmgrDatasource  = config.commmgrDataSourceEnabled instanceof Boolean ? config.commmgrDataSourceEnabled : false
        return useCommmgrDatasource;
    }

    public static isSelfServiceRequest() {
        log.trace "BannerDS.isSelfServiceRequest() will return '${FormContext.isSelfService()}' (FormContext = ${FormContext.get()})"
        FormContext.isSelfService()
    }

    public static boolean isMepEnabled() {
        def enabled = config.mepEnabled instanceof Boolean ? config.mepEnabled : false
        enabled
    }


    public static boolean isContextSecurityEnabled() {
        Boolean contextSecurityEnabled = config.contextSecurityEnabled instanceof Boolean ? config.contextSecurityEnabled : false
        log.debug "contextSecurityEnabled flag is = ${contextSecurityEnabled}"
        contextSecurityEnabled
    }
    //Checks if user is SS user with roles in GOVROLE/TWGRROLE table
    public static boolean isSsbUserWithAnyRole() {
        def user = SecurityContextHolder?.context?.authentication?.principal
        def ssRole = false

        if (user instanceof BannerUser) {
            Set authorities = user?.authorities
            if (authorities) {
                authorities.each {
                    if (it.authority.equals("ROLE_SELFSERVICE-SSROLE_BAN_DEFAULT_M")) {
                        ssRole = true
                    }
                }
            }
        }

        ssRole
    }

}

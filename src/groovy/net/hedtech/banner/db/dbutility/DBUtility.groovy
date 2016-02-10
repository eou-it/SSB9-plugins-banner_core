/* *****************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.db.dbutility

import grails.util.Holders
import net.hedtech.banner.apisupport.ApiUtils
import net.hedtech.banner.security.BannerUser
import net.hedtech.banner.security.FormContext
import org.apache.log4j.Logger

/**
 * Created by karthick on 7/2/2015.
 */
class DBUtility {

    private final static Logger log = Logger.getLogger(getClass())

    private static def  config = Holders.getConfig()

    public static boolean isNotApiProxiedOrNotOracleMappedSsbOrSsbAnonymous(user)   {
        return ( ApiUtils.isApiRequest() && isApiProxySupportDisabled()) ||
                ( isSelfServiceRequest() &&
                        (isNotOracleUser(user) ||
                                isAnonymousTypeUser(user))
                )
    }

    public static boolean isOracleUser(user){
        boolean isOracleUser = user instanceof BannerUser && user?.oracleUserName
        isOracleUser
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

    public static isSelfServiceRequest() {
        log.trace "BannerDS.isSelfServiceRequest() will return '${FormContext.isSelfService()}' (FormContext = ${FormContext.get()})"
        FormContext.isSelfService()
    }

}

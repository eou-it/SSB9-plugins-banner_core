/*******************************************************************************
 Copyright 2009-2016 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.security

import groovy.sql.Sql
import java.sql.SQLException
import javax.sql.DataSource
import org.apache.log4j.Logger
import grails.plugin.springsecurity.SpringSecurityUtils
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.GrantedAuthority

/**
 * Class for manipulating the Spring Security User Authorities.
 */
class BannerGrantedAuthorityService {

    static transactional = false

    private static final Logger staticLogger = Logger.getLogger(BannerGrantedAuthorityService.class)
    private static final String WEB_USER = "WEBUSER"
    private static final String ANONYMOUS_USER = "anonymousUser"

    /**
     *
     * @param authenticationResults - a map prepared out from the Authentication object
     * @param dataSource
     * @return
     */
    public static Collection<BannerGrantedAuthority> determineAuthorities(Map authenticationResults, DataSource dataSource) {

        def connection
        Sql sqlObject
        try {
            // We query the database for all role assignments for the user, using an unproxied connection.
            // The Banner roles are converted to an 'acegi friendly' format: e.g., ROLE_{FORM-OBJECT}_{BANNER_ROLE}
            connection = dataSource.unproxiedConnection
            sqlObject = new Sql(connection)
            return determineAuthorities(authenticationResults, sqlObject)
        } finally {
            connection?.close()
        }
    }

    public static List getSelfServiceUserRole() {
        def user = getUser()
        def roles = new ArrayList()
        if (user instanceof BannerUser) {
            Set authorities = user?.authorities
            if(authorities){
                authorities.each { BannerGrantedAuthority bannerGrantedAuthority ->
                    String role = bannerGrantedAuthority.getAssignedSelfServiceRole()
                    roles << role
                }
            }
        } else if(user instanceof String && user == ANONYMOUS_USER) {
            roles << WEB_USER
        }
        roles
    }

    public static List getSelfServiceDistinctUserRole(){
        def roles = new HashSet<String>(getSelfServiceUserRole())
        def distinctRoles = new ArrayList<String>(roles)
        distinctRoles
    }

    /**
     * This is to pull the authorities from DB for the signed-in user.
     *                                                                                  a
     * We query the database for all role assignments for the user, using an unproxied connection.
     * The Banner roles are converted to an 'acegi friendly' format: e.g., ROLE_{FORM-OBJECT}_{BANNER_ROLE}*
     * @param authenticationResults - a map prepared out from the Authentication object
     * @param sqlObject
     * @return
     */
    public static Collection<BannerGrantedAuthority> determineAuthorities(Map authenticationResults, Sql sqlObject) {

        Collection<BannerGrantedAuthority> authorities = []

        final oracleUserName = authenticationResults['oracleUserName']

        if (!oracleUserName) {
            staticLogger.debug("No authorities returned for $oracleUserName")
            return authorities   // empty list
        }

        try {
            def traceDS = Logger.getLogger(net.hedtech.banner.db.BannerDS.class).isTraceEnabled()
            if (traceDS) {
                sqlObject.call "{call dbms_application_info.set_module( 'SELFSERVICE', 'GETROLES' )}"
            }
            String query = """
	                select GOVUROL_OBJECT, GOVUROL_ROLE from govurol,gubobjs
	                    where govurol_userid = ? and
	                        (govurol_object = gubobjs_name and ( gubobjs_ui_version in ('A','C','D','E') OR gubobjs_name in ('GUAGMNU')) )"""
            sqlObject.eachRow(query, [oracleUserName.toUpperCase()]) { row ->
                /**
                 * Performance Tuning - Removed Select * since fetching role password is very expensive.
                 * Password would be fetch on demand while applying the roles for the connection in BannerDS.
                 * Set the role password as null initially as we are no longer fetching it during login.
                 */
                authorities << BannerGrantedAuthority.create(row.GOVUROL_OBJECT, row.GOVUROL_ROLE, null)
            }
            if (traceDS) {
                sqlObject.call "{call dbms_application_info.set_module( NULL, NULL )}"
            }
        } catch (SQLException e) {
            staticLogger.warn "UserAuthorityService not able to determine Authorities for user ${oracleUserName} due to exception $e.message"
        }
        staticLogger.trace "UserAuthorityService.determineAuthorities is returning ${authorities?.size()} authorities. "
        authorities
    }

    /**
     * Entry Condition:- FormContext must be set already.
     */
    public static List<BannerGrantedAuthority> filterAuthorities(List<BannerGrantedAuthority> grantedAuthorities) {
        if (!grantedAuthorities) {
            staticLogger.debug("Input list of Authorities were EMPTY !!!")
            return []
        }
        List formContext = new ArrayList(FormContext.get())
        staticLogger.debug "UserAuthorityService has retrieved the FormContext value: $formContext"
        return filterAuthoritiesForFormNames(grantedAuthorities, formContext)
    }

    public static List<BannerGrantedAuthority> filterAuthorities(List<String> formNames, authentication) {
        if (authentication.principal instanceof String) {
            staticLogger.debug("Authentication Principal is just a String")
            return []
        }
        final authorityList = (authentication.principal.authorities).asList()
        return filterAuthoritiesForFormNames(authorityList, formNames)
    }

    /**
     * Find matching authoritiese for each of the form names.
     */
    private static List<BannerGrantedAuthority> filterAuthoritiesForFormNames(List<BannerGrantedAuthority> grantedAuthorities, List<String> formNames) {
        List<BannerGrantedAuthority> applicableAuthorities = grantedAuthorities.grep { GrantedAuthority authority ->
            if(authority instanceof BannerGrantedAuthority)   {
                formNames?.find { String formName ->
                    authority?.checkIfCompatibleWithACEGIRolePattern(formName)
                }
            }
        }
        staticLogger.debug "Given FormContext of ${formNames?.join(',')}, the user's applicable authorities are $applicableAuthorities"
        return applicableAuthorities
    }

    public static List<BannerGrantedAuthority> filterAuthorities(BannerUser user) {
        List<BannerGrantedAuthority> applicableAuthorities = []
        List<BannerGrantedAuthority> authoritiesForForm
        def forms
        if (FormContext.get()) {
            forms = new ArrayList(FormContext.get())
        }
        forms?.each { form ->
            authoritiesForForm = user.getAuthoritiesFor(form)
            authoritiesForForm.each { applicableAuthorities << it }
        }
        applicableAuthorities
    }

    /**
     * Entry Condition: Spring security authorities are already loaded.
     */
    public static AccessPrivilege getAccessPrivilegeType(String formName) {
        BannerGrantedAuthority authority = getAuthorityForAnyAccessPrivilegeType(formName)
        authority?.getAccessPrivilege()
    }

    public static BannerGrantedAuthority getAuthorityForAnyAccessPrivilegeType(String formName) {
        return getAuthority(formName, [(AccessPrivilege.READONLY), (AccessPrivilege.READWRITE)])
    }

    public static BannerGrantedAuthority getAuthority(String formName, AccessPrivilege accessPrivilegeType) {
        getAuthority(formName, [(accessPrivilegeType)])
    }

    /**
     * Entry Condition: Spring Security Authorities are already loaded.
     */
    public static BannerGrantedAuthority getAuthority(String formName, List<AccessPrivilege> accessPrivilegeTypeList) {
        final authorities = SpringSecurityUtils.getPrincipalAuthorities()
        if (!authorities || authorities.isEmpty()) {
            throw new IllegalStateException("There are no authorities loaded for the form - $formName - Seems like there is no user signed-in.")
        }
        authorities.find { BannerGrantedAuthority authority ->
            if (authority) {
                authority?.hasAccessToForm(formName, accessPrivilegeTypeList)
            }
        }
    }

    public static boolean isFormEditable(String formName) {
        BannerGrantedAuthority authority = getAuthorityForAnyAccessPrivilegeType(formName)
        authority?.isReadWrite()

    }

    public static boolean isFormReadonly(String formName) {
        BannerGrantedAuthority authority = getAuthorityForAnyAccessPrivilegeType(formName)
        authority?.isReadOnly()
    }

    public static def getPidm() {
        def user = getUser()
        if (user instanceof BannerUser) {
            return user.pidm
        }
        return null
    }

    public static def getUser() {
        SecurityContextHolder?.context?.authentication?.principal
    }

    public static def getAuthorities() {
        getUser()?.authorities
    }

}

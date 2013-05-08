/*******************************************************************************
 Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.security

import groovy.sql.Sql
import java.sql.SQLException
import javax.sql.DataSource
import org.apache.log4j.Logger
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils

/**
 * Class for manipulating the Spring Security User Authorities.
 */
class BannerUserAuthorityService {

    static transactional = false

    private final Logger log = Logger.getLogger(getClass())
    private static final Logger staticLogger = Logger.getLogger(BannerUserAuthorityService.class)

    /**
     *
     * @param authenticationResults - a map prepared out from the Authentication object
     * @param dataSource
     * @return
     */
    public static Collection<BannerUserAuthority> determineAuthorities( Map authenticationResults, DataSource dataSource ) {

        def connection
        def sqlObject
        try {
            // We query the database for all role assignments for the user, using an unproxied connection.
            // The Banner roles are converted to an 'acegi friendly' format: e.g., ROLE_{FORM-OBJECT}_{BANNER_ROLE}
            connection = dataSource.unproxiedConnection
            sqlObject = new Sql( connection )
            return determineAuthorities( authenticationResults, sqlObject )
        } finally {
            connection?.close()
        }
    }


    /**
     * This is to pull the authorities from DB for the signed-in user.
     *
     * We query the database for all role assignments for the user, using an unproxied connection.
     * The Banner roles are converted to an 'acegi friendly' format: e.g., ROLE_{FORM-OBJECT}_{BANNER_ROLE}
     *
     * @param authenticationResults - a map prepared out from the Authentication object
     * @param sqlObject
     * @return
     */
    public static Collection<BannerUserAuthority> determineAuthorities (Map authenticationResults, Sql sqlObject) {

        def authorities = [] as Collection<BannerUserAuthority>

        if (!authenticationResults['oracleUserName']) {
            return authorities   // empty list
        }

        try {
            String query = """
	                select GOVUROL_OBJECT, GOVUROL_ROLE from govurol,gubobjs
	                    where govurol_userid = ? and
	                        (govurol_object = gubobjs_name and (gubobjs_ui_version in ('A','C') OR gubobjs_name in ('GUAGMNU')) )"""
            sqlObject.eachRow( query, [authenticationResults['oracleUserName'].toUpperCase()] ) { row ->
                /**
                 * Performance Tuning - Removed Select * since fetching role password is very expensive.
                 * Password would be fetch on demand while applying the roles for the connection in BannerDS.
                 * Set the role password as null initially as we are no longer fetching it during login.
                 */
                authorities << BannerUserAuthority.create( row.GOVUROL_OBJECT, row.GOVUROL_ROLE, null )
            }
        } catch (SQLException e) {
            staticLogger.error "UserAuthorityService not able to determine Authorities for user ${authenticationResults['oracleUserName']} due to exception $e.message"
        }
        staticLogger.trace "UserAuthorityService.determineAuthorities is returning ${authorities?.size()} authorities. "
        authorities
    }

    /**
     * Entry Condition:- FormContext must be set already.
     *
     * @param grantedAuthorities
     * @return
     */
    public static List<BannerUserAuthority> filterAuthorities (List<BannerUserAuthority> grantedAuthorities) {
        if (!grantedAuthorities) {
            return []
        }
        List formContext = new ArrayList(FormContext.get())
        staticLogger.debug "UserAuthorityService has retrieved the FormContext value: $formContext"
        return filterAuthoritiesForFormNames(grantedAuthorities, formContext)
    }

    /**
     *  Entry Condition: User is signed-in and authentication object is created.
     *
     * @param formNames
     * @param authentication
     * @return
     */
    public static List<BannerUserAuthority> filterAuthorities (List<String> formNames, authentication) {
        if (authentication.principal instanceof String) {
            return []
        }
        final authorityList = (authentication.principal.authorities).asList()
        return filterAuthoritiesForFormNames(authorityList, formNames)
    }

    /**
     * Find matching authoritiese for each of the form names.
     *
     * @param grantedAuthorities
     * @param formNames
     * @return
     */
    private static List<BannerUserAuthority> filterAuthoritiesForFormNames(List<BannerUserAuthority> grantedAuthorities, List<String> formNames) {
        List<BannerUserAuthority> applicableAuthorities = grantedAuthorities.grep { BannerUserAuthority authority ->
            formNames?.find { String formName ->
                authority?.checkIfCompatibleWithACEGIRolePattern(formName)
            }
        }
        staticLogger.debug "Given FormContext of ${formNames?.join(',')}, the user's applicable authorities are $applicableAuthorities"
        return applicableAuthorities
    }

    public static List<BannerUserAuthority> filterAuthorities(BannerUser user) {
        List<BannerUserAuthority> applicableAuthorities = []
        List<BannerUserAuthority> authoritiesForForm
        def forms
        if (FormContext.get())
            forms = new ArrayList(FormContext.get())
        forms?.each { form ->
            authoritiesForForm = user.getAuthoritiesFor(form)
            authoritiesForForm.each { applicableAuthorities << it }
        }
        applicableAuthorities
    }

    /**
     *
     * Resolve the authority for the given form name.
     *
     * Entry Condition: Spring security authorities are already loaded.
     *
     */
    public static AccessPrivilegeType resolveAuthority (String formName) {
        BannerUserAuthority authority = getAuthorityForAnyAccessPrivilegeType(formName)
        authority?.getAccessPrivilegeType()
    }

    /**
     * Get authority for the given form matching any of the
     * patterns.
     *
     */
    public static BannerUserAuthority getAuthorityForAnyAccessPrivilegeType(String formName) {
        return getAuthority(formName, [(AccessPrivilegeType.READONLY), (AccessPrivilegeType.READWRITE)])
    }

    /**
     * Get the authority for given form name for any of the matching patterns.
     *
     * Entry Condition: Spring Security Authorities are already loaded.
     *
     */
    public static BannerUserAuthority getAuthority(String formName, List<AccessPrivilegeType> accessPrivilegeTypeList) {
        SpringSecurityUtils.getPrincipalAuthorities().find { BannerUserAuthority authority ->
            if (authority) {
                authority?.hasAnyAccessToForm (formName, accessPrivilegeTypeList)
            }
        }
    }

    /**
     * Get the authority for given form name for a given pattern.
     */
    public static BannerUserAuthority getAuthority (String formName, AccessPrivilegeType accessPrivilegeType) {
        getAuthority (formName, [(accessPrivilegeType)])
    }

    /**
     */
    public static boolean isReadWriteAccess(String formName) {
        def authority = getAuthorityForAnyAccessPrivilegeType (formName)
        authority?.isReadWriteAccess()

    }

    /**
     */
    public static boolean isReadonlyAccess(String formName) {
        BannerUserAuthority authority = getAuthorityForAnyAccessPrivilegeType (formName)
        authority?.isReadOnlyAccess()
    }

    /**
     * Method depends on the authorities loaded on to the spring security.
     */
    public AccessPrivilegeType getAccessPrivilegeType(String formName) {
        return this.resolveAuthority(formName)
    }


}

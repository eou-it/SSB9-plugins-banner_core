/*******************************************************************************
 Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.security

import groovy.sql.Sql
import java.sql.SQLException
import java.util.regex.Pattern
import javax.sql.DataSource
import org.apache.log4j.Logger
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils
import org.springframework.security.core.GrantedAuthority

/**
 * A service used to determine and manipulate the user authorities
 * on the respective forms.
 *
 * This code was scattered through the sources in BannerAuthenticationProvider,
 * BannerAccessDecisionVoter, BannerDS, TabLevelSecurityService and even in banner_ui
 * plugin too. So encapsulated into a single class.
 *
 */
class UserAuthorityService {

    static transactional = false

    private final Logger log = Logger.getLogger(getClass())
    private static final Logger staticLogger = Logger.getLogger(UserAuthorityService.class)

    public static final def READONLY_PATTERN =  (~/DEFAULT_Q/ as Pattern)
    public static final def READ_WRITE_PATTERN = (~/DEFAULT_M/ as Pattern)

    public static final String READ_ONLY_ACCESS = "READ_ONLY"
    public static final String READ_WRITE_ACCESS = "READ_WRITE"
    public static final String UNDEFINED_ACCESS = "UNDEFINED"

    /**
     *
     * @param authentication
     * @param dataSource
     * @return
     */
    public static Collection<GrantedAuthority> determineAuthorities( Map authenticationResults, DataSource dataSource ) {

        def conn
        def db
        try {
            // We query the database for all role assignments for the user, using an unproxied connection.
            // The Banner roles are converted to an 'acegi friendly' format: e.g., ROLE_{FORM-OBJECT}_{BANNER_ROLE}
            conn = dataSource.unproxiedConnection
            db = new Sql( conn )
            return determineAuthorities( authenticationResults, db )
        } finally {
            conn?.close()
        }
    }


    /**
     * This is to pull the authorities from DB for the signed-in user.
     *
     * We query the database for all role assignments for the user, using an unproxied connection.
     * The Banner roles are converted to an 'acegi friendly' format: e.g., ROLE_{FORM-OBJECT}_{BANNER_ROLE}
     *
     * Entry conditions:-
     *      1. authorities to be determined for a signed-in user.
     *      2.
     *
     *
     * @param authentication
     * @param db
     * @return
     */
    private static Collection<GrantedAuthority> determineAuthorities (Map authenticationResults, Sql db) {

        def authorities = [] as Collection<GrantedAuthority>

        if (!authenticationResults['oracleUserName']) {
            return authorities   // empty list
        }

        try {
            String query = """
	                select GOVUROL_OBJECT, GOVUROL_ROLE from govurol,gubobjs
	                    where govurol_userid = ? and
	                        (govurol_object = gubobjs_name and (gubobjs_ui_version in ('A','C') OR gubobjs_name in ('GUAGMNU')) )"""
            db.eachRow( query, [authenticationResults['oracleUserName'].toUpperCase()] ) { row ->
                /**
                 * Performance Tuning - Removed Select * since fetching role password is very expensive.
                 * Password would be fetch on demand while applying the roles for the connection in BannerDS.
                 * Set the role password as null initially as we are no longer fetching it during login.
                 */
                authorities << BannerGrantedAuthority.create( row.GOVUROL_OBJECT, row.GOVUROL_ROLE, null )
            }
        } catch (SQLException e) {
            staticLogger.error "UserAuthorityService not able to determine Authorities for user ${authenticationResults['oracleUserName']} due to exception $e.message"
            return new ArrayList<GrantedAuthority>()
        }
        staticLogger.trace "UserAuthorityService.determineAuthorities is returning ${authorities?.size()} authorities. "
        authorities
    }

    /**
     *
     * Entry Conditions:-
     *      1. FormContext must be set already.
     *
     * @param grantedAuthorities
     * @return
     */
    public static def filterAuthorities (List<GrantedAuthority> grantedAuthorities) {
        if (!grantedAuthorities) {
            return []
        }
        List formContext = new ArrayList(FormContext.get())
        staticLogger.debug "UserAuthorityService has retrieved the FormContext value: $formContext"
        return filterAuthoritiesForFormNames(grantedAuthorities, formContext)
    }

    /**
     *  Entry Condition:
     *      1. User is signed-in and authentication object is created.
     *
     * @param formNames
     * @param authentication
     * @return
     */
    public static def filterAuthorities (formNames, authentication) {
        if (authentication.principal instanceof String) {
            return []
        }
        return filterAuthoritiesForFormNames(authentication.principal.authorities, formNames)
    }

    /**
     * Find matching authoritiese for each of the form names.
     *
     * @param grantedAuthorities
     * @param formNames
     * @return
     */
    private static List filterAuthoritiesForFormNames(def grantedAuthorities, List formNames) {
        List applicableAuthorities = grantedAuthorities.asList().grep { authorityHolder ->
            formNames.find { formName ->
                authorityHolder.authority ==~ getACEGICompatibleRolePattern(formName)
            }
        }
        staticLogger.debug "Given FormContext of ${formNames?.join(',')}, the user's applicable authorities are $applicableAuthorities"
        return applicableAuthorities
    }

    /**
     *
     * Resolve the authority for the given form name.
     *
     * Entry Condition :-
     *     1. Spring security authorities are already loaded.
     *
     * @param formName
     * @return  String value either of [READ_ONLY_ACCESS, READ_WRITE_ACCESS, UNDEFINED_ACCESS]
     */
    public static def resolveAuthority (formName) {
        def authority = getAuthority(formName, [(READONLY_PATTERN), (READ_WRITE_PATTERN)])
        if (authority) {
            return (isReadonlyPattern(authority))? READ_ONLY_ACCESS : ((isReadWritePattern(authority))?READ_WRITE_ACCESS :UNDEFINED_ACCESS)
        } else {
            return UNDEFINED_ACCESS
        }
    }

    /**
     * Get the authority for given form name for any of the matching patterns.
     *
     * Entry Condition:-
     *      1. Spring Security Authorities are already loaded.
     *
     * @param formName
     * @return
     */
    public static def getAuthority(String formName, List patternList) { // should get authorities from SpringSecurityUtils.getPrincipalAuthorities()
        SpringSecurityUtils.getPrincipalAuthorities().find { authority ->
            authority.objectName == formName && patternList.any{pattern -> pattern.matcher(authority.roleName)}
        }
    }

    public static boolean isReadWriteAccessLevel(String userAccessLevel) {
        return userAccessLevel == READ_WRITE_ACCESS
    }

    public static boolean isReadonlyAccessLevel(String userAccessLevel) {
        return userAccessLevel == READ_ONLY_ACCESS
    }

    public static boolean isUndefinedAccessLevel(String userAccessLevel) {
        return userAccessLevel == UNDEFINED_ACCESS
    }


    /**
     * Get the authority for given form name for a given pattern.
     *
     * @param formName
     * @param pattern
     * @return
     */
    public static def getAuthority (String formName, def pattern) {
        getAuthority (formName, [(pattern)])
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

    /**
     * checks if the given authority is a read-write pattern.
     *
     * @param authority
     * @return
     */
    public static boolean isReadWritePattern(GrantedAuthority authority) {
        READ_WRITE_PATTERN.matcher(authority.roleName)
    }

    /**
     * checks if the given authority is a readonly pattern.
     *
     * @param authority
     * @return
     */
    public static boolean isReadonlyPattern(GrantedAuthority authority) {
        READONLY_PATTERN.matcher(authority.roleName)
    }

}

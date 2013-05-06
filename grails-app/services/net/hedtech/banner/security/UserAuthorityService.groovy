/*******************************************************************************
 Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.security

import org.springframework.security.core.GrantedAuthority
import groovy.sql.Sql
import java.sql.SQLException
import javax.sql.DataSource
import org.apache.log4j.Logger
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils

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

    /**
     *
     * @param authentication
     * @param dataSource
     * @return
     */
    public static Collection<BannerGrantedAuthority> determineAuthorities( Map authenticationResults, DataSource dataSource ) {

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
    private static Collection<BannerGrantedAuthority> determineAuthorities (Map authenticationResults, Sql db) {

        def authorities = [] as Collection<BannerGrantedAuthority>

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
            return new ArrayList<BannerGrantedAuthority>()
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
    public static def filterAuthorities (List<BannerGrantedAuthority> grantedAuthorities) {
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

    public static List<BannerGrantedAuthority> filterAuthorities(BannerUser user) {
        List<BannerGrantedAuthority> applicableAuthorities = []
        List<BannerGrantedAuthority> authoritiesForForm
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
     * Find matching authoritiese for each of the form names.
     *
     * @param grantedAuthorities
     * @param formNames
     * @return
     */
    private static List filterAuthoritiesForFormNames(def grantedAuthorities, List formNames) {
        List applicableAuthorities = grantedAuthorities.asList().grep { authorityHolder ->
            formNames.find { formName ->
                final BannerGrantedAuthority authority = authorityHolder?.authority
                authority?.checkIfCompatibleWithACEGIRolePattern(formName)
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
    public static AccessPrivilegeType resolveAuthority (formName) {
        BannerGrantedAuthority authority = getAuthorityForAnyPattern(formName)
        authority?.getAccessPrivilegeType()
    }

    /**
     * Get authority for the given form matching any of the
     * patterns.
     *
     * @param formName
     * @return
     */
    public static BannerGrantedAuthority getAuthorityForAnyPattern(formName) {
        return getAuthority(formName, [(READONLY_PATTERN), (READ_WRITE_PATTERN)])
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
    public static BannerGrantedAuthority getAuthority(String formName, List patternList) { // should get authorities from SpringSecurityUtils.getPrincipalAuthorities()
        SpringSecurityUtils.getPrincipalAuthorities().find { authority ->
            if (authority) {
                authority.objectName == formName && patternList.any{pattern -> pattern.matcher(authority.roleName)}
            }
        }
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
     */
    public static boolean isReadWritePattern(String formName) {
        def authority = getAuthorityForAnyPattern (formName)
        authority?.isReadWriteAccess()

    }

    /**
     */
    public static boolean isReadonlyPattern(String formName) {
        BannerGrantedAuthority authority = getAuthorityForAnyPattern (formName)
        authority?.isReadOnlyAccess()
    }

}

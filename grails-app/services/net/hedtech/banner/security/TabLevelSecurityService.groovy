/*******************************************************************************
Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
*******************************************************************************/
package net.hedtech.banner.security

import groovy.sql.Sql

import org.apache.log4j.Logger

import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils
import java.util.regex.Pattern
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.GrantedAuthority
import net.hedtech.banner.ListManipulator

/**
 * A service used to support persistence of supplemental data.
 */
class TabLevelSecurityService {

    static transactional = true

    private final Logger log = Logger.getLogger(getClass())
    private static final Logger staticLogger = Logger.getLogger(TabLevelSecurityService.class)

    public static final String READ__ONLY_ACCESS = "READ_ONLY"
    public static final String READ__WRITE_ACCESS = "READ_WRITE"
    public static final String UNDEFINED_ACCESS = "UNDEFINED"

    def sessionFactory                     // injected by Spring

    /**
     * Method finds the user from the Spring Context. The role specified for
     * the user will be used to limit the tab-security-settings specified
     * for the form.
     *
     * Assumptions: 1. There must be a user signed-in.
     *             2. There must be a valid user role for this form.
     *
     * @param formName
     * @return the tab security settings. eg: [CURRICULUM:F,REGISTRATION:F,STUDENT:F,STUDY_PATH:Q,TIMESTAT:F]
     */
    public def getTabSecurityRestrictions (String formName) {
        if (!formName) {
            log.error("Error:- Form Name is required")
            throw new IllegalArgumentException("Error:- Form Name is required")
        }

        def dbConfiguredTabPrivilegeMap = [:]

        String userName = SecurityContextHolder.context.authentication?.user?.username

        if (!userName) {
            log.error("Error:- There must be a user signed-in")
            throw new IllegalArgumentException("Error:- There must be a user signed-in")
        }

        String userAccessLevel = getUserAccessLevel(userName, formName)

        if (! isUndefinedAccessLevel(userAccessLevel)) {
            dbConfiguredTabPrivilegeMap = getDBConfiguredTabSecurityRestrictions (userName, formName)
            return limitTabPrivilegesByUserAccessLevel(dbConfiguredTabPrivilegeMap, userAccessLevel)
        } else {
            // It is an impossible case. There must be an access level defined for the user to
            // access this form at the time when this method is being invoked.
        }
        return dbConfiguredTabPrivilegeMap
    }

    /**
     * Method depends on the authorities loaded on to the spring
     * security.
     *
     * @param userName
     * @param formName
     * @return
     */
    private String getUserAccessLevel(String userName, String formName) {
        def authorities = SpringSecurityUtils.getPrincipalAuthorities()
        def authority =  authorities?.find { authority ->
            (authority.objectName == formName && ( isReadonlyPattern(authority) || isReadWritePattern(authority)))
        }
        if (authority) {
            return (isReadonlyPattern(authority))? READ__ONLY_ACCESS : ((isReadWritePattern(authority))?READ__WRITE_ACCESS :UNDEFINED_ACCESS)
        } else {
            return UNDEFINED_ACCESS
        }
    }

    /**
     * This method pulls records from GURUTAB which are inserted through
     * GSASECR INB form by the security administrator.
     *
     * @param userName
     * @param formName
     * @return
     */
    private def getDBConfiguredTabSecurityRestrictions( String userName, String formName ) {
        Sql sql = new Sql(sessionFactory.getCurrentSession().connection())
        try {
            String strDbConfiguredTabPrivileges = ""
            sql.call("{$Sql.VARCHAR = call g\$_security.g\$_get_tab_security_fnc($formName, $userName)}") { accessString ->
                strDbConfiguredTabPrivileges = accessString
            }
            return getTabSecurityPrivilegeMap(strDbConfiguredTabPrivileges)
        } catch (e) {
            log.error(". ${e.message}")
            throw e
        } finally {
            sql?.close()
        }
    }

    /**
     * Method limits the db-configured tab security settings by the
     * user access level defined for the form.
     *
     * @param dbConfiguredTabPrivilegeMap
     * @param userAccessLevel
     * @return
     */
    private def limitTabPrivilegesByUserAccessLevel(dbConfiguredTabPrivilegeMap, userAccessLevel) {
        def revisedTabPrivileges = dbConfiguredTabPrivilegeMap
        if (isReadonlyAccessLevel(userAccessLevel)) {
            revisedTabPrivileges = lowerFullQueryAccessToReadonlyAccess (dbConfiguredTabPrivilegeMap)
        } else if (isReadWriteAccessLevel(userAccessLevel)) {
            // no limiting to be done here.
        } else {
            // An impossible case. Form should have an access level set for the user.
        }

        return revisedTabPrivileges
    }

    /**
     * Lowering the full-query-access settings to read-only access.
     *
     * @param dbConfiguredTabPrivilegeMap
     * @return
     */
    private def lowerFullQueryAccessToReadonlyAccess(dbConfiguredTabPrivilegeMap) {
        def revisedTabPrivileges =  [:]
        dbConfiguredTabPrivilegeMap?.each { key, value ->
            revisedTabPrivileges << (value == "F"?[(key):"Q"]:[(key):value])
        }
        return revisedTabPrivileges
    }


    private def getTabSecurityPrivilegeMap (String tabSecurityPrivilegeString) {
        return ListManipulator.stringRepresentationToMap (tabSecurityPrivilegeString, ":")
    }

    private boolean isReadWriteAccessLevel(String userAccessLevel) {
        return userAccessLevel == READ__WRITE_ACCESS
    }

    private boolean isReadonlyAccessLevel(String userAccessLevel) {
        return userAccessLevel == READ__ONLY_ACCESS
    }

    private boolean isUndefinedAccessLevel(String userAccessLevel) {
        return userAccessLevel == UNDEFINED_ACCESS
    }

    private boolean isReadWritePattern(GrantedAuthority authority) {
        (~/DEFAULT_M/ as Pattern).matcher(authority.roleName)
    }

    private boolean isReadonlyPattern(GrantedAuthority authority) {
        (~/DEFAULT_Q/ as Pattern).matcher(authority.roleName)
    }

}

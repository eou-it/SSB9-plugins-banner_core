/*******************************************************************************
Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
*******************************************************************************/
package net.hedtech.banner.security

import org.springframework.security.core.context.SecurityContextHolder

import groovy.sql.Sql
import net.hedtech.banner.ListManipulator
import org.apache.log4j.Logger

/**
 * Service to support the tab-level-security implementation.
 */
class TabLevelSecurityService {

    static transactional = true

    private final Logger log = Logger.getLogger(getClass())
    private static final Logger staticLogger = Logger.getLogger(TabLevelSecurityService.class)

    def sessionFactory                     // injected by Spring

    def bannerUserAuthorityService

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

        String userName = SecurityContextHolder.context.authentication?.user?.username?.toUpperCase()

        if (!userName) {
            log.error("Error:- There must be a user signed-in")
            throw new IllegalArgumentException("Error:- There must be a user signed-in")
        }

        AccessPrivilegeType accessPrivilegeType = bannerUserAuthorityService.getAccessPrivilegeType(formName)

        if (accessPrivilegeType != AccessPrivilegeType.UNDEFINED) {
            dbConfiguredTabPrivilegeMap = getDBConfiguredTabSecurityRestrictions (userName, formName)
            return limitTabPrivilegesByUserAccessLevel(dbConfiguredTabPrivilegeMap, accessPrivilegeType)
        } else {
            // It is an impossible case. There must be an access level defined for the user to
            // access this form at the time when this method is being invoked.
        }
        return dbConfiguredTabPrivilegeMap
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
    private def limitTabPrivilegesByUserAccessLevel(dbConfiguredTabPrivilegeMap, AccessPrivilegeType accessPrivilegeType) {
        def revisedTabPrivileges = dbConfiguredTabPrivilegeMap
        if (accessPrivilegeType == AccessPrivilegeType.READONLY) {
            revisedTabPrivileges = lowerFullQueryAccessToReadonlyAccess (dbConfiguredTabPrivilegeMap)
        } else if (accessPrivilegeType == AccessPrivilegeType.READWRITE) {
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

}

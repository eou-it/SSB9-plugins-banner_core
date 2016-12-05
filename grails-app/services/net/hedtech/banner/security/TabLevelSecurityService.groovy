/*******************************************************************************
Copyright 2009-2016 Ellucian Company L.P. and its affiliates.
*******************************************************************************/
package net.hedtech.banner.security

import net.hedtech.banner.SpringContextUtils
import org.springframework.security.core.context.SecurityContextHolder

import groovy.sql.Sql
import net.hedtech.banner.ListManipulator
import org.apache.log4j.Logger

/**
 * Service to support the tab-level-security implementation.
 */
class TabLevelSecurityService {

    static transactional = false
    public static final String FULL_ACCESS_TO_END_USER = "F"
    public static final String READONLY_ACCESS_TO_END_USER = "Q"

    private static final Logger log = Logger.getLogger(getClass())

    def sessionFactory                     // injected by Spring

    def bannerGrantedAuthorityService

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
        BannerGrantedAuthorityService bannerGrantedAuthorityService = SpringContextUtils.applicationContext.getBean('bannerCoreBannerGrantedAuthorityService')
        AccessPrivilege accessPrivilegeType = bannerGrantedAuthorityService.getAccessPrivilegeType(formName)

        if (accessPrivilegeType != AccessPrivilege.UNDEFINED) {
            dbConfiguredTabPrivilegeMap = getDBConfiguredTabSecurityRestrictions (userName, formName)
            return limitTabPrivilegesByUserAccessLevel(dbConfiguredTabPrivilegeMap, accessPrivilegeType)
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
    private Map<String, TabLevelSecurityEndUserAccess> getDBConfiguredTabSecurityRestrictions( String userName, String formName ) {
        Sql sql = new Sql(sessionFactory.getCurrentSession().connection())
        try {
            String strDbConfiguredTabPrivileges = ""
            sql.call("{$Sql.VARCHAR = call g\$_security.g\$_get_tab_security_fnc($formName, $userName)}") { accessString ->
                strDbConfiguredTabPrivileges = accessString
            }
            return getTabSecurityPrivilegeMap(strDbConfiguredTabPrivileges)
        } catch (Exception e) {
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
    private Map<String, TabLevelSecurityEndUserAccess> limitTabPrivilegesByUserAccessLevel(Map<String, TabLevelSecurityEndUserAccess> dbConfiguredTabPrivilegeMap, AccessPrivilege accessPrivilegeType) {
        def revisedTabPrivileges = dbConfiguredTabPrivilegeMap
        if (accessPrivilegeType == AccessPrivilege.READONLY) {
            revisedTabPrivileges = lowerFullQueryAccessToReadonlyAccess (dbConfiguredTabPrivilegeMap)
        }
        return revisedTabPrivileges
    }

    /**
     * Lowering the full-query-access settings to read-only access.
     *
     * @param dbConfiguredTabPrivilegeMap
     * @return
     */
    private Map<String, TabLevelSecurityEndUserAccess> lowerFullQueryAccessToReadonlyAccess(Map<String, TabLevelSecurityEndUserAccess> dbConfiguredTabPrivilegeMap) {
        Map<String, TabLevelSecurityEndUserAccess> revisedTabPrivileges =  [:]
        dbConfiguredTabPrivilegeMap?.each {String tabId, TabLevelSecurityEndUserAccess tabLevelSecurityAccessIndicator ->
            TabLevelSecurityEndUserAccess revisedPrivilege =
                (tabLevelSecurityAccessIndicator == (TabLevelSecurityEndUserAccess.FULL) ?
                    (TabLevelSecurityEndUserAccess.READONLY):tabLevelSecurityAccessIndicator)
            revisedTabPrivileges << [(tabId):(revisedPrivilege)]
        }
        return revisedTabPrivileges
    }

    private Map<String, TabLevelSecurityEndUserAccess> getTabSecurityPrivilegeMap (String tabSecurityPrivilegeString) {
        Map<String, TabLevelSecurityEndUserAccess> tabLevelSecurityPrivilegeMap = [:]

        final Map<String, String> tabAccessIndicatorStringCodeMap = ListManipulator.stringRepresentationToMap(tabSecurityPrivilegeString, ":")
        tabAccessIndicatorStringCodeMap?.each { String tabId, String accessIndicatorCode ->
            tabLevelSecurityPrivilegeMap[(tabId)] = TabLevelSecurityEndUserAccess.getTabLevelSecurityAccessIndicator(accessIndicatorCode)
        }
        return tabLevelSecurityPrivilegeMap
    }

}
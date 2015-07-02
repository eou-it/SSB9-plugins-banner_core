/*******************************************************************************
Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
*******************************************************************************/ 
package net.hedtech.banner.security

import groovy.sql.Sql
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.junit.After
import org.junit.Before
import org.junit.Test

import java.sql.SQLException

/**
 * Integration test for the self service Banner authentication provider.  
 **/
class TabLevelSecurityServiceIntegrationTests extends BaseIntegrationTestCase {

    public static final String FORM_READONLY_ACCESS_ROLE = "BAN_DEFAULT_Q"

    def tabLevelSecurityService

    def dataSource  // injected by Spring


    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()

    }
	
	@After
    public void tearDown() {
        super.tearDown()
    }

    @Test
    void testUserWithReadwritePermissionForTheForm() {
        Sql sql
        def url = ConfigurationHolder.config.bannerDataSource.url

        final EDITABLE_FORM_NAME = "SSARRES"
        final EDITABLE_USER_NAME = "GRAILS_USER"
        final PASSWORD = "u_pick_it"

        try {
            sql = Sql.newInstance(url,
                    "bansecr",
                    PASSWORD,
                    'oracle.jdbc.driver.OracleDriver')
            try{
                insertIntoGuratab (EDITABLE_FORM_NAME,
                        [
                                'tabScheduleRestrictionsDept':[TabLevelSecuritySecurityAdminAccess.ALL_PRIVILEGES.getCode(), 'Department'],
                                'tabScheduleRestrictionsMajor':[TabLevelSecuritySecurityAdminAccess.ALL_PRIVILEGES.getCode(), 'Field of Study'],
                                'tabScheduleRestrictionsClass':[TabLevelSecuritySecurityAdminAccess.ALL_PRIVILEGES.getCode(), 'Class and Level'],
                                'tabScheduleRestrictionsDegree':[TabLevelSecuritySecurityAdminAccess.ALL_PRIVILEGES.getCode(), 'Degree and Program'],
                                'tabScheduleRestrictionsCampus':[TabLevelSecuritySecurityAdminAccess.ALL_PRIVILEGES.getCode(), 'Campus and College'],
                                'tabScheduleRestrictionsAttr':[TabLevelSecuritySecurityAdminAccess.ALL_PRIVILEGES.getCode(), 'Student Attribute and Cohort']
                        ], sql)
            }catch (SQLException se){
                fail(" Scripts for Inserting Test Data into GURUTAB could not run successfully for the form - $EDITABLE_FORM_NAME")
            }
            try{
                insertIntoGuruobj(EDITABLE_FORM_NAME,"BAN_DEFAULT_M",EDITABLE_USER_NAME, sql)
            }catch (SQLException se){
                fail(" Scripts for Inserting Test Data into GURUOBJ could not run successfully for the form - - $EDITABLE_FORM_NAME")
            }
            try{
                insertIntoGurutab (EDITABLE_FORM_NAME, EDITABLE_USER_NAME,
                        [
                                'tabScheduleRestrictionsDept':TabLevelSecurityEndUserAccess.HIDDEN.getCode(),
                                'tabScheduleRestrictionsMajor':TabLevelSecurityEndUserAccess.HIDDEN.getCode(),
                                'tabScheduleRestrictionsCampus':TabLevelSecurityEndUserAccess.READONLY.getCode()
                        ], sql)
            }catch (SQLException se){
                fail(" Scripts for Inserting Test Data into GURATAB could not run successfully for the form - - $EDITABLE_FORM_NAME")
            }
            logout()

            login (EDITABLE_USER_NAME, PASSWORD)
            def s = tabLevelSecurityService.getTabSecurityRestrictions(EDITABLE_FORM_NAME)
            verifyTabLevelSecurityIndicators(s,
                    ['tabScheduleRestrictionsDept', 'tabScheduleRestrictionsMajor','tabScheduleRestrictionsClass','tabScheduleRestrictionsDegree','tabScheduleRestrictionsCampus','tabScheduleRestrictionsAttr'],
                    [TabLevelSecurityEndUserAccess.HIDDEN, TabLevelSecurityEndUserAccess.HIDDEN,
                            TabLevelSecurityEndUserAccess.FULL, TabLevelSecurityEndUserAccess.FULL,
                            TabLevelSecurityEndUserAccess.READONLY, TabLevelSecurityEndUserAccess.FULL])
            logout()

        } finally {
            deleteFromGurutab(EDITABLE_FORM_NAME,EDITABLE_USER_NAME, sql)
            deleteFromGuratab(EDITABLE_FORM_NAME, sql)
            deleteFromGuruobj(EDITABLE_FORM_NAME, EDITABLE_USER_NAME, sql)
            sql?.close() // note that the test will close the connection, since it's our current session's connection
        }
    }

    private def verifyTabLevelSecurityIndicators(s, tabIdentifiers, tabLevelSecurityAccessIndicators) {
        assertNotNull(s)
        assertEquals (6, s.size())

        assertEquals(tabLevelSecurityAccessIndicators[0], s[(tabIdentifiers[0])])
        assertEquals(tabLevelSecurityAccessIndicators[1], s[(tabIdentifiers[1])])
        assertEquals(tabLevelSecurityAccessIndicators[2], s[(tabIdentifiers[2])])
        assertEquals(tabLevelSecurityAccessIndicators[3], s[(tabIdentifiers[3])])
        assertEquals(tabLevelSecurityAccessIndicators[4], s[(tabIdentifiers[4])])
        assertEquals(tabLevelSecurityAccessIndicators[5], s[(tabIdentifiers[5])])
    }

    /**
     * Commented out as the banner-core-testapp CI do not have readonly user
     */
    /**
     * No Gurutab records, So no tab level security.
     */
    @Test
    void testUserWithReadonlyPermissionForTheForm() {

        Sql sql
        def url = ConfigurationHolder.config.bannerDataSource.url

        final READONLY_FORM_NAME = "SCACLBD"
        final READONLY_USER_NAME = "GRAILS_USER_READONLY"
        final PASSWORD = "u_pick_it"

        try {
            sql = Sql.newInstance(url,
                    "bansecr",
                    PASSWORD,
                    'oracle.jdbc.driver.OracleDriver')
            try {
                insertIntoGuratab (READONLY_FORM_NAME,
                        [
                                'tabScheduleRestrictionsDept':[TabLevelSecuritySecurityAdminAccess.ALL_PRIVILEGES.getCode(), 'Department'],
                                'tabScheduleRestrictionsMajor':[TabLevelSecuritySecurityAdminAccess.ALL_PRIVILEGES.getCode(), 'Field of Study'],
                                'tabScheduleRestrictionsClass':[TabLevelSecuritySecurityAdminAccess.ALL_PRIVILEGES.getCode(), 'Class and Level'],
                                'tabScheduleRestrictionsDegree':[TabLevelSecuritySecurityAdminAccess.ALL_PRIVILEGES.getCode(), 'Degree and Program'],
                                'tabScheduleRestrictionsCampus':[TabLevelSecuritySecurityAdminAccess.ALL_PRIVILEGES.getCode(), 'Campus and College'],
                                'tabScheduleRestrictionsAttr':[TabLevelSecuritySecurityAdminAccess.ALL_PRIVILEGES.getCode(), 'Student Attribute and Cohort']
                        ], sql)
            }catch (SQLException se){
                fail(" Scripts for Inserting Test Data into GURUTAB could not run successfully for the form - $READONLY_FORM_NAME")
            }
            try {
                insertIntoGuruobj(READONLY_FORM_NAME,FORM_READONLY_ACCESS_ROLE, READONLY_USER_NAME, sql)
            }catch (SQLException se){
                fail(" Scripts for Inserting Test Data into GURUOBJ could not run successfully for the form - - $READONLY_FORM_NAME")
            }
            try{
                insertIntoGurutab (READONLY_FORM_NAME, READONLY_USER_NAME,
                        ['tabScheduleRestrictionsDept':TabLevelSecurityEndUserAccess.READONLY.getCode(),
                                'tabScheduleRestrictionsMajor':TabLevelSecurityEndUserAccess.HIDDEN.getCode(),
                                'tabScheduleRestrictionsCampus':TabLevelSecurityEndUserAccess.READONLY.getCode()
                        ], sql)
            }catch (SQLException se){
                fail(" Scripts for Inserting Test Data into GURATAB could not run successfully for the form - - $READONLY_FORM_NAME")
            }
            logout()

            login (READONLY_USER_NAME, PASSWORD)
            def s1 = tabLevelSecurityService.getTabSecurityRestrictions(READONLY_FORM_NAME)
            verifyTabLevelSecurityIndicators(s1,
                                ['tabScheduleRestrictionsDept', 'tabScheduleRestrictionsMajor','tabScheduleRestrictionsClass','tabScheduleRestrictionsDegree','tabScheduleRestrictionsCampus','tabScheduleRestrictionsAttr'],
                                [TabLevelSecurityEndUserAccess.READONLY, TabLevelSecurityEndUserAccess.HIDDEN,
                                        TabLevelSecurityEndUserAccess.READONLY, TabLevelSecurityEndUserAccess.READONLY,
                                        TabLevelSecurityEndUserAccess.READONLY, TabLevelSecurityEndUserAccess.READONLY])
        } finally {
            deleteFromGurutab(READONLY_FORM_NAME,READONLY_USER_NAME, sql)
            deleteFromGuratab(READONLY_FORM_NAME, sql)
            deleteFromGuruobj(READONLY_FORM_NAME, READONLY_USER_NAME, sql)
            sql?.close() // note that the test will close the connection, since it's our current session's connection
        }

    }

    private deleteFromGuratab (String formName, Sql sql) {
        sql.executeUpdate("delete FROM GURATAB where GURATAB_FORM_NAME = ${formName}")
    }

    private deleteFromGurutab (String formName, String userName, Sql sql) {
        sql.executeUpdate("delete FROM GURUTAB where GURUTAB_FORM_NAME = '${formName}' and gurutab_class_or_user='${userName}'")
    }

    private insertIntoGuratab (String formName, tabPrivileges, Sql sql) {
        tabPrivileges?.each { tabId, tabDetails ->
            final String tabName = tabDetails[1]
            final String privilege = tabDetails[0]
            final query = "Insert into GURATAB (GURATAB_FORM_NAME,GURATAB_INTERNAL_TAB_NAME,GURATAB_EXTERNAL_TAB_NAME,GURATAB_RESTRICTIONS,GURATAB_SYS_REQ_IND, GURATAB_ACTIVITY_DATE,GURATAB_USER_ID,GURATAB_DATA_ORIGIN) values ('${formName}','${tabId}','${tabName}','${privilege}','Y',to_date('01-JAN-10','DD-MON-RR'),'GRAILS','GRAILS')"
            sql.executeUpdate(query.toString())
        }
    }

    private insertIntoGurutab (String formName, String userName, tabPrivileges, Sql sql) {
        tabPrivileges?.each { tabId, tabPrivilege ->
            final query = "Insert into GURUTAB (GURUTAB_FORM_NAME,GURUTAB_INTERNAL_TAB_NAME,GURUTAB_ACCESS,GURUTAB_CLASS_OR_USER,GURUTAB_ACTIVITY_DATE,GURUTAB_USER_ID,GURUTAB_DATA_ORIGIN) values ('${formName}','${tabId}','${tabPrivilege}','${userName}',to_date('19-SEP-07','DD-MON-RR'),'GRAILS','GRAILS')"
            sql.executeUpdate(query.toString())

        }
    }

    private insertIntoGuruobj (String formName, String access, String userName, Sql sql) {
        final query = "Insert into GURUOBJ (GURUOBJ_OBJECT,GURUOBJ_ROLE,GURUOBJ_USERID,GURUOBJ_ACTIVITY_DATE,GURUOBJ_USER_ID,GURUOBJ_COMMENTS,GURUOBJ_DATA_ORIGIN) values ('${formName}','${access}','${userName}',to_date('06-OCT-11','DD-MON-RR'),'BANSECR','TEST','BANNER')"
        sql.executeUpdate(query.toString())
    }

    private deleteFromGuruobj (String formName, userName, Sql sql) {
        sql.executeUpdate("delete FROM GURUOBJ where GURUOBJ_OBJECT = '${formName}' and GURUOBJ_USERID= '${userName}'")
    }


}



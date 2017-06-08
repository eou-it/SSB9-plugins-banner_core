/*******************************************************************************
 Copyright 2009-2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.security

import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
/**
 * Integration test for the self service Banner authentication provider.
 **/
// This TabLevelSecurityService is used in ZK Admin apps and its no longer supported . In near future this feature will be removed
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
        final EDITABLE_FORM_NAME = "SSARRES"
        final EDITABLE_USER_NAME = "GRAILS_USER"
        final PASSWORD = "u_pick_it"

        try {
            logout()
            login(EDITABLE_USER_NAME, PASSWORD)
            def s = tabLevelSecurityService.getTabSecurityRestrictions(EDITABLE_FORM_NAME)
            verifyTabLevelSecurityIndicators(s,
                    ['tabScheduleRestrictionsDept', 'tabScheduleRestrictionsMajor', 'tabScheduleRestrictionsClass', 'tabScheduleRestrictionsDegree', 'tabScheduleRestrictionsCampus', 'tabScheduleRestrictionsAttr'],
                    [TabLevelSecurityEndUserAccess.HIDDEN, TabLevelSecurityEndUserAccess.HIDDEN,
                     TabLevelSecurityEndUserAccess.FULL, TabLevelSecurityEndUserAccess.FULL,
                     TabLevelSecurityEndUserAccess.FULL, TabLevelSecurityEndUserAccess.FULL])
            logout()
        } catch (e) {
            throw e
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
        final READONLY_FORM_NAME = "SCACLBD"
        final READONLY_USER_NAME = "GRAILS_USER_READONLY"
        final PASSWORD = "u_pick_it"

        try {
            logout()
            login(READONLY_USER_NAME, PASSWORD)
            def s1 = tabLevelSecurityService.getTabSecurityRestrictions(READONLY_FORM_NAME)
            verifyTabLevelSecurityIndicators(s1,
                    ['tabScheduleRestrictionsDept', 'tabScheduleRestrictionsMajor', 'tabScheduleRestrictionsClass', 'tabScheduleRestrictionsDegree', 'tabScheduleRestrictionsCampus', 'tabScheduleRestrictionsAttr'],
                    [TabLevelSecurityEndUserAccess.READONLY, TabLevelSecurityEndUserAccess.HIDDEN,
                     TabLevelSecurityEndUserAccess.FULL, TabLevelSecurityEndUserAccess.FULL,
                     TabLevelSecurityEndUserAccess.READONLY, TabLevelSecurityEndUserAccess.FULL])
            logout()
        } catch (e) {
            throw e
        }

    }
}

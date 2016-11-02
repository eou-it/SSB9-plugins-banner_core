/*******************************************************************************
 Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

package net.hedtech.banner.general.utility

import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test


class PersonalPreferenceServiceIntegrationTests extends BaseIntegrationTestCase {

    def personalPreferenceService

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
    void testCreatePersonalPreference() {
        def personalPreference = newPersonalPreference()
        personalPreference = personalPreferenceService.create(personalPreference)
        assertNotNull "PersonalPreference ID is null in PersonalPreference Service Tests Create", personalPreference.id
        assertNotNull personalPreference.dataOrigin
        assertNotNull personalPreference.lastModifiedBy
        assertNotNull personalPreference.lastModified
    }

    @Test
    void testUpdate() {
        def personalPreference = newPersonalPreference()
        personalPreference = personalPreferenceService.create(personalPreference)
        // create new values for the fields
        def igroup = "XXXXX"
        def ikey = "XXXXX"
        def istring = "XXXXX"
        def ivalue = "XXXXX"
        def isystemRequiredIndicator = false
        // change the values
        personalPreference.group = igroup
        personalPreference.key = ikey
        personalPreference.string = istring
        personalPreference.value = ivalue
        personalPreference.systemRequiredIndicator = isystemRequiredIndicator
        personalPreference = personalPreferenceService.update(personalPreference)
        // test the values
        assertEquals igroup, personalPreference.group
        assertEquals ikey, personalPreference.key
        assertEquals istring, personalPreference.string
        assertEquals ivalue, personalPreference.value
        assertEquals isystemRequiredIndicator, personalPreference.systemRequiredIndicator
    }

    @Test
    void testPersonalPreferenceDelete() {
        def personalPreference = newPersonalPreference()
        personalPreference = personalPreferenceService.create(personalPreference)

        def id = personalPreference.id
        personalPreferenceService.delete(id)

        assertNull "PersonalPreference should have been deleted", personalPreference.get(id)
    }

    private def newPersonalPreference() {
        def personalPreference = new PersonalPreference(
                group: "TTTTT",
                key: "TTTTT",
                string: "TTTTT",
                value: "TTTTT",
                systemRequiredIndicator: true,

        )
        return personalPreference
    }

    @Test
    public void testFetchPersonalPreference() {
        testPersonalPreferenceFetch()
    }

    /**
     * Please put all the custom service tests in this protected section to protect the code
     * from being overwritten on re-generation
     */
    /*PROTECTED REGION ID(personalpreference_custom_service_integration_test_methods) ENABLED START*/
    void testPersonalPreferenceFetch() {
        def prefs = personalPreferenceService.fetchPersonalPreference("MENU", "WIN32COMMON", "STARTUP_MENU")
        assertNotNull prefs

        prefs = personalPreferenceService.fetchPersonalPreference("MENU_TEST", "WIN32COMMON_TEST", "STARTUP_MENU_TEST")
        assertTrue(prefs.empty)
    }
    /*PROTECTED REGION END*/
}

/*******************************************************************************
 Copyright 2016-2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

package net.hedtech.banner.general.utility

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import groovy.sql.Sql
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException

@Integration
@Rollback
class MenuAndToolbarPreferenceIntegrationTests extends BaseIntegrationTestCase {

    @Before
    public void setUp() {
        formContext = ['GUAGMNU'] // Since we are not testing a controller, we need to explicitly set this
        super.setUp()
    }

    @After
    public void tearDown() {
        super.tearDown()
    }

    @Test
    void testCreateMenuAndToolbarPreference() {
        MenuAndToolbarPreference menuAndToolbarPreference = newMenuAndToolbarPreference()
        save menuAndToolbarPreference
        //Test if the generated entity now has an id assigned
        assertNotNull menuAndToolbarPreference.id

        MenuAndToolbarPreference copyMenuAndToolbarPref = newMenuAndToolbarPreference()
        assertFalse(copyMenuAndToolbarPref.equals(menuAndToolbarPreference))

        copyMenuAndToolbarPref = copyMenuAndToolbarPref.get(menuAndToolbarPreference.id)
        assertTrue(copyMenuAndToolbarPref == menuAndToolbarPreference)

        MenuAndToolbarPreference copy = getNewenuAndToolbarPreference()
        assertFalse(copy == menuAndToolbarPreference)

        assertNotNull(menuAndToolbarPreference.toString())
        assertNotNull(menuAndToolbarPreference.hashCode())

        def test = new String()
        assertFalse(menuAndToolbarPreference.equals(test))
        assertFalse(menuAndToolbarPreference == null)

        copy.setId(menuAndToolbarPreference.id)
        copy.setLastModifiedBy(menuAndToolbarPreference.lastModifiedBy)
        copy.setTlbBtn(menuAndToolbarPreference.tlbBtn)
        copy.setDisplayHtCb(menuAndToolbarPreference.getDisplayHtCb())
        copy.setDisplayVtCb(menuAndToolbarPreference.displayVtCb)
        copy.setDisplayHint(menuAndToolbarPreference.displayHint)
        copy.setFormnameCb(menuAndToolbarPreference.formnameCb)
        copy.setReleaseCb(menuAndToolbarPreference.releaseCb)
        copy.setDbaseInstitutionCb(menuAndToolbarPreference.dbaseInstitutionCb)
        copy.setDateTimeCb(menuAndToolbarPreference.dateTimeCb)
        copy.setRequiredItemCb(menuAndToolbarPreference.requiredItemCb)
        copy.setLinescrnXPosition(menuAndToolbarPreference.linescrnXPosition)
        copy.setLinebtnXPosition(menuAndToolbarPreference.linebtnXPosition)
        copy.setFormnameDisplayIndicator(menuAndToolbarPreference.formnameDisplayIndicator)
        copy.setVersion(menuAndToolbarPreference.version)
        copy.setDataOrigin(menuAndToolbarPreference.dataOrigin)
        assertFalse(copy == menuAndToolbarPreference)

        copy.setLastModified(menuAndToolbarPreference.lastModified)
        assertTrue(copy == menuAndToolbarPreference)
    }

    @Test
    void testUpdateMenuAndToolbarPreference() {
        def menuAndToolbarPreference = newMenuAndToolbarPreference()
        save menuAndToolbarPreference

        assertNotNull menuAndToolbarPreference.id
        assertEquals 0L, menuAndToolbarPreference.version
        //  assertEquals "", menuAndToolbarPreference.tlbBtn
        assertEquals "T", menuAndToolbarPreference.displayHtCb
        assertEquals "T", menuAndToolbarPreference.displayVtCb
        assertEquals "T", menuAndToolbarPreference.displayHint
        assertEquals "T", menuAndToolbarPreference.formnameCb
        assertEquals "T", menuAndToolbarPreference.releaseCb
        assertEquals "T", menuAndToolbarPreference.dbaseInstitutionCb
        assertEquals "T", menuAndToolbarPreference.dateTimeCb
        assertEquals "T", menuAndToolbarPreference.requiredItemCb
        assertEquals "Y", menuAndToolbarPreference.formnameDisplayIndicator

        //Update the entity
        def testDate = new Date()
        menuAndToolbarPreference.tlbBtn = ""
        menuAndToolbarPreference.displayHtCb = "U"
        menuAndToolbarPreference.displayVtCb = "U"
        menuAndToolbarPreference.displayHint = "U"
        menuAndToolbarPreference.formnameCb = "U"
        menuAndToolbarPreference.releaseCb = "U"
        menuAndToolbarPreference.dbaseInstitutionCb = "U"
        menuAndToolbarPreference.dateTimeCb = "U"
        menuAndToolbarPreference.requiredItemCb = "U"
        menuAndToolbarPreference.linescrnXPosition = 0
        menuAndToolbarPreference.linebtnXPosition = 0
        menuAndToolbarPreference.formnameDisplayIndicator = "N"
        menuAndToolbarPreference.lastModified = testDate
        menuAndToolbarPreference.lastModifiedBy = "test"
        menuAndToolbarPreference.dataOrigin = "Banner"
        save menuAndToolbarPreference

        menuAndToolbarPreference = menuAndToolbarPreference.get(menuAndToolbarPreference.id)
        assertEquals 1L, menuAndToolbarPreference?.version
        assertEquals "", menuAndToolbarPreference.tlbBtn
        assertEquals "U", menuAndToolbarPreference.displayHtCb
        assertEquals "U", menuAndToolbarPreference.displayVtCb
        assertEquals "U", menuAndToolbarPreference.displayHint
        assertEquals "U", menuAndToolbarPreference.formnameCb
        assertEquals "U", menuAndToolbarPreference.releaseCb
        assertEquals "U", menuAndToolbarPreference.dbaseInstitutionCb
        assertEquals "U", menuAndToolbarPreference.dateTimeCb
        assertEquals "U", menuAndToolbarPreference.requiredItemCb
        assertEquals 0, menuAndToolbarPreference.linescrnXPosition
        assertEquals 0, menuAndToolbarPreference.linebtnXPosition
        assertEquals "N", menuAndToolbarPreference.formnameDisplayIndicator
    }

    @Test
    void testOptimisticLock() {
        def menuAndToolbarPreference = newMenuAndToolbarPreference()
        save menuAndToolbarPreference

        def sql
        try {
            sql = new Sql(sessionFactory.getCurrentSession().connection())
            sql.executeUpdate("update GURTPRF set GURTPRF_VERSION = 999 where GURTPRF_SURROGATE_ID = ?", [menuAndToolbarPreference.id])
        } finally {
            sql?.close() // note that the test will close the connection, since it's our current session's connection
        }
        //Try to update the entity
        menuAndToolbarPreference.tlbBtn = ""
        menuAndToolbarPreference.displayHtCb = "U"
        menuAndToolbarPreference.displayVtCb = "U"
        menuAndToolbarPreference.displayHint = "U"
        menuAndToolbarPreference.formnameCb = "U"
        menuAndToolbarPreference.releaseCb = "U"
        menuAndToolbarPreference.dbaseInstitutionCb = "U"
        menuAndToolbarPreference.dateTimeCb = "U"
        menuAndToolbarPreference.requiredItemCb = "U"
        menuAndToolbarPreference.linescrnXPosition = 0
        menuAndToolbarPreference.linebtnXPosition = 0
        menuAndToolbarPreference.formnameDisplayIndicator = "Y"
        menuAndToolbarPreference.lastModified = new Date()
        menuAndToolbarPreference.lastModifiedBy = "test"
        menuAndToolbarPreference.dataOrigin = "Banner"
        shouldFail(HibernateOptimisticLockingFailureException) {
            menuAndToolbarPreference.save(flush: true)
        }
    }

    @Test
    void testDeleteMenuAndToolbarPreference() {
        def menuAndToolbarPreference = newMenuAndToolbarPreference()
        save menuAndToolbarPreference
        def id = menuAndToolbarPreference.id
        assertNotNull id
        menuAndToolbarPreference.delete()
        assertNull menuAndToolbarPreference.get(id)
    }

    @Test
    void testValidation() {
        def menuAndToolbarPreference = newMenuAndToolbarPreference()
        assertTrue "MenuAndToolbarPreference could not be validated as expected due to ${menuAndToolbarPreference.errors}", menuAndToolbarPreference.validate()
    }

    @Test
    void testNullValidationFailure() {
        def menuAndToolbarPreference = new MenuAndToolbarPreference()
        assertNoErrorsFor menuAndToolbarPreference,
                [
                        'tlbBtn',
                        'displayHtCb',
                        'displayVtCb',
                        'displayHint',
                        'formnameCb',
                        'releaseCb',
                        'dbaseInstitutionCb',
                        'dateTimeCb',
                        'requiredItemCb',
                        'linescrnXPosition',
                        'linebtnXPosition',
                        'formnameDisplayIndicator'
                ]
    }

    @Test
    void testMaxSizeValidationFailures() {
        def menuAndToolbarPreference = new MenuAndToolbarPreference(
                tlbBtn: 'XX',
                displayHtCb: 'XXX',
                displayVtCb: 'XXX',
                displayHint: 'XXX',
                formnameCb: 'XXX',
                releaseCb: 'XXX',
                dbaseInstitutionCb: 'XXX',
                dateTimeCb: 'XXX',
                requiredItemCb: 'XXX')
        assertFalse "MenuAndToolbarPreference should have failed validation", menuAndToolbarPreference.validate()
        assertErrorsFor menuAndToolbarPreference, 'maxSize', ['tlbBtn', 'displayHtCb', 'displayVtCb', 'displayHint', 'formnameCb', 'releaseCb', 'dbaseInstitutionCb', 'dateTimeCb', 'requiredItemCb']
    }

    private def newMenuAndToolbarPreference() {
        def menuAndToolbarPreference = new MenuAndToolbarPreference(
                tlbBtn: "",
                displayHtCb: "T",
                displayVtCb: "T",
                displayHint: "T",
                formnameCb: "T",
                releaseCb: "T",
                dbaseInstitutionCb: "T",
                dateTimeCb: "T",
                requiredItemCb: "T",
                linescrnXPosition: 1,
                linebtnXPosition: 1,
                formnameDisplayIndicator: "Y",
                lastModified: new Date(),
                lastModifiedBy: "test",
                dataOrigin: "Banner"
        )
        return menuAndToolbarPreference
    }

    private def getNewenuAndToolbarPreference() {
        def menuAndToolbarPreference = new MenuAndToolbarPreference(
                tlbBtn: "",
                displayHtCb: "T1",
                displayVtCb: "T1",
                displayHint: "T1",
                formnameCb: "T1",
                releaseCb: "T1",
                dbaseInstitutionCb: "T1",
                dateTimeCb: "T1",
                requiredItemCb: "T1",
                linescrnXPosition: 11,
                linebtnXPosition: 11,
                formnameDisplayIndicator: "N",
                lastModified: new Date() - 1,
                lastModifiedBy: "test1",
                dataOrigin: "Banner1"
        )
        return menuAndToolbarPreference
    }

}

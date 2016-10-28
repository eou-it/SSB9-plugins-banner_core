
/*******************************************************************************
Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
*******************************************************************************/

package net.hedtech.banner.general.utility

import groovy.sql.Sql
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException

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
		def menuAndToolbarPreference = newMenuAndToolbarPreference()
		save menuAndToolbarPreference
		//Test if the generated entity now has an id assigned
        assertNotNull menuAndToolbarPreference.id

        def copyMenuAndToolbarPref = newMenuAndToolbarPreference()
        assertFalse(copyMenuAndToolbarPref.equals(menuAndToolbarPreference))

        copyMenuAndToolbarPref = copyMenuAndToolbarPref.get(menuAndToolbarPreference.id)
        assertTrue(copyMenuAndToolbarPref == menuAndToolbarPreference)

        assertFalse(menuAndToolbarPreference.equals(null))
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

        menuAndToolbarPreference = menuAndToolbarPreference.get( menuAndToolbarPreference.id )
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
            sql = new Sql( sessionFactory.getCurrentSession().connection() )
            sql.executeUpdate( "update GURTPRF set GURTPRF_VERSION = 999 where GURTPRF_SURROGATE_ID = ?", [ menuAndToolbarPreference.id ] )
        } finally {
            sql?.close() // note that the test will close the connection, since it's our current session's connection
        }
		//Try to update the entity
		menuAndToolbarPreference.tlbBtn=""
		menuAndToolbarPreference.displayHtCb="U"
		menuAndToolbarPreference.displayVtCb="U"
		menuAndToolbarPreference.displayHint="U"
		menuAndToolbarPreference.formnameCb="U"
		menuAndToolbarPreference.releaseCb="U"
		menuAndToolbarPreference.dbaseInstitutionCb="U"
		menuAndToolbarPreference.dateTimeCb="U"
		menuAndToolbarPreference.requiredItemCb="U"
		menuAndToolbarPreference.linescrnXPosition= 0
		menuAndToolbarPreference.linebtnXPosition= 0
		menuAndToolbarPreference.formnameDisplayIndicator= "Y"
		menuAndToolbarPreference.lastModified= new Date()
		menuAndToolbarPreference.lastModifiedBy="test"
		menuAndToolbarPreference.dataOrigin= "Banner"
        shouldFail( HibernateOptimisticLockingFailureException ) {
            menuAndToolbarPreference.save( flush: true )
        }
    }

    @Test
	void testDeleteMenuAndToolbarPreference() {
		def menuAndToolbarPreference = newMenuAndToolbarPreference()
		save menuAndToolbarPreference
		def id = menuAndToolbarPreference.id
		assertNotNull id
		menuAndToolbarPreference.delete()
		assertNull menuAndToolbarPreference.get( id )
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
        tlbBtn:'XX',
        displayHtCb:'XXX',
        displayVtCb:'XXX',
        displayHint:'XXX',
        formnameCb:'XXX',
        releaseCb:'XXX',
        dbaseInstitutionCb:'XXX',
        dateTimeCb:'XXX',
        requiredItemCb:'XXX' )
		assertFalse "MenuAndToolbarPreference should have failed validation", menuAndToolbarPreference.validate()
		assertErrorsFor menuAndToolbarPreference, 'maxSize', [ 'tlbBtn', 'displayHtCb', 'displayVtCb', 'displayHint', 'formnameCb', 'releaseCb', 'dbaseInstitutionCb', 'dateTimeCb', 'requiredItemCb' ]
    }

    @Test
	void testValidationMessages() {
	    def MenuAndToolbarPreference = newMenuAndToolbarPreference()
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
    		formnameDisplayIndicator:"Y",
            lastModified: new Date(),
			lastModifiedBy: "test",
			dataOrigin: "Banner"
        )
        return menuAndToolbarPreference
    }

}

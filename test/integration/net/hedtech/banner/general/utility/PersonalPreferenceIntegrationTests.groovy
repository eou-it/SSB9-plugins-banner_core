
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

class PersonalPreferenceIntegrationTests extends BaseIntegrationTestCase {

	def personalPreferenceService

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
	void testCreatePersonalPreference() {
		def personalPreference = newPersonalPreference()
		save personalPreference
		//Test if the generated entity now has an id assigned
        assertNotNull personalPreference.id
	}

    @Test
	void testUpdatePersonalPreference() {
		def personalPreference = newPersonalPreference()
		save personalPreference

        assertNotNull personalPreference.id
        assertEquals 0L, personalPreference.version
        assertEquals "TTTTT", personalPreference.group
        assertEquals "TTTTT", personalPreference.key
        assertEquals "TTTTT", personalPreference.string
        assertEquals "TTTTT", personalPreference.value
        assertTrue personalPreference.systemRequiredIndicator

		//Update the entity
		def testDate = new Date()
		personalPreference.group = "UUUUU"
		personalPreference.key = "UUUUU"
		personalPreference.string = "UUUUU"
		personalPreference.value = "UUUUU"
		personalPreference.systemRequiredIndicator = false
		personalPreference.lastModified = testDate
		personalPreference.lastModifiedBy = "test"
		personalPreference.dataOrigin = "Banner"
        save personalPreference

        personalPreference = PersonalPreference.get( personalPreference.id )
        assertEquals 1L, personalPreference?.version
        assertEquals "UUUUU", personalPreference.group
        assertEquals "UUUUU", personalPreference.key
        assertEquals "UUUUU", personalPreference.string
        assertEquals "UUUUU", personalPreference.value
        assertEquals false, personalPreference.systemRequiredIndicator
	}

    @Test
    void testOptimisticLock() {
		def personalPreference = newPersonalPreference()
		save personalPreference

        def sql
        try {
            sql = new Sql( sessionFactory.getCurrentSession().connection() )
            sql.executeUpdate( "update GURUPRF set GURUPRF_VERSION = 999 where GURUPRF_SURROGATE_ID = ?", [ personalPreference.id ] )
        } finally {
            sql?.close() // note that the test will close the connection, since it's our current session's connection
        }
		//Try to update the entity
		personalPreference.group="UUUUU"
		personalPreference.key="UUUUU"
		personalPreference.string="UUUUU"
		personalPreference.value="UUUUU"
		personalPreference.systemRequiredIndicator= false
		personalPreference.lastModified= new Date()
		personalPreference.lastModifiedBy="test"
		personalPreference.dataOrigin= "Banner"
        shouldFail( HibernateOptimisticLockingFailureException ) {
            personalPreference.save( flush: true )
        }
    }

    @Test
	void testDeletePersonalPreference() {
		def personalPreference = newPersonalPreference()
		save personalPreference
		def id = personalPreference.id
		assertNotNull id
		personalPreference.delete()
		assertNull PersonalPreference.get( id )
	}

    @Test
    void testValidation() {
       def personalPreference = newPersonalPreference()
       assertTrue "PersonalPreference could not be validated as expected due to ${personalPreference.errors}", personalPreference.validate()
    }

    @Test
    void testNullValidationFailure() {
        def personalPreference = new PersonalPreference()
        assertFalse "PersonalPreference should have failed validation", personalPreference.validate()
        assertErrorsFor personalPreference, 'nullable',
                                               [
                                                 'group',
                                                 'key',
                                                 'string'
                                               ]
        assertNoErrorsFor personalPreference,
        									   [
             									 'value',
             									 'systemRequiredIndicator'
											   ]
    }

    @Test
    void testMaxSizeValidationFailures() {
        def personalPreference = new PersonalPreference(
        value:'XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX' )
		assertFalse "PersonalPreference should have failed validation", personalPreference.validate()
		assertErrorsFor personalPreference, 'maxSize', [ 'value' ]
    }

    @Test
	void testValidationMessages() {
	    def personalPreference = newPersonalPreference()
	    personalPreference.group = null
	    assertFalse personalPreference.validate()
	   // assertLocalizedError personalPreference, 'nullable', /.*Field.*group.*of class.*PersonalPreference.*cannot be null.*/, 'group'
	    personalPreference.key = null
	    assertFalse personalPreference.validate()
	  //  assertLocalizedError personalPreference, 'nullable', /.*Field.*key.*of class.*PersonalPreference.*cannot be null.*/, 'key'
	    personalPreference.string = null
	    assertFalse personalPreference.validate()
	  //  assertLocalizedError personalPreference, 'nullable', /.*Field.*string.*of class.*PersonalPreference.*cannot be null.*/, 'string'
	}


  private def newPersonalPreference() {
    def personalPreference = new PersonalPreference(
    		group: "TTTTT",
    		key: "TTTTT",
    		string: "TTTTT",
    		value: "TTTTT",
    		systemRequiredIndicator: true,
            lastModified: new Date(),
			lastModifiedBy: "test",
			dataOrigin: "Banner"
        )
        return personalPreference
    }

   /**
     * Please put all the custom tests in this protected section to protect the code
     * from being overwritten on re-generation
     */
    /*PROTECTED REGION ID(personalpreference_custom_integration_test_methods) ENABLED START*/
    /*PROTECTED REGION END*/
}

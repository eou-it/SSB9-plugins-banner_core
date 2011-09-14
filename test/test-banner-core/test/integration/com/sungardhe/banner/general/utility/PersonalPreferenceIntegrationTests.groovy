
/*********************************************************************************
 Copyright 2009-2011 SunGard Higher Education. All Rights Reserved.
 This copyrighted software contains confidential and proprietary information of 
 SunGard Higher Education and its subsidiaries. Any use of this software is limited 
 solely to SunGard Higher Education licensees, and is further subject to the terms 
 and conditions of one or more written license agreements between SunGard Higher 
 Education and the licensee in question. SunGard is either a registered trademark or
 trademark of SunGard Data Systems in the U.S.A. and/or other regions and/or countries.
 Banner and Luminis are either registered trademarks or trademarks of SunGard Higher 
 Education in the U.S.A. and/or other regions and/or countries.
 **********************************************************************************/
 
package com.sungardhe.banner.general.utility

import com.sungardhe.banner.testing.BaseIntegrationTestCase 
import com.sungardhe.banner.general.utility.PersonalPreference

import grails.test.GrailsUnitTestCase
import groovy.sql.Sql 
import org.hibernate.annotations.OptimisticLock
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException


class PersonalPreferenceIntegrationTests extends BaseIntegrationTestCase {

	def personalPreferenceService
	
	protected void setUp() {
		formContext = ['GJAJPRF'] // Since we are not testing a controller, we need to explicitly set this
		super.setUp()
	}

	protected void tearDown() {
		super.tearDown()
	}

	void testCreatePersonalPreference() {
		def personalPreference = newPersonalPreference()
		save personalPreference
		//Test if the generated entity now has an id assigned		
        assertNotNull personalPreference.id
	}

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
	
	void testDeletePersonalPreference() {
		def personalPreference = newPersonalPreference()
		save personalPreference
		def id = personalPreference.id
		assertNotNull id
		personalPreference.delete()
		assertNull PersonalPreference.get( id )
	}
	
    void testValidation() {
       def personalPreference = newPersonalPreference()
       assertTrue "PersonalPreference could not be validated as expected due to ${personalPreference.errors}", personalPreference.validate()
    }

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
    
    void testMaxSizeValidationFailures() {
        def personalPreference = new PersonalPreference( 
        value:'XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX' )
		assertFalse "PersonalPreference should have failed validation", personalPreference.validate()
		assertErrorsFor personalPreference, 'maxSize', [ 'value' ]    
    }
    
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

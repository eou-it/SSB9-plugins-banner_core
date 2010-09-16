/** *****************************************************************************
 � 2010 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
package com.sungardhe.banner.supplemental

import java.util.Map;

import com.sungardhe.banner.testing.Zip
import com.sungardhe.banner.testing.Interest
import com.sungardhe.banner.testing.BaseIntegrationTestCase

import java.sql.Connection

import groovy.sql.Sql
import org.springframework.security.core.context.SecurityContextHolder
import org.codehaus.groovy.grails.commons.ConfigurationHolder

import com.sungardhe.banner.exceptions.ApplicationException

import org.junit.Ignore


/**
 * Integration tests of the supplemental data service.
 */
class SdeServiceIntegrationTests extends BaseIntegrationTestCase {

    def zipService                     // injected by Spring
    def supplementalDataService        // injected by Spring
    def sessionContext                 // injected by Spring


    protected void setUp() {
        formContext = ['STVCOLL']
        super.setUp()
		updateGORSDAVTable()
		
    }

    protected void tearDown() {
        super.tearDown()
    }

    /**
     * Tests that when a model is loaded by Hibernate, the 'SupplementalDataSupportListener' requests the
     * SupplementalDataService to load the supplemental data properties for the model.
     */
   void testCreateZip() {		
		def zip = new Zip( code: "TT", city: "TT")		
		
		zip = zipService.create( zip )
		assertNotNull zip.id
		assertEquals "TT", zip.code
		assertEquals "TT", zip.city
		
		assertEquals ConfigurationHolder.config?.dataOrigin, zip.dataOrigin
		assertEquals SecurityContextHolder.context?.authentication?.principal?.username, zip.lastModifiedBy
		assertNotNull zip.lastModified
    }
	
	
	/**
	 * Tests updating the entity.
	 */
	void testUpdateZip() { 
		def zip = new Zip( code: "TT", city: "TT")		

		zip = zipService.create( zip )
		assertNotNull zip.id
		
		zip.city = "new"
		
		def updatedZip = zipService.update( zip )
		assertEquals zip.id, updatedZip.id
        assertEquals "new", updatedZip.city 
	}
	
	
	/**
	 * Tests PL/SQL component integration.
	 */
	void testSdeLoad(){	
		
		def tableName = 'GTVZIPC'
		//def id = 25
		
		def id = Zip.findByCodeAndCity("00001","newcity").id
		
		Sql sql = new Sql(sessionFactory.getCurrentSession().connection())
		
		sql.call ("""
				declare
				l_pkey 	GORSDAV.GORSDAV_PK_PARENTTAB%TYPE;
				l_rowid VARCHAR2(18):= gfksjpa.f_get_row_id(${tableName},${id});

				  begin

				   l_pkey := gp_goksdif.f_get_pk(${tableName},l_rowid);
				   gp_goksdif.p_set_current_pk(l_pkey);

				 end;
        """
		)
		
		def session = sessionFactory.getCurrentSession()
		def resultSet = session.createSQLQuery("SELECT govsdav_attr_name, govsdav_value_as_char FROM govsdav WHERE govsdav_table_name= :tableName").setString("tableName", tableName).list()
		assertNotNull resultSet
		
		def returnList = []
		resultSet.each(){
			returnList.add([attributeName:"${it[0]}",
			                value:"${it[1]}"])
		}
		
		assertEquals 3, returnList.size()
		
        returnList.each { sdeEntry ->			
			println sdeEntry.attributeName
			println sdeEntry.value
         }
	}
	
	
	/**
	 * Tests loading the entity with SDE defined. (SDE data is not empty)
	 */	
   void testLoadNotEmptySdeData(){
	    assertTrue supplementalDataService.supportsSupplementalProperties( Zip )
	
        def found = Zip.findByCodeAndCity("00001","newcity")
	    assertTrue found?.hasSupplementalProperties()
	    assertEquals "comment 1", found.COMMENTS.value
	    assertEquals "comment 1", found.TEST.value
	    assertNull    found.NUMBER.value	
      }


	/**
	 * Tests loading the entity with SDE defined. (no SDE data)
	 */
	void testLoadEmptySdeData(){
		assertTrue supplementalDataService.supportsSupplementalProperties( Zip )
		
		def found = Zip.findByCodeAndCity("02186","Milton")

        assertTrue found?.hasSupplementalProperties()
		assertNull found.COMMENTS.value
		assertNull found.TEST.value
		assertNull found.NUMBER.value	
	}
	
	
	/**
	 * Tests loading the entity without SDE defined. 
	 */
	void testLoadWithoutSdeData(){
		assertFalse supplementalDataService.supportsSupplementalProperties( Interest )
		
		def found = Interest.findByCode("AH")
		assertFalse found?.hasSupplementalProperties()
	}
	
	
	/**
	 * Tests when SDE attributes are defined for the entity. 
	 * 1. SDE data already exists
	 * 2. Update SDE data for all attributes
	 */
	void testSaveNotEmptySdeData(){
		assertTrue supplementalDataService.supportsSupplementalProperties( Zip )
		
		def found = Zip.findByCodeAndCity("00001","newcity")
		assertTrue found?.hasSupplementalProperties()
		assertEquals "comment 1", found.COMMENTS.value
		assertEquals "comment 1", found.TEST.value
		assertNull    found.NUMBER.value	
		
		found.dataOrigin = "test"
		found.COMMENTS.value = "my comments"
		found.TEST.value = "my test"
		found.NUMBER.value = "10"
		
		save found
				
		def updatedSde = Zip.findByCodeAndCity("00001","newcity")
		assertEquals "my comments", updatedSde.COMMENTS.value
		assertEquals "my test", updatedSde.TEST.value
		assertEquals "10", updatedSde.NUMBER.value
		
	}
	
	/**
	 * Tests when SDE attributes are defined for the entity. 
	 * 1. SDE data already exists
	 * 2. Remove SDE data from the attribute
	 */
	void testSaveDeleteNotEmptySdeData(){
		assertTrue supplementalDataService.supportsSupplementalProperties( Zip )
		
		def found = Zip.findByCodeAndCity("00001","newcity")
		assertTrue found?.hasSupplementalProperties()
		assertEquals "comment 1", found.COMMENTS.value
		assertEquals "comment 1", found.TEST.value
		assertNull    found.NUMBER.value	
		
		found.dataOrigin = "foo"
		
		found.COMMENTS.value = null
		
		save found
		
		def updatedSde = Zip.findByCodeAndCity("00001","newcity")
		assertNull "my comments", updatedSde.COMMENTS.value	
	}
	
	/**
	 * Tests when SDE attributes are defined for the entity. 
	 * 1. No SDE data
	 * 2. Add SDE data to these attributes
	 */
	void testLoadAndCreateEmptySdeData(){
		assertTrue supplementalDataService.supportsSupplementalProperties( Zip )
		
		def found = Zip.findByCodeAndCity("02186","Milton")
		assertNull found.COMMENTS.value
		assertNull found.TEST.value
		assertNull found.NUMBER.value	
		
		found.dataOrigin = "foo"
		
		found.COMMENTS.value = "my comments"
		found.TEST.value = "my test"
		found.NUMBER.value = "10"
		
		save found
		
		def updatedSde = Zip.findByCodeAndCity("02186","Milton")
		assertEquals "my comments", updatedSde.COMMENTS.value
		assertEquals "my test", updatedSde.TEST.value
		assertEquals "10", updatedSde.NUMBER.value
	}
	
	/**
	 * Tests when SDE attributes are defined for the entity. 
	 * 1. Creates a new entity
	 * 1. No SDE data
	 * 2. Add SDE data to these attributes
	 */
	void testCreateNewSdeData() {		
		
		assertTrue supplementalDataService.supportsSupplementalProperties( Zip )
		
		def zip = new Zip( code: "TT", city: "TT")		
		
		zip = zipService.create( zip ) /// ?

        def zipFound = Zip.findByCodeAndCity("TT","TT")
		zipFound.refresh() // TO DO
       
		assertNotNull zipFound.id
		assertEquals "TT", zipFound.code
		assertEquals "TT", zipFound.city
		
		assertTrue zipFound?.hasSupplementalProperties()
		
		zipFound.dataOrigin = "foo"
		
		zipFound.COMMENTS.value = "my comments"
		zipFound.TEST.value = "my test"
		zipFound.NUMBER.value = "10"
		
		save zipFound
		
		def updatedSde = Zip.findByCodeAndCity("TT","TT")
		assertEquals "my comments", updatedSde.COMMENTS.value
		assertEquals "my test", updatedSde.TEST.value
		assertEquals "10", updatedSde.NUMBER.value
	}
	
	/**
	 * Tests when SDE attributes are defined for the entity. 
	 * 1. Creates a new entity
	 * 1. No SDE data
	 * 2. Add SDE data to these attributes with wrong Number format
	 */
	void testNumericValidationSdeData(){		
		
		assertTrue supplementalDataService.supportsSupplementalProperties( Zip )
		
		def zip = new Zip( code: "BB", city: "BB")		
		
		try {	
		
		zip = zipService.create( zip )
		
		def zipFound = Zip.findByCodeAndCity("BB","BB")
		zipFound.refresh() // TO DO
		
		assertTrue zipFound?.hasSupplementalProperties()
				
		zipFound.dataOrigin = "foo"
		zipFound.COMMENTS.value = "my comments"
		zipFound.TEST.value = "my test"
		zipFound.NUMBER.value = "test"
		
		zipService.update( zip )			
		fail("Should have received an error: Invalid Number" )
		}catch (e) {
		    println e
			if ( e.wrappedException.message == "Invalid Number" ){
				println "Found correct message."
			}else{ 
				fail( "Did not find expected Invalid Number message, found: ${e.wrappedException.message}") 
			}			
		}		
	}
	
	/**
	 * Tests when SDE attributes are defined for the entity. 
	 * 1. Creates a new entity
	 * 1. No SDE data
	 * 2. Add SDE data to these attributes with wrong Date format
	 */
	void testDateValidationSdeData(){		
		
		assertTrue supplementalDataService.supportsSupplementalProperties( Zip )
		
		def zip = new Zip( code: "BB", city: "BB")		
		
		try {				
			zip = zipService.create( zip )
			
			def zipFound = Zip.findByCodeAndCity("BB","BB")
			zipFound.refresh() // TO DO
			
			assertTrue zipFound?.hasSupplementalProperties()
				
			zipFound.dataOrigin = "foo"
			zipFound.COMMENTS.value = "my comments"
			zipFound.TEST.value = "my test"
			
			zipFound.NUMBER.dataType = "DATE" // forced Date
			zipFound.NUMBER.value = "15-Apr2010" // wrong format
			
			zipService.update( zip )			
			fail("Should have received an error: Invalid Date" )
		}catch (e) {
			println e
			if ( e.wrappedException.message == "Invalid Date" ){
				println "Found correct message."
			}else{ 
				fail( "Did not find expected Invalid Date message, found: ${e.wrappedException.message}") 
			}			
		}		
	}
	
	private def updateGORSDAVTable(){
		def sql
		try {
			sql = new Sql( sessionFactory.getCurrentSession().connection() )
			def rowCount = sql.executeUpdate( """delete gorsdav
					                             where gorsdav_table_name = 'GTVZIPC'
					                             and gorsdav_disc > 1
					                          """)
		} finally {
			sql?.close() // note that the test will close the connection, since it's our current session's connection
		}
	}
  }

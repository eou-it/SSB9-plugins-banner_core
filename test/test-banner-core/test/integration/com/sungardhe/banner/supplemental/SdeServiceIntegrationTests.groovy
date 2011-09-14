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
package com.sungardhe.banner.supplemental

import com.sungardhe.banner.testing.Zip
import com.sungardhe.banner.testing.Interest
import com.sungardhe.banner.testing.BaseIntegrationTestCase

import groovy.sql.Sql
import org.springframework.security.core.context.SecurityContextHolder
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import com.sungardhe.banner.exceptions.ApplicationException

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
        //insertUserDefinedAttrGTVZIPCTable()
        updateGorsdamTableValidation()
    }


    protected void tearDown() {
        super.tearDown()
    }

    /**
     * Tests that when a model is loaded by Hibernate, the 'SupplementalDataSupportListener' requests the
     * SupplementalDataService to load the supplemental data properties for the model.
     * */
    void testCreateZip() {
        def zip = new Zip(code: "TT", city: "TT")

        zip = zipService.create(zip)
        assertNotNull zip.id
        assertEquals "TT", zip.code
        assertEquals "TT", zip.city

        assertEquals ConfigurationHolder.config?.dataOrigin, zip.dataOrigin
        assertEquals SecurityContextHolder.context?.authentication?.principal?.username, zip.lastModifiedBy
        assertNotNull zip.lastModified
    }

    /**
     * Tests updating the entity.
     * */
    void testUpdateZip() {
        def zip = new Zip(code: "TT", city: "TT")

        zip = zipService.create(zip)
        assertNotNull zip.id
        zip.city = "new"

        def updatedZip = zipService.update(zip)
        assertEquals zip.id, updatedZip.id
        assertEquals "new", updatedZip.city
    }

    /**
     * Tests PL/SQL component integration.
     * */
    void testSdeLoad() {

        def tableName = 'GTVZIPC'
        def id = Zip.findByCodeAndCity("00001", "newcity").id

        Sql sql = new Sql(sessionFactory.getCurrentSession().connection())

        sql.call("""declare
				         l_pkey GORSDAV.GORSDAV_PK_PARENTTAB%TYPE;
				         l_rowid VARCHAR2(18):= gfksjpa.f_get_row_id(${tableName},${id});
				     begin
				         l_pkey := gp_goksdif.f_get_pk(${tableName},l_rowid);
				         gp_goksdif.p_set_current_pk(l_pkey);
				     end;
                  """)

        def session = sessionFactory.getCurrentSession()
        def resultSet = session.createSQLQuery("SELECT govsdav_attr_name, govsdav_value_as_char FROM govsdav WHERE govsdav_table_name= :tableName").setString("tableName", tableName).list()
        assertNotNull resultSet

        def returnList = []
        resultSet.each() {
            returnList.add([attributeName: "${it[0]}", value: "${it[1]}"])
        }

        assertTrue returnList.size() > 10 // 9 + 13 from stvlang table
    }

    /**
     * Tests loading the entity with SDE defined. (SDE data is not empty).
     * */
    void testLoadNotEmptySdeData() {
        assertTrue supplementalDataService.supportsSupplementalProperties(Zip)

        def found = Zip.findByCodeAndCity("00001", "newcity")

        assertTrue found?.hasSupplementalProperties()
        assertEquals "comment 1", found.COMMENTS."1".value
        assertEquals "comment 2", found.COMMENTS."2".value
        assertEquals "cmment 3", found.COMMENTS."3".value

        assertEquals "Enter a comment", found.COMMENTS."1".prompt
        assertEquals "Enter a comment", found.COMMENTS."2".prompt
        assertEquals "Enter a comment", found.COMMENTS."3".prompt

        assertEquals "Use record dulicate to add more records", found.COMMENTS."1".attrInfo
        assertEquals "Use record dulicate to add more records", found.COMMENTS."2".attrInfo
        assertEquals "Use record dulicate to add more records", found.COMMENTS."3".attrInfo

        assertEquals "VARCHAR2", found.COMMENTS."1".dataType
        assertEquals "M", found.COMMENTS."1".discType
        assertEquals 3, found.COMMENTS."1".validation
        assertEquals 1, found.COMMENTS."1".attrOrder

        assertEquals "comment 1", found.TEST."1".value
        assertEquals "comment 2", found.TEST."2".value
        assertEquals "comment 3", found.TEST."3".value

        assertEquals "Comment 1", found.TEST."1".prompt
        assertEquals "Comment 2", found.TEST."2".prompt
        assertEquals "Comment 3", found.TEST."3".prompt

        assertEquals "VARCHAR2", found.TEST."1".dataType
        assertEquals "M", found.TEST."1".discType
        assertEquals 6, found.TEST."1".validation
        assertNull found.TEST."1".attrInfo
        assertEquals 2, found.TEST."1".attrOrder


        assertNull found.NUMBER."1".value
        assertEquals "enter a numbere", found.NUMBER."1".prompt

        assertEquals "NUMBER", found.NUMBER."1".dataType
        assertEquals "S", found.NUMBER."1".discType
        assertEquals 1, found.NUMBER."1".validation
        assertEquals 6, found.NUMBER."1".dataLength
        assertEquals 2, found.NUMBER."1".dataScale
        assertEquals "with 2 decimal points", found.NUMBER."1".attrInfo
        assertEquals 3, found.NUMBER."1".attrOrder


        assertEquals 5, found.supplementalPropertyNames().size()
        assertTrue 'TEST' in found.supplementalPropertyNames()
        assertTrue 'NUMBER' in found.supplementalPropertyNames()
        assertTrue 'COMMENTS' in found.supplementalPropertyNames()
        assertTrue 'USERDEFINED' in found.supplementalPropertyNames()
        assertTrue 'LANGUAGE' in found.supplementalPropertyNames()

        assertEquals 3, found.getSupplementalProperties()."TEST".size()
        assertEquals 3, found.getSupplementalProperties()."COMMENTS".size()
        assertEquals 1, found.getSupplementalProperties()."NUMBER".size()
        assertEquals 2, found.getSupplementalProperties()."USERDEFINED".size()
        assertTrue found.getSupplementalProperties()."LANGUAGE".size() > 2
    }

    /**
     * Tests loading the entity with SDE defined. (no SDE data)
     * */
    void testLoadEmptySdeData() {
        assertTrue supplementalDataService.supportsSupplementalProperties(Zip)
        def found = Zip.findByCodeAndCity("02186", "Milton")

        assertTrue found?.hasSupplementalProperties()
        assertNull found.COMMENTS."1".value
        assertNull found.TEST."1".value
        assertNull found.NUMBER."1".value
    }

    /**
     * Tests loading the entity without SDE defined.
     * */
    void testLoadWithoutSdeData() {
        assertFalse supplementalDataService.supportsSupplementalProperties(Interest)

        def found = Interest.findByCode("AH")
        assertFalse found?.hasSupplementalProperties()
    }

    /**
     * Tests when SDE attributes are defined for the entity.
     * 1. SDE data already exists
     * 2. Update SDE data for all attributes
     * */
    void testSaveNotEmptySdeData() {
        assertTrue supplementalDataService.supportsSupplementalProperties(Zip)

        def found = Zip.findByCodeAndCity("00001", "newcity")
        assertTrue found?.hasSupplementalProperties()
        assertEquals "comment 1", found.COMMENTS."1".value
        assertEquals "comment 1", found.TEST."1".value
        assertNull found.NUMBER."1".value

        found.dataOrigin = "test"
        found.COMMENTS."1".value = "my comments"
        found.TEST."1".value = "my test"
        found.NUMBER."1".value = "10"

        def zip = zipService.update(found)
        assertEquals "my comments", zip.COMMENTS."1".value
        assertEquals "my test", zip.TEST."1".value
        assertEquals "10", zip.NUMBER."1".value

        def updatedSde = Zip.findByCodeAndCity("00001", "newcity")
        updatedSde.refresh() // not needed normally, but used to ensure better testing...
        assertEquals "my comments", updatedSde.COMMENTS."1".value
        assertEquals "my test", updatedSde.TEST."1".value
        assertEquals "10", updatedSde.NUMBER."1".value
    }

    /**
     * Tests when SDE attributes are defined for the entity.
     * 1. SDE data already exists
     * 2. Remove SDE data from the attribute
     * */
    void testSaveDeleteNotEmptySdeData() {
        assertTrue supplementalDataService.supportsSupplementalProperties(Zip)

        def found = Zip.findByCodeAndCity("00001", "newcity")
        assertTrue found?.hasSupplementalProperties()
        assertEquals "comment 1", found.COMMENTS."1".value
        assertEquals "comment 1", found.TEST."1".value
        assertNull found.NUMBER."1".value
        assertEquals 3, found.COMMENTS.size()

        found.COMMENTS."1".value = null
        def zip = zipService.update(found)
        assertEquals 2, zip.COMMENTS.size()

        def updatedSde = Zip.findByCodeAndCity("00001", "newcity")
        updatedSde.refresh() // not needed normally, but used to ensure better testing...
        assertEquals 2, zip.COMMENTS.size()
    }

    /**
     * Tests when SDE attributes are defined for the entity.
     * 1. SDE data already exists
     * 2. Remove SDE data from the attribute
     */
    void testSaveDeleteNotEmptySdeDataInTheMiddle() {
        assertTrue supplementalDataService.supportsSupplementalProperties(Zip)

        def found = Zip.findByCodeAndCity("00001", "newcity")
        assertTrue found?.hasSupplementalProperties()
        assertEquals "comment 2", found.COMMENTS."2".value   // in the middle
        assertEquals "comment 1", found.TEST."1".value
        assertNull found.NUMBER."1".value

        found.COMMENTS."2".value = null
        def zip = zipService.update(found)
        assertEquals 2, zip.COMMENTS.size()

        def updatedSde = Zip.findByCodeAndCity("00001", "newcity")

        def zipFound = Zip.findByCodeAndCity("00001", "newcity")
        zipFound.refresh() // not needed normally, but used to ensure better testing...

        assertNotNull zipFound.COMMENTS."2".value   // rebuilt discriminator
        assertEquals "cmment 3", zipFound.COMMENTS."2".value

        assertNull zipFound.COMMENTS."3"
    }

    /**
     * Tests when SDE attributes are defined for the entity.
     * 1. No SDE data
     * 2. Add SDE data to these attributes
     * */
    void testLoadAndCreateEmptySdeData() {
        assertTrue supplementalDataService.supportsSupplementalProperties(Zip)

        def found = Zip.findByCodeAndCity("02186", "Milton")
        assertNull found.COMMENTS."1".value
        assertNull found.TEST."1".value
        assertNull found.NUMBER."1".value

        found.dataOrigin = "foo"
        found.COMMENTS."1".value = "my comments"
        found.TEST."1".value = "my test"
        found.NUMBER."1".value = "10"

        def zip = zipService.update(found)
        assertEquals "my comments", zip.COMMENTS."1".value
        assertEquals "my test", zip.TEST."1".value
        assertEquals "10", zip.NUMBER."1".value

        def updatedSde = Zip.findByCodeAndCity("02186", "Milton")
        updatedSde.refresh() // not needed normally, but used to ensure better testing...
        assertEquals "my comments", updatedSde.COMMENTS."1".value
        assertEquals "my test", updatedSde.TEST."1".value
        assertEquals "10", updatedSde.NUMBER."1".value
    }

    /**
     * Tests when SDE attributes are defined for the entity.
     * 1. Creates a new entity
     * 1. No SDE data
     * 2. Add SDE data to these attributes
     * */
    void testCreateNewSdeData() {

        assertTrue supplementalDataService.supportsSupplementalProperties(Zip)

        def zip = zipService.create(new Zip(code: "TT", city: "TT"))
        assertNotNull zip.id
        assertEquals "TT", zip.code
        assertEquals "TT", zip.city
        assertTrue zip?.hasSupplementalProperties()

        zip.COMMENTS."1".value = "my comments"
        zip.TEST."1".value = "my test"
        zip.NUMBER."1".value = "10"

        // add a second discriminated value for TEST
        zip.TEST."2" = [required: zip.TEST."1".required, value: "my test1", disc: "2",
                pkParentTab: zip.TEST."1".pkParentTab, id: zip.TEST."1".id,
                dataType: zip.TEST."1".dataType]

        // and a third discriminator value
        zip.TEST."3" = [required: zip.TEST."1".required, value: null, disc: "3",
                pkParentTab: zip.TEST."1".pkParentTab, id: zip.TEST."1".id,
                dataType: zip.TEST."1".dataType]

        // here we show adding a value via a setter...
        zip.TEST."3".value = "my test3"

        zipService.update(zip)
        def updatedSde = Zip.findByCodeAndCity("TT", "TT")
        assertEquals "my comments", updatedSde.COMMENTS."1".value
        assertEquals "my test", updatedSde.TEST."1".value
        assertEquals "my test1", updatedSde.TEST."2".value
        assertEquals "my test3", updatedSde.TEST."3".value
        assertEquals "10", updatedSde.NUMBER."1".value
    }

    /**
     * Tests when SDE attributes are defined for the entity.
     * 1. Creates a new entity
     * 1. No SDE data
     * 2. Add SDE data to these attributes with wrong Number format
     * */
    void testNumericValidationSdeData() {

        assertTrue supplementalDataService.supportsSupplementalProperties(Zip)

        def zip = new Zip(code: "BB", city: "BB")

        try {
            zip = zipService.create(zip)
            def zipFound = Zip.findByCodeAndCity("BB", "BB")

            assertTrue zipFound?.hasSupplementalProperties()

            zipFound.dataOrigin = "foo"
            zipFound.COMMENTS."1".value = "my comments"
            zipFound.TEST."1".value = "my test"
            zipFound.NUMBER."1".value = "test"

            zipService.update(zip)
            fail("Should have received an error: Invalid Number")
        }
        catch (ApplicationException e) {
            assertEquals "Invalid Number", e.wrappedException.message
        }
    }

    /**
     * Tests when SDE attributes are defined for the entity.
     * 1. Creates a new entity
     * 1. No SDE data
     * 2. Add SDE data to these attributes with wrong Date format
     * */
    void testDateValidationSdeData() {

        assertTrue supplementalDataService.supportsSupplementalProperties(Zip)

        def zip = new Zip(code: "BB", city: "BB")

        try {
            zip = zipService.create(zip)
            def zipFound = Zip.findByCodeAndCity("BB", "BB")
            assertTrue zipFound?.hasSupplementalProperties()

            zipFound.dataOrigin = "foo"
            zipFound.COMMENTS."1".value = "my comments"
            zipFound.TEST."1".value = "my test"

            zipFound.NUMBER."1".dataType = "DATE" // forced Date
            zipFound.NUMBER."1".value = "15-Apr2010" // wrong format

            zipService.update(zip)
            fail("Should have received an error: Invalid Date")
        }
        catch (ApplicationException e) {
            assertEquals "Invalid Date", e.wrappedException.message
        }
    }

    /**
     * Tests when the block is SDE enabled
     * */
    void testIsSde() {
        def isSde = supplementalDataService.hasSde("zipBlock")
        assertTrue isSde

        def isSde1 = supplementalDataService.hasSde("fooBlock")
        assertFalse isSde1

        def isSde2 = supplementalDataService.hasSde("zip")
        assertFalse isSde2

        def isSde3 = supplementalDataService.hasSde("studentBlock")
        assertFalse isSde3

        //def isSde4 =  supplementalDataService.hasSde("courseLaborDistributionBlock")
        //assertTrue isSde4
    }



    void testLoadUseDefinedSdeData() {
        assertTrue supplementalDataService.supportsSupplementalProperties(Zip)

        def found = Zip.findByCodeAndCity("00001", "newcity")
        assertNotNull found.USERDEFINED
        assertNotNull found.USERDEFINED."name"
        assertNotNull found.USERDEFINED."phone"

        assertEquals "User Defined name", found.USERDEFINED."name".prompt
        assertEquals "User Defined phone", found.USERDEFINED."phone".prompt

        // adds new values for user-defined attributes
        found.USERDEFINED."name".value = "my name"
        found.USERDEFINED."phone".value = "1234"

        zipService.update(found)
        def updatedSde = Zip.findByCodeAndCity("00001", "newcity")
        updatedSde.refresh()
        assertEquals "my name", updatedSde.USERDEFINED."name".value
        assertEquals "1234", updatedSde.USERDEFINED."phone".value

        // deletes values for user-defined attributes
        found.USERDEFINED."name".value = null
        found.USERDEFINED."phone".value = null

        zipService.update(found)
        def deletedSde = Zip.findByCodeAndCity("00001", "newcity")
        deletedSde.refresh()
        assertNull updatedSde.USERDEFINED."name".value
        assertNull updatedSde.USERDEFINED."phone".value
    }


    void testLoadSQLBasedAttributeSdeData() {
        assertTrue supplementalDataService.supportsSupplementalProperties(Zip)

        def found = Zip.findByCodeAndCity("00001", "newcity")
        assertNotNull found.LANGUAGE
        assertEquals "Language", found.LANGUAGE."ENG".prompt
        assertEquals "Language", found.LANGUAGE."RUS".prompt
        assertEquals "Language", found.LANGUAGE."GRM".prompt


        found.LANGUAGE."GRM".value = "Munchen"

        zipService.update(found)
        def updatedSde = Zip.findByCodeAndCity("00001", "newcity")
        updatedSde.refresh()
        assertEquals "Munchen", updatedSde.LANGUAGE."GRM".value

        // deletes values for user-defined attributes
        found.LANGUAGE."GRM".value = null

        zipService.update(found)
        def deletedSde = Zip.findByCodeAndCity("00001", "newcity")
        deletedSde.refresh()
        assertNull updatedSde.LANGUAGE."GRM".value

    }

    void testValidationSDE() {
        def found = Zip.findByCodeAndCity("00001", "newcity")

        found.dataOrigin = "test"
        found.COMMENTS."1".value = "my comments"
        found.TEST."1".value = "my test"
        found.NUMBER."1".value = "105666"

        try {
            def zip = zipService.update(found)
            fail "This should have failed"
        }
        catch (ApplicationException ae) {
            if (ae.wrappedException =~ /\*Error\* Invalid Number. Expected format: 999D99/)
                println "Found correct message code *Error* Invalid Number. Expected format: 999D99"
            else
                fail("Did not find expected error code *Error* Invalid Number. Expected format: 999D99, found: ${ae.wrappedException}")
        }
    }


    void testValidationLov() {

        updateGorsdamTableLov()

        def found = Zip.findByCodeAndCity("00001", "newcity")

        found.dataOrigin = "test"
        found.COMMENTS."1".value = "1234"
        found.TEST."1".value = "my test"
        found.NUMBER."1".value = "10"

        try {
            def zip = zipService.update(found)
            fail "This should have failed"
        }
        catch (ApplicationException ae) {
            if (ae.wrappedException =~ /\*Error\* Value 1234 not found in validation table STVTERM./)
                println "Found correct message code *Error* Value 1234 not found in validation table STVTERM."
            else
                fail("Did not find expected error code *Error* Value 1234 not found in validation table STVTERM., found: ${ae.wrappedException}")
        }
    }



    private def updateGORSDAVTable() {
        def sql
        try {
            sql = new Sql(sessionFactory.getCurrentSession().connection())
            sql.executeUpdate("delete gorsdav where gorsdav_table_name = 'GTVZIPC' and gorsdav_disc > 1")
        }
        finally {
            sql?.close()  // note that the test will close the connection, since it's our current session's connection
        }
    }

    private def insertUserDefinedAttrGTVZIPCTable() {
        def sql
        try {
            sql = new Sql(sessionFactory.getCurrentSession().connection())
            sql.executeUpdate("""
            INSERT INTO GORSDAM (GORSDAM_TABLE_NAME, GORSDAM_ATTR_NAME ,
                                 GORSDAM_ATTR_TYPE, GORSDAM_ATTR_ORDER,
                                 GORSDAM_ATTR_REQD_IND, GORSDAM_ATTR_DATA_TYPE,
                                 GORSDAM_ATTR_PROMPT, GORSDAM_ACTIVITY_DATE,
                                 GORSDAM_USER_ID,GORSDAM_SDDC_CODE ) values
            ('GTVZIPC', 'USERDEFINED','A',4,'Y','VARCHAR2','User Defined %DISC%',sysdate,user,'cyndy3')
            """)
        }
        finally {
            sql?.close()  // note that the test will close the connection, since it's our current session's connection
        }
    }


    private def updateGorsdamTableValidation() {
        def sql
        try {
            sql = new Sql(sessionFactory.getCurrentSession().connection())
            sql.executeUpdate("""
            UPDATE GORSDAM
              SET GORSDAM_ATTR_DATA_LEN = 6,
                  GORSDAM_ATTR_DATA_SCALE = 2
            WHERE GORSDAM_TABLE_NAME = 'GTVZIPC'
              AND GORSDAM_ATTR_NAME = 'NUMBER'
            """)
        }
        finally {
            sql?.close()  // note that the test will close the connection, since it's our current session's connection
        }
    }

    private def updateGorsdamTableLov() {
        def sql
        try {
            sql = new Sql(sessionFactory.getCurrentSession().connection())
            sql.executeUpdate("""
                 UPDATE GORSDAM
                SET GORSDAM_LOV_FORM = 'STVTERM',
                    GORSDAM_GJAPDEF_VALIDATION = 'LOV_VALIDATION',
                    GORSDAM_ATTR_DATA_LEN = 20,
                    GORSDAM_LOV_LOW_SYSDATE_IND = 'N',
                    GORSDAM_LOV_HIGH_SYSDATE_IND = 'N'
              WHERE GORSDAM_TABLE_NAME = 'GTVZIPC'
                AND GORSDAM_ATTR_NAME = 'COMMENTS'
            """)
        }
        finally {
            sql?.close()  // note that the test will close the connection, since it's our current session's connection
        }
    }
}

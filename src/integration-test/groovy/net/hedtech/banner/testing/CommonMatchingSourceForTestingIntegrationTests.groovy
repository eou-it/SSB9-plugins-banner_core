/** *****************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.testing

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import org.junit.After
import org.junit.Before
import org.junit.Test

@Integration
@Rollback
class CommonMatchingSourceForTestingIntegrationTests extends BaseIntegrationTestCase{

    CommonMatchingSourceForTesting commonMatchingSourceForTesting
    CommonMatchingSourceForTesting commonMatchingSourceForTesting1

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        commonMatchingSourceForTesting=new CommonMatchingSourceForTesting()
        commonMatchingSourceForTesting1=new CommonMatchingSourceForTesting()
    }


    @After
    public void tearDown() {
        super.tearDown()
        commonMatchingSourceForTesting=null
        commonMatchingSourceForTesting1=null
    }

    @Test
    void testCommonMatchingSource() {
        assertNotNull commonMatchingSourceForTesting.toString()
    }

    @Test
    void testCommonMatchingSourceWithEquals() {
        assertTrue commonMatchingSourceForTesting1 == commonMatchingSourceForTesting

    }


    @Test
    void testCommonMatchingSourceWithEqualsID() {
        commonMatchingSourceForTesting.setId(1234)
        commonMatchingSourceForTesting1.setId(3456)
        assertFalse commonMatchingSourceForTesting == commonMatchingSourceForTesting1
    }


    @Test
    void testCommonMatchingSourceWithEqualsVersion() {
        commonMatchingSourceForTesting.setVersion(1)
        commonMatchingSourceForTesting1.setVersion(2)
        assertFalse commonMatchingSourceForTesting == commonMatchingSourceForTesting1

    }


    @Test
    void testCommonMatchingSourceWithEqualsDescription() {
        commonMatchingSourceForTesting.setDescription("termTypeDescription")
        commonMatchingSourceForTesting1.setDescription("termTypeDescription1")
        assertFalse commonMatchingSourceForTesting == commonMatchingSourceForTesting1

    }

    @Test
    void testCommonMatchingSourceWithEqualsLongDescription() {
        commonMatchingSourceForTesting.setLongDescription("commonMatchingDescription")
        commonMatchingSourceForTesting1.setLongDescription("commonMatchingDescription1")
        assertFalse commonMatchingSourceForTesting == commonMatchingSourceForTesting1
    }


    @Test
    void testCommonMatchingSourceWithEqualsLastModified() {
        commonMatchingSourceForTesting.setLastModified(new Date(12,2,15))
        commonMatchingSourceForTesting1.setLastModified(new Date(12,2,12))
        assertFalse commonMatchingSourceForTesting == commonMatchingSourceForTesting1
    }


    @Test
    void testCommonMatchingSourceWithEqualsCode() {
        commonMatchingSourceForTesting.setCode("code")
        commonMatchingSourceForTesting1.setCode("code1")
        assertFalse commonMatchingSourceForTesting == commonMatchingSourceForTesting1
    }


    @Test
    void testCommonMatchingSourceWithEqualsLastModifiedBy() {
        commonMatchingSourceForTesting.setLastModifiedBy("termTypeLastModifiedBy")
        commonMatchingSourceForTesting1.setLastModifiedBy("termTypeLastModifiedBy2")
        assertFalse commonMatchingSourceForTesting == commonMatchingSourceForTesting1
    }


    @Test
    void testCommonMatchingSourceWithEqualsDataOrigin() {
        commonMatchingSourceForTesting.setDataOrigin("dataOrigin")
        commonMatchingSourceForTesting1.setDataOrigin("dataOrigin2")
        assertFalse commonMatchingSourceForTesting == commonMatchingSourceForTesting1
    }


    @Test
    void testCommonMatchingSourceWithEqualsOtherInstance() {
        AcademicYearForTesting academicYearForTesting=new AcademicYearForTesting()
        commonMatchingSourceForTesting.setDataOrigin("dataOrigin")
        assertFalse commonMatchingSourceForTesting == academicYearForTesting
    }


    @Test
    void testCommonMatchingSourceWithEqualsSameObject() {
        assertTrue commonMatchingSourceForTesting1 == commonMatchingSourceForTesting
    }


    @Test
    void testCommonMatchingSourceWithHashCode() {
        assertTrue commonMatchingSourceForTesting.hashCode() ==commonMatchingSourceForTesting1.hashCode()
    }


    @Test
    void testCommonMatchingSourceWithHashCodeAllFields() {
        commonMatchingSourceForTesting.setCode("code")
        commonMatchingSourceForTesting1.setCode("code")
        commonMatchingSourceForTesting1.hashCode()
        assertTrue commonMatchingSourceForTesting1.hashCode() ==commonMatchingSourceForTesting.hashCode()
    }
}

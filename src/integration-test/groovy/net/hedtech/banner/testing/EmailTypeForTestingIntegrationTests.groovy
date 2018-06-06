/** *****************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.testing

import org.junit.After
import org.junit.Before
import org.junit.Test

class EmailTypeForTestingIntegrationTests extends BaseIntegrationTestCase{

    EmailTypeForTesting emailTypeForTesting
    EmailTypeForTesting emailTypeForTesting1

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        emailTypeForTesting=new EmailTypeForTesting()
        emailTypeForTesting1=new EmailTypeForTesting()
    }


    @After
    public void tearDown() {
        super.tearDown()
        emailTypeForTesting=null
        emailTypeForTesting1=null
    }

    @Test
    void testEmailType() {
        assertNotNull emailTypeForTesting.toString()
    }

    @Test
    void testEmailTypeWithEquals() {
        assertTrue emailTypeForTesting1 == emailTypeForTesting

    }


    @Test
    void testEmailTypeWithEqualsID() {
        emailTypeForTesting.setId(1234)
        emailTypeForTesting1.setId(3456)
        assertFalse emailTypeForTesting == emailTypeForTesting1

    }


    @Test
    void testEmailTypeWithEqualsVersion() {
        emailTypeForTesting.setVersion(1)
        emailTypeForTesting1.setVersion(2)
        assertFalse emailTypeForTesting == emailTypeForTesting1

    }


    @Test
    void testEmailTypeWithEqualsDescription() {
        emailTypeForTesting.setDescription("termDescription")
        emailTypeForTesting1.setDescription("termDescription1")
        assertFalse emailTypeForTesting == emailTypeForTesting1

    }


    @Test
    void testEmailTypeWithEqualsLastModified() {
        emailTypeForTesting.setLastModified(new Date(12,2,15))
        emailTypeForTesting1.setLastModified(new Date(12,2,12))
        assertFalse emailTypeForTesting == emailTypeForTesting1
    }


    @Test
    void testEmailTypeWithEqualsCode() {
        emailTypeForTesting.setCode("code")
        emailTypeForTesting1.setCode("code1")
        assertFalse emailTypeForTesting == emailTypeForTesting1
    }


    @Test
    void testEmailTypeWithEqualsLastModifiedBy() {
        emailTypeForTesting.setLastModifiedBy("termLastModifiedBy")
        emailTypeForTesting1.setLastModifiedBy("termLastModifiedBy2")
        assertFalse emailTypeForTesting == emailTypeForTesting1
    }


    @Test
    void testEmailTypeWithEqualsDataOrigin() {
        emailTypeForTesting.setDataOrigin("dataOrigin")
        emailTypeForTesting1.setDataOrigin("dataOrigin2")
        assertFalse emailTypeForTesting == emailTypeForTesting1
    }


    @Test
    void testEmailTypeWithEqualsDisplayWebIndicator() {
        emailTypeForTesting.setDisplayWebIndicator(true)
        emailTypeForTesting1.setDisplayWebIndicator(false)
        assertFalse emailTypeForTesting == emailTypeForTesting1
    }


    @Test
    void testEmailTypeWithEqualsUrlIndicator() {
        emailTypeForTesting.setUrlIndicator(true)
        emailTypeForTesting1.setUrlIndicator(false)
        assertFalse emailTypeForTesting == emailTypeForTesting1
    }


    @Test
    void testEmailTypeWithEqualsOtherInstance() {
        AcademicYearForTesting academicYearForTesting=new AcademicYearForTesting()
        emailTypeForTesting.setDataOrigin("dataOrigin")
        assertFalse emailTypeForTesting == academicYearForTesting
    }


    @Test
    void testEmailTypeWithEqualsSameObject() {
        assertTrue emailTypeForTesting1 == emailTypeForTesting

    }


    @Test
    void testEmailTypeWithHashCode() {
        assertTrue emailTypeForTesting.hashCode() ==emailTypeForTesting1.hashCode()
    }


    @Test
    void testEmailTypeWithHashCodeAllFields() {
        emailTypeForTesting.setId(1234)
        emailTypeForTesting1.setId(1234)
        emailTypeForTesting.setDataOrigin("dataOrigin")
        emailTypeForTesting1.setDataOrigin("dataOrigin")
        emailTypeForTesting.setLastModifiedBy("emailTypeLastModifiedBy")
        emailTypeForTesting1.setLastModifiedBy("emailTypeLastModifiedBy")
        emailTypeForTesting.setLastModified(new Date(12,2,15))
        emailTypeForTesting1.setLastModified(new Date(12,2,15))
        emailTypeForTesting.setDescription("emailTypeDescription")
        emailTypeForTesting1.setDescription("emailTypeDescription")
        emailTypeForTesting.setVersion(1)
        emailTypeForTesting1.setVersion(1)
        emailTypeForTesting.setDisplayWebIndicator(true)
        emailTypeForTesting1.setDisplayWebIndicator(true)
        emailTypeForTesting.setUrlIndicator(true)
        emailTypeForTesting1.setUrlIndicator(true)
        assertTrue emailTypeForTesting == emailTypeForTesting1
        emailTypeForTesting1.hashCode()
        assertTrue emailTypeForTesting1.hashCode() ==emailTypeForTesting.hashCode()
    }
}

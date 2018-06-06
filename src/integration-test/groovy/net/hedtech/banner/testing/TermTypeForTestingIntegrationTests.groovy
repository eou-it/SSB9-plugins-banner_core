/** *****************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.testing

import org.junit.After
import org.junit.Before
import org.junit.Test

class TermTypeForTestingIntegrationTests extends BaseIntegrationTestCase {

    TermTypeForTesting termTypeForTesting
    TermTypeForTesting termTypeForTesting1

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        termTypeForTesting=new TermTypeForTesting()
        termTypeForTesting1=new TermTypeForTesting()
    }


    @After
    public void tearDown() {
        super.tearDown()
        termTypeForTesting=null
        termTypeForTesting1=null
    }

    @Test
    void testTerm() {
        assertNotNull termTypeForTesting.toString()
    }

    @Test
    void testTermWithEquals() {
        assertTrue termTypeForTesting1 == termTypeForTesting

    }


    @Test
    void testTermWithEqualsID() {
        termTypeForTesting.setId(1234)
        termTypeForTesting1.setId(3456)
        assertFalse termTypeForTesting == termTypeForTesting1

    }


    @Test
    void testTermWithEqualsVersion() {
        termTypeForTesting.setVersion(1)
        termTypeForTesting1.setVersion(2)
        assertFalse termTypeForTesting == termTypeForTesting1

    }


    @Test
    void testTermWithEqualsDescription() {
        termTypeForTesting.setDescription("termTypeDescription")
        termTypeForTesting1.setDescription("termTypeDescription1")
        assertFalse termTypeForTesting == termTypeForTesting1

    }


    @Test
    void testTermWithEqualsLastModified() {
        termTypeForTesting.setLastModified(new Date(12,2,15))
        termTypeForTesting1.setLastModified(new Date(12,2,12))
        assertFalse termTypeForTesting == termTypeForTesting1
    }

    @Test
    void testTermWithEqualsCode() {
        termTypeForTesting.setCode("code")
        termTypeForTesting1.setCode("code1")
        assertFalse termTypeForTesting == termTypeForTesting1
    }

    @Test
    void testTermWithEqualsLastModifiedBy() {
        termTypeForTesting.setLastModifiedBy("termTypeLastModifiedBy")
        termTypeForTesting1.setLastModifiedBy("termTypeLastModifiedBy2")
        assertFalse termTypeForTesting == termTypeForTesting1

    }


    @Test
    void testTermWithEqualsDataOrigin() {
        termTypeForTesting.setDataOrigin("dataOrigin")
        termTypeForTesting1.setDataOrigin("dataOrigin2")
        assertFalse termTypeForTesting == termTypeForTesting1
    }


    @Test
    void testTermWithEqualsOtherInstance() {
        AcademicYearForTesting academicYearForTesting=new AcademicYearForTesting()
        termTypeForTesting.setDataOrigin("dataOrigin")
        assertFalse termTypeForTesting == academicYearForTesting
    }

    
    @Test
    void testTermWithEqualsSameObject() {
        assertTrue termTypeForTesting1 == termTypeForTesting

    }


    @Test
    void testTermWithHashCode() {
        assertTrue termTypeForTesting.hashCode() ==termTypeForTesting1.hashCode()
    }


    @Test
    void testTermWithHashCodeAllFields() {
        termTypeForTesting.setId(1234)
        termTypeForTesting.setDataOrigin("dataOrigin")
       
        termTypeForTesting.setLastModifiedBy("termLastModifiedBy")
        termTypeForTesting.setLastModified(new Date(12,2,15))
        termTypeForTesting.setDescription("termTestingDescription")
        termTypeForTesting.setVersion(1)
        
        termTypeForTesting1.setId(1234)
        termTypeForTesting1.setDataOrigin("dataOrigin")
        
        termTypeForTesting1.setLastModifiedBy("termLastModifiedBy")
        termTypeForTesting1.setLastModified(new Date(12,2,15))
        termTypeForTesting1.setDescription("termTestingDescription")
        termTypeForTesting1.setVersion(1)
        

        AcademicYearForTesting academicYearForTesting = new AcademicYearForTesting()
        academicYearForTesting.setCode("code")
        AcademicYearForTesting academicYearForTesting1 = new AcademicYearForTesting()
        academicYearForTesting1.setCode("code")
        assertTrue termTypeForTesting == termTypeForTesting1
        TermTypeForTesting termTypeForTesting=new TermTypeForTesting()
        TermTypeForTesting termTypeForTesting1=new TermTypeForTesting()
        termTypeForTesting.setCode("code")
        termTypeForTesting1.setCode("code")
        termTypeForTesting1.hashCode()
        assertTrue termTypeForTesting1.hashCode() ==termTypeForTesting.hashCode()
    }
}

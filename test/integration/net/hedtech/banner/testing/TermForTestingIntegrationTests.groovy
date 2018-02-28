/** *****************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.testing
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
class TermForTestingIntegrationTests extends BaseIntegrationTestCase {

    TermForTesting termForTesting
    TermForTesting termForTesting1

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        termForTesting=new TermForTesting()
        termForTesting1=new TermForTesting()
    }


    @After
    public void tearDown() {
        super.tearDown()
        termForTesting=null
        termForTesting1=null
    }

    @Test
    void testTerm() {
        assertNotNull termForTesting.toString()
    }

    @Test
    void testTermWithEquals() {
        assertTrue termForTesting1 == termForTesting

    }


    @Test
    void testTermWithEqualsID() {
        termForTesting.setId(1234)
        termForTesting1.setId(3456)
        assertFalse termForTesting == termForTesting1

    }


    @Test
    void testTermWithEqualsVersion() {
        termForTesting.setVersion(1)
        termForTesting1.setVersion(2)
        assertFalse termForTesting == termForTesting1

    }


    @Test
    void testTermWithEqualsDescription() {
        termForTesting.setDescription("termDescription")
        termForTesting1.setDescription("termDescription1")
        assertFalse termForTesting == termForTesting1

    }


    @Test
    void testTermWithEqualsLastModified() {
        termForTesting.setLastModified(new Date(12,2,15))
        termForTesting1.setLastModified(new Date(12,2,12))
        assertFalse termForTesting == termForTesting1
    }

    @Test
    void testTermWithEqualsCode() {
        termForTesting.setCode("code")
        termForTesting1.setCode("code1")
        assertFalse termForTesting == termForTesting1
    }

    @Test
    void testTermWithEqualsLastModifiedBy() {
        termForTesting.setLastModifiedBy("termLastModifiedBy")
        termForTesting1.setLastModifiedBy("termLastModifiedBy2")
        assertFalse termForTesting == termForTesting1

    }


    @Test
    void testTermWithEqualsDataOrigin() {
        termForTesting.setDataOrigin("dataOrigin")
        termForTesting1.setDataOrigin("dataOrigin2")
        assertFalse termForTesting == termForTesting1
    }


    @Test
    void testTermWithEqualsOtherInstance() {
        AcademicYearForTesting academicYearForTesting=new AcademicYearForTesting()
        termForTesting.setDataOrigin("dataOrigin")
        assertFalse termForTesting == academicYearForTesting
    }

    @Test
    void testTermWithEqualsFinancialAidTerm() {
        termForTesting.setFinancialAidTerm("termFinancialAidTerm")
        termForTesting1.setFinancialAidTerm("termFinancialAidTerm1")
        assertFalse termForTesting == termForTesting1
    }

    @Test
    void testTermWithEqualsAcademicYear() {
        AcademicYearForTesting academicYearForTesting = new AcademicYearForTesting()
        academicYearForTesting.setCode("code")
        AcademicYearForTesting academicYearForTesting1 = new AcademicYearForTesting()
        academicYearForTesting1.setCode("code1")
        termForTesting.setAcademicYear(academicYearForTesting)
        termForTesting1.setAcademicYear(academicYearForTesting1)
        assertFalse termForTesting == termForTesting1
    }

    @Test
    void testTermWithEqualsEndDate() {
        termForTesting.setEndDate(new Date(12,2,15))
        termForTesting1.setEndDate(new Date(12,2,12))
        assertFalse termForTesting == termForTesting1
    }

    @Test
    void testTermWithEqualsHousingEndDate() {
        termForTesting.setHousingEndDate(new Date(12,2,15))
        termForTesting1.setHousingEndDate(new Date(12,2,12))
        assertFalse termForTesting == termForTesting1
    }

    @Test
    void testTermWithEqualsHousingStartDate() {
        termForTesting.setHousingStartDate(new Date(12,2,15))
        termForTesting1.setHousingStartDate(new Date(12,2,12))
        assertFalse termForTesting == termForTesting1
    }

    @Test
    void testTermWithEqualsStartDate() {
        termForTesting.setStartDate(new Date(12,2,15))
        termForTesting1.setStartDate(new Date(12,2,12))
        assertFalse termForTesting == termForTesting1
    }

    @Test
    void testTermWithEqualsFinancialAidProcessingYear() {
        termForTesting.setFinancialAidProcessingYear("financialAidProcessingYear")
        termForTesting1.setFinancialAidProcessingYear("financialAidProcessingYear1")
        assertFalse termForTesting == termForTesting1
    }


    @Test
    void testTermWithEqualsFinancialAidPeriod() {
        termForTesting.setFinancialAidPeriod(10)
        termForTesting1.setFinancialAidPeriod(12)
        assertFalse termForTesting == termForTesting1
    }

    @Test
    void testTermWithEqualsFinancialEndPeriod() {
        termForTesting.setFinancialEndPeriod(10)
        termForTesting1.setFinancialEndPeriod(12)
        assertFalse termForTesting == termForTesting1
    }


    @Test
    void testTermWithEqualsSystemReqInd() {
        termForTesting.setSystemReqInd(true)
        termForTesting1.setSystemReqInd(false)
        assertFalse termForTesting == termForTesting1
    }


    @Test
    void testTermWithEqualsFinanceSummerIndicator() {
        termForTesting.setFinanceSummerIndicator(true)
        termForTesting1.setFinanceSummerIndicator(false)
        assertFalse termForTesting == termForTesting1
    }


    @Test
    void testTermWithEqualsTermType() {
       TermTypeForTesting termTypeForTesting=new TermTypeForTesting()
        TermTypeForTesting termTypeForTesting1=new TermTypeForTesting()
        termTypeForTesting.setCode("code")
        termTypeForTesting1.setCode("code1")
        termForTesting.setTermType(termTypeForTesting)
        termForTesting1.setTermType(termTypeForTesting1)
        assertFalse termForTesting == termForTesting1
    }


    @Test
    void testTermWithEqualsSameObject() {
        assertTrue termForTesting1 == termForTesting

    }


    @Test
    void testTermWithHashCode() {
        assertTrue termForTesting.hashCode() ==termForTesting1.hashCode()
    }


    @Test
    void testTermWithHashCodeAllFields() {
        termForTesting.setId(1234)
        termForTesting.setDataOrigin("dataOrigin")
        termForTesting.setFinancialAidTerm("code")
        termForTesting.setLastModifiedBy("termLastModifiedBy")
        termForTesting.setLastModified(new Date(12,2,15))
        termForTesting.setDescription("termTestingDescription")
        termForTesting.setVersion(1)
        termForTesting.setHousingEndDate(new Date(12,2,15))
        termForTesting.setEndDate(new Date(12,2,15))
        termForTesting1.setId(1234)
        termForTesting1.setDataOrigin("dataOrigin")
        termForTesting1.setFinancialAidTerm("code")
        termForTesting1.setLastModifiedBy("termLastModifiedBy")
        termForTesting1.setLastModified(new Date(12,2,15))
        termForTesting1.setDescription("termTestingDescription")
        termForTesting1.setVersion(1)
        termForTesting1.setHousingEndDate(new Date(12,2,12))
        termForTesting1.setEndDate(new Date(12,2,15))
        termForTesting.setFinancialAidTerm("termFinancialAidTerm")
        termForTesting1.setFinancialAidTerm("termFinancialAidTerm")
        termForTesting.setAcademicYear(new AcademicYearForTesting())
        termForTesting1.setAcademicYear(new AcademicYearForTesting())
        termForTesting.setFinancialAidProcessingYear("financialAidProcessingYear")
        termForTesting1.setFinancialAidProcessingYear("financialAidProcessingYear")
        termForTesting.setFinancialAidPeriod(10)
        termForTesting1.setFinancialAidPeriod(10)
        termForTesting.setFinancialEndPeriod(10)
        termForTesting1.setFinancialEndPeriod(10)
        termForTesting.setSystemReqInd(true)
        termForTesting1.setSystemReqInd(true)
        termForTesting.setFinanceSummerIndicator(true)
        termForTesting1.setFinanceSummerIndicator(true)
        termForTesting.setHousingStartDate(new Date(12,2,15))
        termForTesting1.setHousingStartDate(new Date(12,2,15))
        termForTesting.setStartDate(new Date(12,2,15))
        termForTesting1.setStartDate(new Date(12,2,15))
        AcademicYearForTesting academicYearForTesting = new AcademicYearForTesting()
        academicYearForTesting.setCode("code")
        AcademicYearForTesting academicYearForTesting1 = new AcademicYearForTesting()
        academicYearForTesting1.setCode("code")
        termForTesting.setAcademicYear(academicYearForTesting)
        termForTesting1.setAcademicYear(academicYearForTesting1)
        assertFalse termForTesting == termForTesting1
        TermTypeForTesting termTypeForTesting=new TermTypeForTesting()
        TermTypeForTesting termTypeForTesting1=new TermTypeForTesting()
        termTypeForTesting.setCode("code")
        termTypeForTesting1.setCode("code")
        termForTesting1.hashCode()
        assertFalse termForTesting1.hashCode() ==termForTesting.hashCode()
    }
}

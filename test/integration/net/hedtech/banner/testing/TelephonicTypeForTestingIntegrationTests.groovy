/** *****************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.testing
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

class TelephonicTypeForTestingIntegrationTests extends BaseIntegrationTestCase {

    TelephoneTypeForTesting telephoneTypeForTesting
    TelephoneTypeForTesting telephoneTypeForTesting2

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        telephoneTypeForTesting=new TelephoneTypeForTesting()
        telephoneTypeForTesting2=new TelephoneTypeForTesting()
    }


    @After
    public void tearDown() {
        super.tearDown()
        telephoneTypeForTesting=null
        telephoneTypeForTesting2=null
    }


    @Test
    void testTelephonicType() {
        telephoneTypeForTesting.toString()
    }

    @Test
    void testTelephonicTypeWithEquals() {
        telephoneTypeForTesting2.equals(telephoneTypeForTesting)

    }


    @Test
    void testTelephonicTypeWithEqualsID() {
        telephoneTypeForTesting.setId(1234)
        telephoneTypeForTesting2.setId(3456)
        telephoneTypeForTesting.equals(telephoneTypeForTesting2)

    }


    @Test
    void testTelephonicTypeWithEqualsVersion() {
        telephoneTypeForTesting.setVersion(1)
        telephoneTypeForTesting2.setVersion(2)
        telephoneTypeForTesting.equals(telephoneTypeForTesting2)

    }


    @Test
    void testTelephonicTypeWithEqualsDescription() {
        telephoneTypeForTesting.setDescription("telephoneTypeForTestingDescription")
        telephoneTypeForTesting2.setDescription("telephoneTypeForTesting2Description")
        telephoneTypeForTesting.equals(telephoneTypeForTesting2)

    }


    @Test
    void testTelephonicTypeWithEqualsLastModified() {
        telephoneTypeForTesting.setLastModified(new Date(12,2,15))
        telephoneTypeForTesting2.setLastModified(new Date(12,2,12))
        telephoneTypeForTesting.equals(telephoneTypeForTesting2)
    }


    @Test
    void testTelephonicTypeWithEqualsLastModifiedBy() {
        telephoneTypeForTesting.setLastModifiedBy("telephoneTypeForLastModifiedBy")
        telephoneTypeForTesting2.setLastModifiedBy("telephoneTypeForLastModifiedBy2")
        telephoneTypeForTesting.equals(telephoneTypeForTesting2)
    }


    @Test
    void testTelephonicTypeWithEqualsCode() {
        telephoneTypeForTesting.setCode("code")
        telephoneTypeForTesting2.setCode("code2")
        telephoneTypeForTesting.equals(telephoneTypeForTesting2)
    }

    @Test
    void testTelephonicTypeWithEqualsDataOrigin() {
        telephoneTypeForTesting.setDataOrigin("dataOrigin")
        telephoneTypeForTesting2.setDataOrigin("dataOrigin2")
        telephoneTypeForTesting.equals(telephoneTypeForTesting2)
    }


    @Test
    void testTelephonicTypeWithEqualsOtherInstance() {
        AcademicYearForTesting academicYearForTesting=new AcademicYearForTesting()
        telephoneTypeForTesting.setDataOrigin("dataOrigin")
        telephoneTypeForTesting2.setDataOrigin("dataOrigin2")
        telephoneTypeForTesting.equals(academicYearForTesting)
    }


    @Test
    void testTelephonicTypeWithEqualsSameObject() {
        telephoneTypeForTesting.equals(telephoneTypeForTesting)
    }


    @Test
    void testTelephonicTypeWithHashCode() {
        telephoneTypeForTesting.hashCode().equals(telephoneTypeForTesting2.hashCode())
    }


    @Test
    void testTelephonicTypeWithHashCodeAllFields() {
        telephoneTypeForTesting.setId(1234)
        telephoneTypeForTesting.setDataOrigin("dataOrigin")
        telephoneTypeForTesting.setCode("code")
        telephoneTypeForTesting.setLastModifiedBy("telephoneTypeForLastModifiedBy")
        telephoneTypeForTesting.setLastModified(new Date(12,2,15))
        telephoneTypeForTesting.setDescription("telephoneTypeForTestingDescription")
        telephoneTypeForTesting.setVersion(1)
        telephoneTypeForTesting2.setId(1234)
        telephoneTypeForTesting2.setDataOrigin("dataOrigin")
        telephoneTypeForTesting2.setCode("code")
        telephoneTypeForTesting2.setLastModifiedBy("telephoneTypeForLastModifiedBy")
        telephoneTypeForTesting2.setLastModified(new Date(12,2,15))
        telephoneTypeForTesting2.setDescription("telephoneTypeForTestingDescription")
        telephoneTypeForTesting2.setVersion(1)
        telephoneTypeForTesting2.hashCode()
        telephoneTypeForTesting2.hashCode().equals(telephoneTypeForTesting.hashCode())
    }
}

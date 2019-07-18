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
class AddressTypeForTestingIntegrationTests extends BaseIntegrationTestCase{

    AddressTypeForTesting addressTypeForTesting
    AddressTypeForTesting addressTypeForTesting1

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        addressTypeForTesting=new AddressTypeForTesting()
        addressTypeForTesting1=new AddressTypeForTesting()
    }


    @After
    public void tearDown() {
        super.tearDown()
        addressTypeForTesting=null
        addressTypeForTesting1=null
    }


    @Test
    void testTerm() {
        assertNotNull addressTypeForTesting.toString()
    }


    @Test
    void testTermWithEquals() {
        assertTrue addressTypeForTesting1 == addressTypeForTesting
    }


    @Test
    void testAddressTypeWithEqualsID() {
        addressTypeForTesting.setId(1234)
        addressTypeForTesting1.setId(3456)
        assertFalse addressTypeForTesting == addressTypeForTesting1
    }


    @Test
    void testAddressTypeWithEqualsVersion() {
        addressTypeForTesting.setVersion(1)
        addressTypeForTesting1.setVersion(2)
        assertFalse addressTypeForTesting == addressTypeForTesting1

    }


    @Test
    void testAddressTypeWithEqualsDescription() {
        addressTypeForTesting.setDescription("termTypeDescription")
        addressTypeForTesting1.setDescription("termTypeDescription1")
        assertFalse addressTypeForTesting == addressTypeForTesting1

    }


    @Test
    void testAddressTypeWithEqualsLastModified() {
        addressTypeForTesting.setLastModified(new Date(12,2,15))
        addressTypeForTesting1.setLastModified(new Date(12,2,12))
        assertFalse addressTypeForTesting == addressTypeForTesting1
    }


    @Test
    void testAddressTypeWithEqualsCode() {
        addressTypeForTesting.setCode("code")
        addressTypeForTesting1.setCode("code1")
        assertFalse addressTypeForTesting == addressTypeForTesting1
    }


    @Test
    void testAddressTypeWithEqualsLastModifiedBy() {
        addressTypeForTesting.setLastModifiedBy("termTypeLastModifiedBy")
        addressTypeForTesting1.setLastModifiedBy("termTypeLastModifiedBy2")
        assertFalse addressTypeForTesting == addressTypeForTesting1

    }


    @Test
    void testAddressTypeWithEqualsDataOrigin() {
        addressTypeForTesting.setDataOrigin("dataOrigin")
        addressTypeForTesting1.setDataOrigin("dataOrigin2")
        assertFalse addressTypeForTesting == addressTypeForTesting1
    }


    @Test
    void testAddressTypeWithEqualsSystemRequiredIndicator() {
        addressTypeForTesting.setSystemRequiredIndicator("systemRequiredIndicator")
        addressTypeForTesting1.setSystemRequiredIndicator("systemRequiredIndicator1")
        assertFalse addressTypeForTesting == addressTypeForTesting1
    }


    @Test
    void testAddressTypeWithEqualsTelephonicForTesting() {
        TelephoneTypeForTesting telephoneTypeForTesting=new TelephoneTypeForTesting()
        TelephoneTypeForTesting telephoneTypeForTesting2=new TelephoneTypeForTesting()
        telephoneTypeForTesting.setId(1234)
        telephoneTypeForTesting2.setId(3456)
        addressTypeForTesting.setTelephoneType(telephoneTypeForTesting)
        addressTypeForTesting1.setTelephoneType(telephoneTypeForTesting2)
        assertFalse addressTypeForTesting == addressTypeForTesting1
    }


    @Test
    void testAddressTypeWithEqualsOtherInstance() {
        AcademicYearForTesting academicYearForTesting=new AcademicYearForTesting()
        addressTypeForTesting.setDataOrigin("dataOrigin")
        assertFalse addressTypeForTesting == academicYearForTesting
    }


    @Test
    void testAddressTypeWithEqualsSameObject() {
        assertTrue addressTypeForTesting1 == addressTypeForTesting

    }


    @Test
    void testAddressTypeWithHashCode() {
        assertTrue addressTypeForTesting.hashCode() ==addressTypeForTesting1.hashCode()
    }


    @Test
    void testAddressTypeWithHashCodeAllFields() {
        addressTypeForTesting.setId(1234)
        addressTypeForTesting.setDataOrigin("dataOrigin")
        addressTypeForTesting.setLastModifiedBy("termLastModifiedBy")
        addressTypeForTesting.setLastModified(new Date(12,2,15))
        addressTypeForTesting.setDescription("termTestingDescription")
        addressTypeForTesting.setVersion(1)
        addressTypeForTesting1.setId(1234)
        addressTypeForTesting1.setDataOrigin("dataOrigin")
        addressTypeForTesting1.setLastModifiedBy("termLastModifiedBy")
        addressTypeForTesting1.setLastModified(new Date(12,2,15))
        addressTypeForTesting1.setDescription("termTestingDescription")
        addressTypeForTesting1.setVersion(1)
        AcademicYearForTesting academicYearForTesting = new AcademicYearForTesting()
        academicYearForTesting.setCode("code")
        AcademicYearForTesting academicYearForTesting1 = new AcademicYearForTesting()
        academicYearForTesting1.setCode("code")
        assertTrue addressTypeForTesting == addressTypeForTesting1
        AddressTypeForTesting addressTypeForTesting=new AddressTypeForTesting()
        AddressTypeForTesting addressTypeForTesting1=new AddressTypeForTesting()
        addressTypeForTesting.setCode("code")
        addressTypeForTesting1.setCode("code")
        addressTypeForTesting.setSystemRequiredIndicator("systemRequiredIndicator")
        addressTypeForTesting1.setSystemRequiredIndicator("systemRequiredIndicator")
        TelephoneTypeForTesting telephoneTypeForTesting=new TelephoneTypeForTesting()
        TelephoneTypeForTesting telephoneTypeForTesting2=new TelephoneTypeForTesting()
        telephoneTypeForTesting.setId(1234)
        telephoneTypeForTesting2.setId(1234)
        addressTypeForTesting.setTelephoneType(telephoneTypeForTesting)
        addressTypeForTesting1.setTelephoneType(telephoneTypeForTesting2)
        addressTypeForTesting1.hashCode()
        assertTrue addressTypeForTesting1.hashCode() ==addressTypeForTesting.hashCode()
    }
}

/** *****************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */

package net.hedtech.banner.testing

import org.junit.After
import org.junit.Before
import org.junit.Test

class CommonMatchingSourceRuleForTestingIntegrationTests extends BaseIntegrationTestCase{
    CommonMatchingSourceRuleForTesting commonMatchingSourceRuleForTesting
    CommonMatchingSourceRuleForTesting commonMatchingSourceRuleForTesting1

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        commonMatchingSourceRuleForTesting=new CommonMatchingSourceRuleForTesting()
        commonMatchingSourceRuleForTesting1=new CommonMatchingSourceRuleForTesting()
    }


    @After
    public void tearDown() {
        super.tearDown()
        commonMatchingSourceRuleForTesting=null
        commonMatchingSourceRuleForTesting1=null
    }


    @Test
    void testCommonMatchingSourceRule() {
        assertNotNull commonMatchingSourceRuleForTesting.toString()
    }


    @Test
    void testCommonMatchingSourceRuleWithEquals() {
        assertTrue commonMatchingSourceRuleForTesting1 == commonMatchingSourceRuleForTesting

    }


    @Test
    void testCommonMatchingSourceRuleWithEqualsID() {
        commonMatchingSourceRuleForTesting.setId(1234)
        commonMatchingSourceRuleForTesting1.setId(3456)
        assertFalse commonMatchingSourceRuleForTesting == commonMatchingSourceRuleForTesting1

    }


    @Test
    void testCommonMatchingSourceRuleWithEqualsVersion() {
        commonMatchingSourceRuleForTesting.setVersion(1)
        commonMatchingSourceRuleForTesting1.setVersion(2)
        assertFalse commonMatchingSourceRuleForTesting == commonMatchingSourceRuleForTesting1

    }


    @Test
    void testCommonMatchingSourceRuleWithEqualsOnlineMatchIndicator() {
        commonMatchingSourceRuleForTesting.setOnlineMatchIndicator(true)
        commonMatchingSourceRuleForTesting1.setOnlineMatchIndicator(false)
        assertFalse commonMatchingSourceRuleForTesting == commonMatchingSourceRuleForTesting1
    }


    @Test
    void testCommonMatchingSourceRuleWithEqualsTransposeDateIndicator() {
        commonMatchingSourceRuleForTesting.setTransposeDateIndicator(true)
        commonMatchingSourceRuleForTesting1.setTransposeDateIndicator(false)
        assertFalse commonMatchingSourceRuleForTesting == commonMatchingSourceRuleForTesting1
    }

    @Test
    void testCommonMatchingSourceRuleWithEqualsTransposeNameIndicator() {
        commonMatchingSourceRuleForTesting.setTransposeNameIndicator(true)
        commonMatchingSourceRuleForTesting1.setTransposeNameIndicator(false)
        assertFalse commonMatchingSourceRuleForTesting == commonMatchingSourceRuleForTesting1
    }

    @Test
    void testCommonMatchingSourceRuleWithEqualsAliasWildcardIndicator() {
        commonMatchingSourceRuleForTesting.setAliasWildcardIndicator(true)
        commonMatchingSourceRuleForTesting1.setAliasWildcardIndicator(false)
        assertFalse commonMatchingSourceRuleForTesting == commonMatchingSourceRuleForTesting1
    }
    @Test
    void testCommonMatchingSourceRuleWithEqualsLengthOverrideIndicator() {
        commonMatchingSourceRuleForTesting.setLengthOverrideIndicator(true)
        commonMatchingSourceRuleForTesting1.setLengthOverrideIndicator(false)
        assertFalse commonMatchingSourceRuleForTesting == commonMatchingSourceRuleForTesting1
    }

    @Test
    void testCommonMatchingSourceRuleWithEqualsApiFailureIndicator() {
        commonMatchingSourceRuleForTesting.setApiFailureIndicator(true)
        commonMatchingSourceRuleForTesting1.setApiFailureIndicator(false)
        assertFalse commonMatchingSourceRuleForTesting == commonMatchingSourceRuleForTesting1
    }

    @Test
    void testCommonMatchingSourceRuleWithEqualsLastModified() {
        commonMatchingSourceRuleForTesting.setLastModified(new Date(12,2,15))
        commonMatchingSourceRuleForTesting1.setLastModified(new Date(12,2,12))
        assertFalse commonMatchingSourceRuleForTesting == commonMatchingSourceRuleForTesting1
    }

    @Test
    void testCommonMatchingSourceRuleWithEqualsCode() {
        commonMatchingSourceRuleForTesting.setEntity("entity")
        commonMatchingSourceRuleForTesting1.setEntity("entity1")
        assertFalse commonMatchingSourceRuleForTesting == commonMatchingSourceRuleForTesting1
    }


    @Test
    void testCommonMatchingSourceRuleWithEqualsLastModifiedBy() {
        commonMatchingSourceRuleForTesting.setLastModifiedBy("termTypeLastModifiedBy")
        commonMatchingSourceRuleForTesting1.setLastModifiedBy("termTypeLastModifiedBy2")
        assertFalse commonMatchingSourceRuleForTesting == commonMatchingSourceRuleForTesting1

    }


    @Test
    void testCommonMatchingSourceRuleWithEqualsCommonMatchingSource() {
        CommonMatchingSourceForTesting commonMatchingSourceForTesting=new CommonMatchingSourceForTesting()
        CommonMatchingSourceForTesting commonMatchingSourceForTesting1=new CommonMatchingSourceForTesting()
        commonMatchingSourceForTesting.setId(1234)
        commonMatchingSourceForTesting1.setId(3456)
        commonMatchingSourceRuleForTesting.setCommonMatchingSource(commonMatchingSourceForTesting)
        commonMatchingSourceRuleForTesting1.setCommonMatchingSource(commonMatchingSourceForTesting1)
        assertFalse commonMatchingSourceRuleForTesting == commonMatchingSourceRuleForTesting1
    }


    @Test
    void testCommonMatchingSourceRuleWithEqualsAddressType() {
        AddressTypeForTesting addressTypeForTesting=new AddressTypeForTesting()
        AddressTypeForTesting addressTypeForTesting1=new AddressTypeForTesting()
        addressTypeForTesting.setId(1234)
        addressTypeForTesting1.setId(3456)
        commonMatchingSourceRuleForTesting.setAddressType(addressTypeForTesting)
        commonMatchingSourceRuleForTesting1.setAddressType(addressTypeForTesting1)
        assertFalse commonMatchingSourceRuleForTesting == commonMatchingSourceRuleForTesting1
    }


    @Test
    void testCommonMatchingSourceRuleWithEqualsEmailType() {
        EmailTypeForTesting emailTypeForTesting=new EmailTypeForTesting()
        EmailTypeForTesting emailTypeForTesting1=new EmailTypeForTesting()
        emailTypeForTesting.setId(1234)
        emailTypeForTesting1.setId(3456)
        commonMatchingSourceRuleForTesting.setEmailType(emailTypeForTesting)
        commonMatchingSourceRuleForTesting1.setEmailType(emailTypeForTesting1)
        assertFalse commonMatchingSourceRuleForTesting == commonMatchingSourceRuleForTesting1
    }


    @Test
    void testCommonMatchingSourceRuleWithEqualsTelephonicType(){
        TelephoneTypeForTesting telephoneTypeForTesting=new TelephoneTypeForTesting()
        TelephoneTypeForTesting telephoneTypeForTesting1=new TelephoneTypeForTesting()
        telephoneTypeForTesting.setId(1234)
        telephoneTypeForTesting1.setId(3456)
        commonMatchingSourceRuleForTesting.setTelephoneType(telephoneTypeForTesting)
        commonMatchingSourceRuleForTesting1.setTelephoneType(telephoneTypeForTesting1)
        assertFalse commonMatchingSourceRuleForTesting == commonMatchingSourceRuleForTesting1
    }


    @Test
    void testCommonMatchingSourceRuleWithEqualsDataOrigin() {
        commonMatchingSourceRuleForTesting.setDataOrigin("dataOrigin")
        commonMatchingSourceRuleForTesting1.setDataOrigin("dataOrigin2")
        assertFalse commonMatchingSourceRuleForTesting == commonMatchingSourceRuleForTesting1
    }


    @Test
    void testCommonMatchingSourceRuleWithEqualsOtherInstance() {
        AcademicYearForTesting academicYearForTesting=new AcademicYearForTesting()
        commonMatchingSourceRuleForTesting.setDataOrigin("dataOrigin")
        assertFalse commonMatchingSourceRuleForTesting == academicYearForTesting
    }


    @Test
    void testCommonMatchingSourceRuleWithEqualsSameObject() {
        assertTrue commonMatchingSourceRuleForTesting1 == commonMatchingSourceRuleForTesting

    }


    @Test
    void testCommonMatchingSourceRuleWithHashCode() {
        assertTrue commonMatchingSourceRuleForTesting.hashCode() ==commonMatchingSourceRuleForTesting1.hashCode()
    }


    @Test
    void testCommonMatchingSourceWithHashCodeAllFields() {
        commonMatchingSourceRuleForTesting.setId(1234)
        commonMatchingSourceRuleForTesting.setDataOrigin("dataOrigin")

        commonMatchingSourceRuleForTesting.setLastModifiedBy("termLastModifiedBy")
        commonMatchingSourceRuleForTesting.setLastModified(new Date(12,2,15))
        commonMatchingSourceRuleForTesting.setVersion(1)
        commonMatchingSourceRuleForTesting1.setId(1234)
        commonMatchingSourceRuleForTesting1.setDataOrigin("dataOrigin")
        commonMatchingSourceRuleForTesting1.setLastModifiedBy("termLastModifiedBy")
        commonMatchingSourceRuleForTesting1.setLastModified(new Date(12,2,15))
        commonMatchingSourceRuleForTesting1.setVersion(1)
        commonMatchingSourceRuleForTesting.setOnlineMatchIndicator(true)
        commonMatchingSourceRuleForTesting1.setOnlineMatchIndicator(true)
        commonMatchingSourceRuleForTesting.setTransposeDateIndicator(true)
        commonMatchingSourceRuleForTesting1.setTransposeDateIndicator(true)
        commonMatchingSourceRuleForTesting.setTransposeNameIndicator(true)
        commonMatchingSourceRuleForTesting1.setTransposeNameIndicator(true)
        commonMatchingSourceRuleForTesting.setAliasWildcardIndicator(true)
        commonMatchingSourceRuleForTesting1.setAliasWildcardIndicator(true)
        commonMatchingSourceRuleForTesting.setLengthOverrideIndicator(true)
        commonMatchingSourceRuleForTesting1.setLengthOverrideIndicator(true)
        commonMatchingSourceRuleForTesting.setApiFailureIndicator(true)
        commonMatchingSourceRuleForTesting1.setApiFailureIndicator(true)
        commonMatchingSourceRuleForTesting.setEntity("entity")
        commonMatchingSourceRuleForTesting1.setEntity("entity")
        AcademicYearForTesting academicYearForTesting = new AcademicYearForTesting()
        academicYearForTesting.setCode("code")
        AcademicYearForTesting academicYearForTesting1 = new AcademicYearForTesting()
        academicYearForTesting1.setCode("code")
        CommonMatchingSourceForTesting commonMatchingSourceForTesting=new CommonMatchingSourceForTesting()
        CommonMatchingSourceForTesting commonMatchingSourceForTesting1=new CommonMatchingSourceForTesting()
        commonMatchingSourceForTesting.setId(1234)
        commonMatchingSourceForTesting1.setId(1234)
        commonMatchingSourceRuleForTesting.setCommonMatchingSource(commonMatchingSourceForTesting)
        commonMatchingSourceRuleForTesting1.setCommonMatchingSource(commonMatchingSourceForTesting1)
        AddressTypeForTesting addressTypeForTesting=new AddressTypeForTesting()
        AddressTypeForTesting addressTypeForTesting1=new AddressTypeForTesting()
        addressTypeForTesting.setId(1234)
        addressTypeForTesting1.setId(1234)
        commonMatchingSourceRuleForTesting.setAddressType(addressTypeForTesting)
        commonMatchingSourceRuleForTesting1.setAddressType(addressTypeForTesting1)
        EmailTypeForTesting emailTypeForTesting=new EmailTypeForTesting()
        EmailTypeForTesting emailTypeForTesting1=new EmailTypeForTesting()
        emailTypeForTesting.setId(1234)
        emailTypeForTesting1.setId(1234)
        commonMatchingSourceRuleForTesting.setEmailType(emailTypeForTesting)
        commonMatchingSourceRuleForTesting1.setEmailType(emailTypeForTesting1)
        TelephoneTypeForTesting telephoneTypeForTesting=new TelephoneTypeForTesting()
        TelephoneTypeForTesting telephoneTypeForTesting1=new TelephoneTypeForTesting()
        telephoneTypeForTesting.setId(1234)
        telephoneTypeForTesting1.setId(1234)
        commonMatchingSourceRuleForTesting.setTelephoneType(telephoneTypeForTesting)
        commonMatchingSourceRuleForTesting1.setTelephoneType(telephoneTypeForTesting1)
        assertTrue commonMatchingSourceRuleForTesting == commonMatchingSourceRuleForTesting1
        assertTrue commonMatchingSourceRuleForTesting1.hashCode() ==commonMatchingSourceRuleForTesting.hashCode()
    }
}

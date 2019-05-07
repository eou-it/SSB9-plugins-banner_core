/*******************************************************************************
Copyright 2009-2017 Ellucian Company L.P. and its affiliates.
*******************************************************************************/

package net.hedtech.banner.general.utility

import net.hedtech.banner.testing.BaseIntegrationTestCase
import net.hedtech.banner.testing.ZipForTesting
import org.junit.After
import org.junit.Before
import org.junit.Test
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration

@Integration
@Rollback
class DomainAttributePropertiesServiceIntegrationTests extends BaseIntegrationTestCase {

    def domainAttributePropertiesService

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
    }

    @After
    public void tearDown() {
        super.tearDown()
    }

    @Test
    void testExtractClassWithDomainName() {

        def classMetadata
        classMetadata = domainAttributePropertiesService.extractClass("net.hedtech.banner.testing.AcademicYearForTesting")
        assertNotNull classMetadata
        }

    @Test
    void testExtractClassWithNullDomainName() {

        def classMetadata
        // facultyScheduleQueryView
        classMetadata = domainAttributePropertiesService.extractClass(null)
        assertNull classMetadata
    }

    @Test
    void testExtractClassWithEmptyDomainName() {

        def classMetadata
        // facultyScheduleQueryView
        classMetadata = domainAttributePropertiesService.extractClass("")
        assertNull classMetadata
    }


    @Test
    void testINvalidgetDataLengthForColumn() {

        def maxSize
        // facultyScheduleQueryView
        maxSize = domainAttributePropertiesService.getDataLengthForColumn("test_invalid","test invalid")
        assertNull  maxSize

    }
    @Test
    void testEmptygetDataLengthForColumn() {

        def maxSize
        // facultyScheduleQueryView
        maxSize = domainAttributePropertiesService.getDataLengthForColumn("","")
        assertNull maxSize

    }

    @Test
    void testGetClassMetadataByEntityNameWithoutConstraintProperties() {

         def classMetadata
         // academicYearForTesting
         classMetadata = domainAttributePropertiesService.extractClassMetadataByName("net.hedtech.banner.testing.AcademicYearForTesting")

         assertNotNull classMetadata
         assertEquals "STVACYR_CODE", classMetadata.attributes.code.columnName
         assertEquals 4, classMetadata.attributes.code.maxSize
         assertEquals "String", classMetadata.attributes.code.propertyType

     }

    @Test
    void testExtractClassMetadataByNameEmptyDomainName() {

        def classMetadata
        // facultyScheduleQueryView
        classMetadata = domainAttributePropertiesService.extractClassMetadataByName("")
        assertNull classMetadata
    }
    @Test
    void testExtractClassMetadataByNameNullDomainName() {

        def classMetadata
        // facultyScheduleQueryView
        classMetadata = domainAttributePropertiesService.extractClassMetadataByName(null)
        assertNull classMetadata
    }
    @Test
    void testExtractClassMetadataByNameInvalidDomainName() {

        def classMetadata
        // facultyScheduleQueryView
        classMetadata = domainAttributePropertiesService.extractClassMetadataByName("invalid test")
        assertNull classMetadata
     }



    @Test
    void testGetClassMetadataByPojo() {

         def classMetadata
         def zipForTesting = new ZipForTesting()

         classMetadata = domainAttributePropertiesService.extractClassMetadataByPojo(zipForTesting)

         assertNotNull classMetadata
         assertNotNull classMetadata.attributes.code
         assertEquals "String", classMetadata.attributes.code.propertyType
         }


    @Test
    void testGetClassMetadataByEntityName() {
        def classMetadata
        // zip
        classMetadata = domainAttributePropertiesService.extractClassMetadataByName("net.hedtech.banner.testing.ZipForTesting")

        assertNotNull classMetadata
        assertEquals "GTVZIPC_CODE", classMetadata.attributes.code.columnName
        assertEquals "GTVZIPC_CITY", classMetadata.attributes.city.columnName
        assertFalse "GTVZIPC_CODE", classMetadata.attributes.city.nullable
        assertEquals 30, classMetadata.attributes.code.maxSize
        assertEquals 50, classMetadata.attributes.city.maxSize
        assertEquals 11, classMetadata.attributes.lastModified.maxSize , 1e-8
        assertEquals "String", classMetadata.attributes.city.propertyType

        // CourseLaborDistribution
        classMetadata = domainAttributePropertiesService.extractClassMetadataByName("net.hedtech.banner.testing.CourseLaborDistributionForTesting")
        assertNotNull classMetadata
        assertEquals "SCRCLBD_SEQ_NO", classMetadata.attributes.sequenceNumber.columnName
        assertEquals new Integer(999), classMetadata.attributes.sequenceNumber.max
        assertEquals new Integer(-999), classMetadata.attributes.sequenceNumber.min
        assertEquals "Integer", classMetadata.attributes.sequenceNumber.propertyType
        assertEquals "SCRCLBD_CRSE_NUMB", classMetadata.attributes.courseNumber.columnName
        assertEquals 5, classMetadata.attributes.courseNumber.maxSize
        assertEquals "String", classMetadata.attributes.courseNumber.propertyType

        // Term
        classMetadata = domainAttributePropertiesService.extractClassMetadataByName("net.hedtech.banner.testing.TermForTesting")
        assertNotNull classMetadata
        assertEquals "STVTERM_CODE", classMetadata.attributes.code.columnName
        assertEquals 6, classMetadata.attributes.code.maxSize

        assertEquals "STVTERM_ACYR_CODE", classMetadata.attributes.academicYear.columnName
        assertEquals 4, classMetadata?.attributes?.academicYear?.maxSize, 1e-8
        assertEquals "AcademicYearForTesting", classMetadata?.attributes?.academicYear?.propertyType

        assertEquals "STVTERM_ACTIVITY_DATE", classMetadata.attributes.lastModified.columnName
        assertEquals 11, classMetadata?.attributes?.lastModified?.maxSize, 1e-8
        assertEquals "Date", classMetadata?.attributes?.lastModified?.propertyType

        assertEquals "STVTERM_FA_END_PERIOD", classMetadata.attributes.financialEndPeriod?.columnName
//        assertEquals 22, classMetadata?.attributes?.financialEndPeriod?.maxSize
        assertEquals "Integer", classMetadata?.attributes?.financialEndPeriod?.propertyType
        assertNull classMetadata.attributes.financialEndPeriod?.max
        assertNull classMetadata.attributes.financialEndPeriod?.min
        classMetadata = domainAttributePropertiesService.extractClassMetadataByName("net.hedtech.banner.testing.Foo")
        assertNotNull classMetadata
        classMetadata = domainAttributePropertiesService.extractClassMetadataByName("net.hedtech.banner.testing.CourseLaborDistributionForTesting")
        assertNotNull classMetadata

    }

    @Test
    void testExtractClassMetadataByNullId() {
        def classMetadata
        classMetadata = domainAttributePropertiesService.extractClassMetadataById(null)
        assertNull classMetadata
    }
    @Test
    void testExtractClassMetadataByEmptyId() {
        def classMetadata
        classMetadata = domainAttributePropertiesService.extractClassMetadataById("")
        assertNull classMetadata
    }
    @Test
    void testExtractClassMetadataByInvalidId() {
        def classMetadata
        classMetadata = domainAttributePropertiesService.extractClassMetadataById("invalid testing")
        assertNull classMetadata
    }

}

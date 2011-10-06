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

package com.sungardhe.banner.general.utility

import com.sungardhe.banner.testing.BaseIntegrationTestCase
import com.sungardhe.banner.testing.InstructorQueryView

class DomainAttributePropertiesServiceIntegrationTests extends BaseIntegrationTestCase {

    def domainAttributePropertiesService

    protected void setUp() {
        formContext = ['STVCOLL']
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testGetClassMetadataByEntityNameWithoutConstraintProperties() {

         def classMetadata
         // facultyScheduleQueryView
         classMetadata = domainAttributePropertiesService.extractClassMetadataByName("facultyScheduleQueryView")

         assertNotNull classMetadata
         assertEquals "SIVASGQ_END_TIME", classMetadata.attributes.endTime.columnName
         assertEquals 4, classMetadata.attributes.endTime.maxSize
         assertEquals "String", classMetadata.attributes.endTime.propertyType

     }


     void testGetClassMetadataByPojo() {

         def classMetadata
         // InstructorQueryView
         def instructorQueryView = new InstructorQueryView()

         classMetadata = domainAttributePropertiesService.extractClassMetadataByPojo(instructorQueryView)

         assertNotNull classMetadata
         assertNotNull classMetadata.attributes.facultyContractType
         assertEquals "String", classMetadata.attributes.facultyContractType.propertyType
     }


    void testGetClassMetadataByEntityName() {
        def classMetadata
        // zip
        classMetadata = domainAttributePropertiesService.extractClassMetadataByName("zip")

        assertNotNull classMetadata
        assertEquals "GTVZIPC_CODE", classMetadata.attributes.code.columnName
        assertEquals "GTVZIPC_CITY", classMetadata.attributes.city.columnName
        assertFalse "GTVZIPC_CODE", classMetadata.attributes.city.nullable
        assertEquals 30, classMetadata.attributes.code.maxSize
        assertEquals 50, classMetadata.attributes.city.maxSize
        assertEquals 11, classMetadata.attributes.lastModified.maxSize
        assertEquals "String", classMetadata.attributes.city.propertyType

        // CourseLaborDistribution
        classMetadata = domainAttributePropertiesService.extractClassMetadataByName("courseLaborDistribution")
        assertNotNull classMetadata
        assertEquals "SCRCLBD_SEQ_NO", classMetadata.attributes.sequenceNumber.columnName
        assertEquals new Integer(999), classMetadata.attributes.sequenceNumber.max
        assertEquals new Integer(-999), classMetadata.attributes.sequenceNumber.min
        assertEquals "Integer", classMetadata.attributes.sequenceNumber.propertyType

        assertEquals "SCRCLBD_CRSE_NUMB", classMetadata.attributes.courseNumber.columnName
        assertEquals 5, classMetadata.attributes.courseNumber.maxSize
        assertEquals "String", classMetadata.attributes.courseNumber.propertyType

        // Term
        classMetadata = domainAttributePropertiesService.extractClassMetadataByName("term")
        assertNotNull classMetadata
        assertEquals "STVTERM_CODE", classMetadata.attributes.code.columnName
        assertEquals 6, classMetadata.attributes.code.maxSize

        assertEquals "STVTERM_ACYR_CODE", classMetadata.attributes.academicYear.columnName
        assertEquals 4, classMetadata?.attributes?.academicYear?.maxSize
        assertEquals "AcademicYear", classMetadata?.attributes?.academicYear?.propertyType

        assertEquals "STVTERM_ACTIVITY_DATE", classMetadata.attributes.lastModified.columnName
        assertEquals 11, classMetadata?.attributes?.lastModified?.maxSize
        assertEquals "Date", classMetadata?.attributes?.lastModified?.propertyType

        assertEquals "STVTERM_FA_END_PERIOD", classMetadata.attributes.financialEndPeriod?.columnName
        assertEquals 22, classMetadata?.attributes?.financialEndPeriod?.maxSize
        assertEquals "Integer", classMetadata?.attributes?.financialEndPeriod?.propertyType
        assertNull classMetadata.attributes.financialEndPeriod?.max
        assertNull classMetadata.attributes.financialEndPeriod?.min



        classMetadata = domainAttributePropertiesService.extractClassMetadataByName("foo")
        assertNotNull classMetadata

        classMetadata = domainAttributePropertiesService.extractClassMetadataByName("courseLaborDistribution")
        assertNotNull classMetadata

        classMetadata = domainAttributePropertiesService.extractClassMetadataByName("myZip")
        assertNull classMetadata

        classMetadata = domainAttributePropertiesService.extractClassMetadataByName("studentBlock")
        assertNull classMetadata

    }

    void testGetClassMetadataById() {
        def classMetadata

        // Zip
        classMetadata = domainAttributePropertiesService.extractClassMetadataById("zipBlock")
        assertNotNull classMetadata
        assertEquals "GTVZIPC_CODE", classMetadata.attributes.code.columnName
        assertEquals "GTVZIPC_CITY", classMetadata.attributes.city.columnName
        assertFalse "GTVZIPC_CODE", classMetadata.attributes.city.nullable
        assertEquals 50, classMetadata.attributes.city.maxSize
        assertEquals "String", classMetadata.attributes.city.propertyType

        // Term
        classMetadata = domainAttributePropertiesService.extractClassMetadataById("termBlock")
        assertNotNull classMetadata
        assertEquals "STVTERM_CODE", classMetadata.attributes.code.columnName
        assertEquals 6, classMetadata.attributes.code.maxSize

        assertEquals "STVTERM_ACYR_CODE", classMetadata.attributes.academicYear.columnName
        assertEquals 4, classMetadata?.attributes?.academicYear?.maxSize
        assertEquals "AcademicYear", classMetadata?.attributes?.academicYear?.propertyType

        classMetadata = domainAttributePropertiesService.extractClassMetadataById("fooBlock")
        assertNotNull classMetadata
        assertEquals "STVCOLL_ADDR_STREET_LINE2", classMetadata.attributes.addressStreetLine2.columnName
        assertEquals 75, classMetadata.attributes.addressStreetLine2.maxSize

        classMetadata = domainAttributePropertiesService.extractClassMetadataById("courseLaborDistributionBlock")
        assertNotNull classMetadata

        classMetadata = domainAttributePropertiesService.extractClassMetadataById("zip")
        assertNull classMetadata

        classMetadata = domainAttributePropertiesService.extractClassMetadataById("studentBlock")
        assertNull classMetadata

    }
}  

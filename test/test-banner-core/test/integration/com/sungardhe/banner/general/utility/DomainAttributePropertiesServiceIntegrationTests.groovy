/** *****************************************************************************

 © 2011 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */

package com.sungardhe.banner.general.utility

import com.sungardhe.banner.testing.BaseIntegrationTestCase

class DomainAttributePropertiesServiceIntegrationTests extends BaseIntegrationTestCase {

    def domainAttributePropertiesService

    protected void setUp() {
        formContext = ['STVCOLL']
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
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
        assertEquals 7, classMetadata.attributes.lastModified.maxSize
        assertEquals "String", classMetadata.attributes.city.propertyType

        // CourseLaborDistribution
        classMetadata = domainAttributePropertiesService.extractClassMetadataByName("courseLaborDistribution")
        assertNotNull classMetadata
        assertEquals "SCRCLBD_SEQ_NO", classMetadata.attributes.sequenceNumber.columnName
        assertEquals 999, classMetadata.attributes.sequenceNumber.max
        assertEquals "Integer", classMetadata.attributes.sequenceNumber.propertyType

        // Term
        classMetadata = domainAttributePropertiesService.extractClassMetadataByName("term")
        assertNotNull classMetadata
        assertEquals "STVTERM_CODE", classMetadata.attributes.code.columnName
        assertEquals 6, classMetadata.attributes.code.maxSize

        assertEquals "STVTERM_ACYR_CODE", classMetadata.attributes.academicYear.columnName
        assertEquals 4, classMetadata?.attributes?.academicYear?.maxSize
        assertEquals "AcademicYear", classMetadata?.attributes?.academicYear?.propertyType

        assertEquals "STVTERM_ACTIVITY_DATE", classMetadata.attributes.lastModified.columnName
        assertEquals 7, classMetadata?.attributes?.lastModified?.maxSize
        assertEquals "Date", classMetadata?.attributes?.lastModified?.propertyType


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

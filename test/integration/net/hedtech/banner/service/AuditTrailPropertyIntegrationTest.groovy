/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.service

import net.hedtech.banner.testing.AcademicYearForTesting
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.Before
import org.junit.After
import org.junit.Test

class AuditTrailPropertyIntegrationTest extends BaseIntegrationTestCase {


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
    void testAuditTrailFields () {
        AcademicYearForTesting newAcademicYear = createNewAcademicYearForTesting()
        newAcademicYear.save(failOnError: true, flush: true)
        assertNotNull newAcademicYear.id
        assertEquals 0L, newAcademicYear.version

        assertEquals 'GRAILS_USER', newAcademicYear.lastModifiedBy
        assertNotNull newAcademicYear.lastModified
        assertEquals 'Banner', newAcademicYear.dataOrigin
    }


    private AcademicYearForTesting createNewAcademicYearForTesting() {
        AcademicYearForTesting newAcademicYear = new AcademicYearForTesting(
                code: 'TEST',
                description: 'TEST_DESCRIPTION',
                sysreqInd: true
        )
        return newAcademicYear
    }

}
/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.service

import net.hedtech.banner.testing.AcademicYearForTesting
import net.hedtech.banner.testing.BaseIntegrationTestCase
import net.hedtech.banner.testing.FacultyScheduleQueryViewForTesting
import net.zorched.test.Address
//import org.codehaus.groovy.grails.validation.ConstrainedProperty
import grails.gorm.validation.ConstrainedProperty
import org.junit.Before
import org.junit.After
import org.junit.Test
import org.springframework.dao.InvalidDataAccessResourceUsageException

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


    @Test
    void testAuditTrailFieldsWithoutLogin () {
        logout()
        AcademicYearForTesting newAcademicYear = createNewAcademicYearForTesting()
        newAcademicYear.save(failOnError: true, flush: true)
        assertNotNull newAcademicYear.id
        assertEquals 0L, newAcademicYear.version

        assertEquals 'ANONYMOUS', newAcademicYear.lastModifiedBy
        assertNotNull newAcademicYear.lastModified
        assertEquals 'Banner', newAcademicYear.dataOrigin
    }


    @Test
    void testAuditTrailFieldsWithDifferentUser () {
        logout()
        AcademicYearForTesting newAcademicYear = createNewAcademicYearForTesting()
        newAcademicYear.setLastModifiedBy('banner')
        newAcademicYear.save(failOnError: true, flush: true)
        assertNotNull newAcademicYear.id
        assertEquals 0L, newAcademicYear.version

        assertEquals 'BANNER', newAcademicYear.lastModifiedBy
        assertNotNull newAcademicYear.lastModified
        assertEquals 'Banner', newAcademicYear.dataOrigin
    }


    @Test
    void testAuditTrailFieldsForViews () {
        FacultyScheduleQueryViewForTesting facultyScheduleQueryView = FacultyScheduleQueryViewForTesting.findAll()[0]
        assertNotNull facultyScheduleQueryView.id

        facultyScheduleQueryView.setMaximumEnrollment(100)
        shouldFail(InvalidDataAccessResourceUsageException) {
            facultyScheduleQueryView.save(failOnError: true, flush: true)
        }

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
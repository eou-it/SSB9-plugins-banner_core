/*******************************************************************************
Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
*******************************************************************************/ 
package net.hedtech.banner.dataextract

import groovy.sql.Sql
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

class DataExtractServiceIntegrationTests extends BaseIntegrationTestCase {

    def dataExtractService

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        updateGUBOBJSTable()
    }
	
	@After
    public void tearDown() {
        super.tearDown()
    }

    @Test
    void testDataExtract() {
        def dataExtractScheduleEvaluation = dataExtractService.hasDataExtract("scheduleEvaluation")
        assertNull "DataExtract is not setup", dataExtractScheduleEvaluation

        def dataExtractSchedule = dataExtractService.hasDataExtract("schedule")
        assertEquals "B", dataExtractSchedule

        def dataExtractScheduleOverride = dataExtractService.hasDataExtract("scheduleOverride")
         assertEquals "D", dataExtractScheduleOverride


        def dataExtractPageDoesNotExist = dataExtractService.hasDataExtract("pageDoesNotExist")
        assertNull "DataExtract is not setup", dataExtractPageDoesNotExist
    }


    public def updateGUBOBJSTable() {
        def sql
        try {
            sql = new Sql(sessionFactory.getCurrentSession().connection())
            sql.executeUpdate("""
                 UPDATE GUBOBJS
                SET GUBOBJS_EXTRACT_ENABLED_IND = 'B'
              WHERE GUBOBJS_NAME = 'SSASECT'
            """)


            sql = new Sql(sessionFactory.getCurrentSession().connection())
             sql.executeUpdate("""
                  UPDATE GUBOBJS
                 SET GUBOBJS_EXTRACT_ENABLED_IND = 'D'
               WHERE GUBOBJS_NAME = 'SSAOVRR'
             """)


            sql.executeUpdate("""
                 UPDATE GUBOBJS
                SET GUBOBJS_EXTRACT_ENABLED_IND = 'N'
              WHERE GUBOBJS_NAME = 'SSAEVAL'
            """)
        }
        finally {
            sql?.close()  // note that the test will close the connection, since it's our current session's connection
        }
    }
}

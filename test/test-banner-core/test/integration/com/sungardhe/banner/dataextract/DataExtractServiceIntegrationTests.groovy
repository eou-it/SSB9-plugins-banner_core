/** *******************************************************************************
 Copyright 2009-2011 SunGard Higher Education. All Rights Reserved.
 This copyrighted software contains confidential and proprietary information of 
 SunGard Higher Education and its subsidiaries. Any use of this software is limited 
 solely to SunGard Higher Education licensees, and is further subject to the terms 
 and conditions of one or more written license agreements between SunGard Higher 
 Education and the licensee in question. SunGard is either a registered trademark or
 trademark of SunGard Data Systems in the U.S.A. and/or other regions and/or countries.
 Banner and Luminis are either registered trademarks or trademarks of SunGard Higher 
 Education in the U.S.A. and/or other regions and/or countries.
 ********************************************************************************* */
package com.sungardhe.banner.dataextract

import com.sungardhe.banner.testing.BaseIntegrationTestCase
import org.springframework.security.core.context.SecurityContextHolder as SCH
import groovy.sql.Sql

class DataExtractServiceIntegrationTests extends BaseIntegrationTestCase {

    def dataExtractService

    protected void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        updateGUBOBJSTable()
    }



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

    private def updateGUBOBJSTable() {
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
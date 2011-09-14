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
package com.sungardhe.banner.menu

import com.sungardhe.banner.testing.BaseIntegrationTestCase
import groovy.sql.Sql

class OptionMenuServiceIntegrationTests extends BaseIntegrationTestCase {

    def optionMenuService

    protected void setUp() {
        formContext = ['SCACRSE']
        super.setUp()
        dataSetup()
    }

    void testOptionMenuForPage() {
        def list
        list = optionMenuService.optionMenuForPage("basicCourseInformation")
        assertNotNull list

        def mnu = new OptionMenu()
        for (i in 0..(list.size() - 1)) {
            if (list.get(i).calledFormName == "SCARRES") {
                mnu = list.get(i)
                break;
            }
        }

        assertEquals 8, mnu.seq
        assertEquals "Reg. Restrictions[SCARRES]", mnu.menuDesc
        assertEquals "SCACRSE", mnu.formName
        assertEquals "SCARRES", mnu.calledFormName
        assertEquals "courseRegistrationRestrictions",mnu.pageName 
        assertEquals 7, list.size()
    }


    void testOptionMenuForBlock() {
        def list
        list = optionMenuService.optionMenuForBlock("basicCourseInformation", "zipBlock")
        assertNotNull list
        assertEquals 1, list.size()

        def mnu = list.get(0)

        assertEquals 7, mnu.seq
        assertEquals "Course Details[SCADETL]", mnu.menuDesc
        assertEquals "SCACRSE", mnu.formName
        assertEquals "SCADETL", mnu.calledFormName
        assertEquals "courseDetailInformation", mnu.pageName
    }

    private def dataSetup() {
        def sql = new Sql(sessionFactory.getCurrentSession().connection())

        sql.executeUpdate("""
        UPDATE GUROPTM
           SET GUROPTM_BLOCK_VALID = 'GTVZIPC'
         WHERE GUROPTM_FORM_NAME = 'SCACRSE'
           AND GUROPTM_FORM_TO_BE_CALLED = 'SCADETL'
      """)

    }
}
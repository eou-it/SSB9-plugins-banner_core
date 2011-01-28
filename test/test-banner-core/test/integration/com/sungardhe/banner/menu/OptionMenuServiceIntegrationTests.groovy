/** *****************************************************************************

 © 2011 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
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
/** *****************************************************************************

 © 2010 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
package com.sungardhe.banner.mep

import com.sungardhe.banner.testing.BaseIntegrationTestCase
import groovy.sql.Sql
import org.springframework.security.core.context.SecurityContextHolder as SCH

class MultiEntityProcessingServiceIntegrationTests  extends BaseIntegrationTestCase {

    def multiEntityProcessingService

    protected void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        setMepLogon()
    }


    void testIsMEP() {
        assertTrue "MEP is not setup", multiEntityProcessingService.isMEP()
    }

    void testSetHomeContext() {
        multiEntityProcessingService.setHomeContext("FOO")

        def homeContext
        homeContext = multiEntityProcessingService.getHomeContext()
        assertEquals "FOO", homeContext
    }

    void testSetProcessContext() {
        multiEntityProcessingService.setProcessContext("FOO")

        def processContext
        processContext = multiEntityProcessingService.getProcessContext()
        assertEquals "FOO", processContext
    }

    void testSetMepOnAccess() {
        multiEntityProcessingService.setMepOnAccess("GRAILS_USER")

        def homeContext = multiEntityProcessingService.getHomeContext()
        assertEquals "BANNER", homeContext

        def processContext = multiEntityProcessingService.getProcessContext()
        assertEquals "BANNER", processContext
    }


    void testChangeProcessContext() {
        multiEntityProcessingService.setMepOnAccess("GRAILS_USER")

        def homeContext = multiEntityProcessingService.getHomeContext()
        assertEquals "BANNER", homeContext

        def processContext = multiEntityProcessingService.getProcessContext()
        assertEquals "BANNER", processContext

        multiEntityProcessingService.setProcessContext("FOO")

        def processContextChanged = multiEntityProcessingService.getProcessContext()
        assertEquals "FOO", processContextChanged
    }


    void testGetUserHomeCodes() {
        def homes
        homes = multiEntityProcessingService.getUserHomeCodes("grails_user")
        assertTrue homes.size() == 2
        assertEquals "BANNER", homes[0].code
        assertEquals "Banner College", homes[0].desc
        assertTrue "Default MEP error", homes[0].default
    }

    void testGetMepCodes() {
        def homes
        homes = multiEntityProcessingService.getMepCodes()
        assertTrue homes.size() == 2
        assertTrue homes[0].code == "BANNER"
    }

    void testGetUserHomesCount() {
        assertTrue multiEntityProcessingService.getUserHomesCount("grails_user") == 2
    }


    void testResetHomeContext() {
        multiEntityProcessingService.resetUserHomeContext("FOO")
        assertEquals "FOO",  SCH.context?.authentication?.principal?.mepHomeContext
    }


    void testMep() {
        def mep = multiEntityProcessingService.hasMep("collegeAndDepartmentText")
        assertTrue "Mep is not set up correctly",  mep
    }

    void testGetMepDescription() {
        def desc = multiEntityProcessingService.getMepDescription("BANNER")
        assertEquals "Banner College",  desc
    }


    private setMepLogon() {
        Sql sql = new Sql(sessionFactory.getCurrentSession().connection())
        try {
            /*
            sql.executeInsert(
                    """
                             Insert into GTVVPDI
	                         (GTVVPDI_CODE, GTVVPDI_DESC, GTVVPDI_TYPE_CDE, GTVVPDI_SYS_DEF_INST_IND, GTVVPDI_ACTIVITY_DATE)
	                         Values('INST', 'Institution', 'I', 'Y', SYSDATE)
                          """)
                          */

            sql.call("{call  g\$_vpdi_security.g\$_vpdi_set_home_context('LOGON')}")
            sql.call("{call  g\$_vpdi_security.g\$_vpdi_set_process_context('LOGON','LOGON')}")
        } catch (e) {
            println "ERROR: Could not establish mif context. ${e.message}"
        }
    }
}
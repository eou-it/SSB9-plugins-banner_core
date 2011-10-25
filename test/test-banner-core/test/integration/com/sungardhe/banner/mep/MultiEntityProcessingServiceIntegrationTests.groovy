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
package com.sungardhe.banner.mep

import com.sungardhe.banner.testing.BaseIntegrationTestCase
import groovy.sql.Sql
import org.springframework.security.core.context.SecurityContextHolder as SCH
import org.springframework.web.context.request.RequestContextHolder

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

    void testSsbMep() {
        try {
            setMepSsb()
            fail "Failed Mep SSB validation."
        } catch (Exception e) {
           // should be RuntimeException for mep
        }
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

       private setMepSsb() {

        if (multiEntityProcessingService?.isMEP()) {
            if (!RequestContextHolder.currentRequestAttributes()?.request?.session?.getAttribute("mep")) {
                throw new RuntimeException("The Mep Code must be provided when running in multi institution context")
            }

            def desc = multiEntityProcessingService?.getMepDescription(RequestContextHolder.currentRequestAttributes()?.request?.session?.getAttribute("mep"))


            if (!desc) {
                throw new RuntimeException("Mep Code is invalid")
            } else {
                RequestContextHolder.currentRequestAttributes()?.request?.session.setAttribute("ssbMepDesc", desc)
                multiEntityProcessingService?.setHomeContext(RequestContextHolder.currentRequestAttributes()?.request?.session?.getAttribute("mep"))
                multiEntityProcessingService?.setProcessContext(RequestContextHolder.currentRequestAttributes()?.request?.session?.getAttribute("mep"))
            }
        }
    }
}
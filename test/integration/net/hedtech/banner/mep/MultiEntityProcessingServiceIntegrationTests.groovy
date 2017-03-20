/*******************************************************************************
Copyright 2009-2016 Ellucian Company L.P. and its affiliates.
*******************************************************************************/
package net.hedtech.banner.mep

import grails.util.Holders
import groovy.sql.Sql
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.security.core.context.SecurityContextHolder as SCH
import org.springframework.web.context.request.RequestContextHolder

class MultiEntityProcessingServiceIntegrationTests  extends BaseIntegrationTestCase {

    def multiEntityProcessingService
    String aaaCollege = "Test1"
    String bbbCollege = "Test2"
    String cccCollege = "Test3"
    final String URL=Holders.config.bannerDataSource.url
    final String BANSECR_USERNAME="bansecr"
    final String BANSECR_PASSWORD="u_pick_it"
    String defaultInstitution
    String defaultInstitutionForUser

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        retrieveDefaultInsitution()
        setMepLogon(aaaCollege)
        //remove the mepEnabled indicator from the session
        RequestContextHolder.currentRequestAttributes().request.session.servletContext.removeAttribute('mepEnabled')
    }

    @After
    public void tearDown() {
        println "*****************************************************************"
        println RequestContextHolder.currentRequestAttributes().request.session
        println RequestContextHolder.currentRequestAttributes().request.session.servletContext.getAttribute('mepEnabled')
        println "*****************************************************************"
        //RequestContextHolder.currentRequestAttributes().request.session.servletContext.removeAttribute('mepEnabled')
        super.tearDown()
    }

    @Test
    void testChangeProcessContext() {
        multiEntityProcessingService.resetUserHomeContext(cccCollege)
        multiEntityProcessingService.setMepOnAccess("GRAILS_USER")

        def homeContext = multiEntityProcessingService.getHomeContext()
        assertEquals cccCollege, homeContext

        def processContext = multiEntityProcessingService.getProcessContext()
        assertEquals cccCollege, processContext

        multiEntityProcessingService.setProcessContext(aaaCollege)

        def processContextChanged = multiEntityProcessingService.getProcessContext()
        assertEquals aaaCollege, processContextChanged

        multiEntityProcessingService.resetUserProcessContext(cccCollege)
    }

    @Test
    void testGetUserHomeCodes() {
        multiEntityProcessingService.resetUserHomeContext(cccCollege)
        def homes
        homes = multiEntityProcessingService.getUserHomeCodes("grails_user")
        assertTrue homes.size() >= 3
        assertTrue homes.contains([code:cccCollege,desc:"Banner College Test3", default:true])
    }

    @Test
    void testGetMepCodes() {
        def homes
        homes = multiEntityProcessingService.getMepCodes()
        assertTrue homes.size() >= 3
        assertTrue homes.contains([code:aaaCollege,desc:"Banner College Test1"])
    }

    @Test
    void testGetUserHomesCount() {
        assertTrue multiEntityProcessingService.getUserHomesCount("grails_user") >=3
    }

    @Test
    void testResetHomeContext() {
        multiEntityProcessingService.resetUserHomeContext(cccCollege)
        assertEquals cccCollege,  SCH.context?.authentication?.principal?.mepHomeContext
    }

    @Test
    void testGetMepDescription() {
        def desc = multiEntityProcessingService.getMepDescription(cccCollege)
        assertEquals "Banner College Test3",  desc

        def con = sessionFactory.getCurrentSession().connection()
        desc = multiEntityProcessingService.getMepDescription(cccCollege, con)
        assertEquals "Banner College Test3",  desc
        if (con) con.close()
    }

    @Test
    void testGetMepDescriptionSsbUser() {
        def mepDescBanner = 'BANNER'
        def desc = multiEntityProcessingService.getMepDescriptionSsbUser(mepDescBanner)
        assertEquals "Banner College",  desc
    }

    @Test
    void testSsbMep() {
        try {
            setMepSsb()
            fail "Failed Mep SSB validation."
        } catch (Exception e) {
           // should be RuntimeException for mep
        }
    }


    @Test
    void testIsMEP() {
        //remove the mepEnabled indicator from the session so that the service is forced to check for mep indicator
        RequestContextHolder.currentRequestAttributes().request.session.servletContext.removeAttribute('mepEnabled')
        assertTrue "MEP is not setup", multiEntityProcessingService.isMEP()
        //remove the mepEnabled indicator from the session
        RequestContextHolder.currentRequestAttributes().request.session.servletContext.removeAttribute('mepEnabled')

        try {
            multiEntityProcessingService.isMEP(sessionFactory.getCurrentSession().connection())
        } catch (Exception e) {
            fail()
        }
    }


    @Test
    void testIsMEPEnabledTrue() {
        RequestContextHolder.currentRequestAttributes().request.session.servletContext.setAttribute('mepEnabled', true)
        assertTrue "MEP is not setup", multiEntityProcessingService.isMEP()
        assertTrue RequestContextHolder.currentRequestAttributes().request.session.servletContext.getAttribute('mepEnabled')
        //remove the mepEnabled indicator from the session
        RequestContextHolder.currentRequestAttributes().request.session.servletContext.removeAttribute('mepEnabled')
    }


    @Test
    void testIsMEPEnabledFalse() {
        //remove the mepEnabled indicator from the session so that the service is forced to check for mep indicator
        RequestContextHolder.currentRequestAttributes().request.session.servletContext.removeAttribute('mepEnabled')
        //MEP should be enabled when we come into this test
        assertTrue "MEP is not setup", multiEntityProcessingService.isMEP()

        //now set the session indicator to false
        RequestContextHolder.currentRequestAttributes().request.session.servletContext.setAttribute('mepEnabled', false)
        //the service will not perform the JDBC call because it already has mepEnabled in the session
        assertFalse "MEP is setup", multiEntityProcessingService.isMEP()
        assertFalse RequestContextHolder.currentRequestAttributes().request.session.servletContext.getAttribute('mepEnabled')
        //remove the mepEnabled indicator from the session
        RequestContextHolder.currentRequestAttributes().request.session.servletContext.removeAttribute('mepEnabled')
    }


    @Test
    void testIsMEPWithConnection() {
        //remove the mepEnabled indicator from the session so that the service is forced to check for mep indicator
        RequestContextHolder.currentRequestAttributes().request.session.servletContext.removeAttribute('mepEnabled')
        Sql sql = new Sql(sessionFactory.getCurrentSession().connection())
        assertTrue "MEP is not setup", multiEntityProcessingService.isMEP(sql)
        //remove the mepEnabled indicator from the session
        RequestContextHolder.currentRequestAttributes().request.session.servletContext.removeAttribute('mepEnabled')
    }


    @Test
    void testIsMEPWithConnectionEnabledTrue() {
        RequestContextHolder.currentRequestAttributes().request.session.servletContext.setAttribute('mepEnabled', true)
        Sql sql = new Sql(sessionFactory.getCurrentSession().connection())
        assertTrue "MEP is not setup", multiEntityProcessingService.isMEP(sql)
        assertTrue RequestContextHolder.currentRequestAttributes().request.session.servletContext.getAttribute('mepEnabled')
        //remove the mepEnabled indicator from the session
        RequestContextHolder.currentRequestAttributes().request.session.servletContext.removeAttribute('mepEnabled')
    }


    @Test
    void testIsMEPWithConnectionEnabledFalse() {
        //remove the mepEnabled indicator from the session so that the service is forced to check for mep indicator
        RequestContextHolder.currentRequestAttributes().request.session.servletContext.removeAttribute('mepEnabled')
        Sql sql = new Sql(sessionFactory.getCurrentSession().connection())
        //MEP should be enabled when we come into this test
        assertTrue "MEP is not setup", multiEntityProcessingService.isMEP(sql)

        //now set the session indicator to false
        RequestContextHolder.currentRequestAttributes().request.session.servletContext.setAttribute('mepEnabled', false)
        //the service will not perform the JDBC call because it already has mepEnabled in the session
        assertFalse "MEP is setup", multiEntityProcessingService.isMEP(sql)
        assertFalse RequestContextHolder.currentRequestAttributes().request.session.servletContext.getAttribute('mepEnabled')
        //remove the mepEnabled indicator from the session
        RequestContextHolder.currentRequestAttributes().request.session.servletContext.removeAttribute('mepEnabled')
    }


    @Test
    void testSetHomeContext() {
        multiEntityProcessingService.setHomeContext(aaaCollege)

        def homeContext
        homeContext = multiEntityProcessingService.getHomeContext()
        assertEquals aaaCollege, homeContext

        multiEntityProcessingService.setHomeContext(null)
        homeContext = multiEntityProcessingService.getHomeContext()
        assertNull(homeContext)

        def con = sessionFactory.getCurrentSession().connection()
        multiEntityProcessingService.setHomeContext(aaaCollege, con)
        homeContext = multiEntityProcessingService.getHomeContext(con)
        assertEquals aaaCollege, homeContext
        if (con) con.close()
    }

    @Test
    public void testSetHomeContextSsbUser () {
        multiEntityProcessingService.setHomeContextSsbUser(aaaCollege)

        def homeContext
        homeContext = multiEntityProcessingService.getHomeContext()
        assertEquals aaaCollege, homeContext
    }

    @Test
    void testSetProcessContext() {
        multiEntityProcessingService.setProcessContext(aaaCollege)

        def processContext
        processContext = multiEntityProcessingService.getProcessContext()
        assertEquals aaaCollege, processContext

        def con = sessionFactory.getCurrentSession().connection()
        multiEntityProcessingService.setProcessContext(aaaCollege, con)
        processContext = multiEntityProcessingService.getProcessContext()
        assertEquals aaaCollege, processContext
        if (con) con.close()

    }

    @Test
    public void testProcessContextSsbUser () {
        multiEntityProcessingService.setProcessContextSsbUser(aaaCollege)
        def processContext
        processContext = multiEntityProcessingService.getProcessContext()
        assertEquals aaaCollege, processContext
    }


    @Test
    void testSetMepOnAccess() {
        multiEntityProcessingService.resetUserHomeContext(bbbCollege)
        multiEntityProcessingService.setMepOnAccess("GRAILS_USER")

        def homeContext = multiEntityProcessingService.getHomeContext()
        assertEquals bbbCollege, homeContext

        def processContext = multiEntityProcessingService.getProcessContext()
        assertEquals bbbCollege, processContext

        def con = sessionFactory.getCurrentSession().connection()
        multiEntityProcessingService.setMepOnAccess("GRAILS_USER", con)
        homeContext = multiEntityProcessingService.getHomeContext()
        assertEquals bbbCollege, homeContext
        if (con) con.close()
    }

    private setMepLogon(String mepCode) {
        Sql sql = new Sql(sessionFactory.getCurrentSession().connection())
        try {
            sql.call("{call  g\$_vpdi_security.g\$_vpdi_set_home_context('${mepCode}')}")
            sql.call("{call  g\$_vpdi_security.g\$_vpdi_set_process_context('${mepCode}','LOGON')}")
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


    private void retrieveDefaultInsitution() {
        def sql
        try {
            sql = new Sql(sessionFactory.getCurrentSession().connection())
            sql.eachRow("select * from gtvvpdi where gtvvpdi_sys_def_inst_ind = 'Y'", {this.defaultInstitution = it.GTVVPDI_CODE })
            sql.eachRow("select * from bansecr.gurusri where gurusri_vpdi_user_id = 'GRAILS_USER' and gurusri_user_def_inst_ind='Y'", {this.defaultInstitutionForUser = it.GURUSRI_VPDI_CODE })
            sql.commit()
        } finally {
            sql?.close()  // note that the test will close the connection, since it's our current session's connection
        }
    }

}

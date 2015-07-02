/*******************************************************************************
Copyright 2009-2015 Ellucian Company L.P. and its affiliates.
*******************************************************************************/
package net.hedtech.banner.mep

import groovy.sql.Sql

import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.junit.After
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.springframework.security.core.context.SecurityContextHolder as SCH
import org.springframework.web.context.request.RequestContextHolder

import org.junit.Ignore

class MultiEntityProcessingServiceIntegrationTests  extends BaseIntegrationTestCase {

    def multiEntityProcessingService
    String aaaCollege = "aaa"
    String bbbCollege = "bbb"
    String cccCollege = "ccc"
    final String URL=ConfigurationHolder.config.bannerDataSource.url
    final String BANSECR_USERNAME="bansecr"
    final String BANSECR_PASSWORD="u_pick_it"
    String defaultInstitution
    String defaultInstitutionForUser

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
       retrieveDefaultInsitution()
        updateOtherInstitutionToNonDefault()
        createNewMepCodesInDB([[mepCode: aaaCollege,mepDesc:'aaa college',mepTypCode:'a',defIndicator:'Y'],
                               [mepCode: bbbCollege,mepDesc:'bbb college',mepTypCode:'b',defIndicator:'N'],
                               [mepCode: cccCollege,mepDesc:'ccc college',mepTypCode:'c',defIndicator:'N']])
        setMepLogon(aaaCollege)
    }

    @After
    public void tearDown() {
        deleteNewMepCodesInDB([aaaCollege, bbbCollege, cccCollege])
        super.tearDown()
        restoreOtherInstitutionToNonDefault()
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
    }

    @Test
    void testGetUserHomeCodes() {
        multiEntityProcessingService.resetUserHomeContext(cccCollege)
        def homes
        homes = multiEntityProcessingService.getUserHomeCodes("grails_user")
        assertTrue homes.size() >= 3
        assertTrue homes.contains([code:cccCollege,desc:"ccc college", default:true])
    }

    @Test
    void testGetMepCodes() {
        def homes
        homes = multiEntityProcessingService.getMepCodes()
        assertTrue homes.size() >=3
        assertTrue homes.contains([code:aaaCollege,desc:"aaa college"])
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
    void testMep() {
        def mep = multiEntityProcessingService.hasMep("collegeAndDepartmentText")
        assertTrue "Mep is not set up correctly",  mep
    }


    @Test
    void testGetMepDescription() {
        def desc = multiEntityProcessingService.getMepDescription(cccCollege)
        assertEquals "ccc college",  desc
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
    }

    @Test
    void testSetProcessContext() {
        multiEntityProcessingService.setProcessContext(bbbCollege)

        def processContext
        processContext = multiEntityProcessingService.getProcessContext()
        assertEquals bbbCollege, processContext
    }


    @Test
    void testSetMepOnAccess() {
        multiEntityProcessingService.resetUserHomeContext(bbbCollege)
        multiEntityProcessingService.setMepOnAccess("GRAILS_USER")

        def homeContext = multiEntityProcessingService.getHomeContext()
        assertEquals bbbCollege, homeContext

        def processContext = multiEntityProcessingService.getProcessContext()
        assertEquals bbbCollege, processContext
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
        //remove the mepEnabled indicator from the session
        RequestContextHolder.currentRequestAttributes().request.session.servletContext.removeAttribute('mepEnabled')
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


    private void createNewMepCodesInDB(List mepObjs) {
        def sql
        try {
            sql = Sql.newInstance(URL,
                    BANSECR_USERNAME,
                    BANSECR_PASSWORD,
                    'oracle.jdbc.driver.OracleDriver')
            mepObjs.each { it ->
                sql.executeInsert("insert into gtvvpdi values (${it.mepCode}, ${it.mepDesc}, ${it.mepTypCode},${it.defIndicator},sysdate,null)")
                sql.executeInsert("insert into gurusri values ('GRAILS_USER', ${it.mepCode}, ${it.defIndicator},sysdate,'GRAILS','GRAILS')")
                sql.commit()
            }
        }  finally {
            sql?.close()  // note that the test will close the connection, since it's our current session's connection
        }
    }

    private void deleteNewMepCodesInDB(List codes) {
        def sql
        def rowcount
        def usercount
        try {
            sql = Sql.newInstance(URL,
                    BANSECR_USERNAME,
                    BANSECR_PASSWORD,
                    'oracle.jdbc.driver.OracleDriver')
            sql.eachRow("select count(*) as a from gtvvpdi where gtvvpdi_code in (?,?,?)",codes, {rowcount = it.a })
            sql.eachRow("select count(*) as b from gurusri where  GURUSRI_VPDI_CODE in (?,?,?)",codes , {usercount = it.b })
            sql.executeUpdate("delete from gurusri where GURUSRI_VPDI_CODE in (?,?,?) and gurusri_vpdi_user_id='GRAILS_USER'",codes)
            sql.executeUpdate("delete from gtvvpdi where gtvvpdi_code in (?,?,?)",codes)
            sql.commit()
        }  finally {
            sql?.close()  // note that the test will close the connection, since it's our current session's connection
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

    private void updateOtherInstitutionToNonDefault() {
        def sql
        try {
            sql = Sql.newInstance(URL,
                    BANSECR_USERNAME,
                    BANSECR_PASSWORD,
                    'oracle.jdbc.driver.OracleDriver')
            sql.executeUpdate("update gtvvpdi set gtvvpdi_sys_def_inst_ind='N'")
            sql.executeUpdate("update gurusri set gurusri_user_def_inst_ind='Y' where gurusri_vpdi_user_id = 'GRAILS_USER'")
            sql.commit()
        } finally {
            sql?.close()  // note that the test will close the connection, since it's our current session's connection
        }
    }

    private void restoreOtherInstitutionToNonDefault() {
        def sql
        try {
            sql = Sql.newInstance(URL,
                    BANSECR_USERNAME,
                    BANSECR_PASSWORD,
                    'oracle.jdbc.driver.OracleDriver')
            sql.executeUpdate("update gtvvpdi set gtvvpdi_sys_def_inst_ind='Y' where gtvvpdi_code='${this.defaultInstitution}'")
            sql.executeUpdate("update gurusri set gurusri_user_def_inst_ind='Y' where gurusri_vpdi_user_id = 'GRAILS_USER' and gurusri_vpdi_code='${this.defaultInstitutionForUser}'")
            sql.commit()
        } finally {
            sql?.close()  // note that the test will close the connection, since it's our current session's connection
        }
    }
}

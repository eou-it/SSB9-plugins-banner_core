/*******************************************************************************
 Copyright 2009-2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

package net.hedtech.banner.security

import grails.spring.BeanBuilder
import grails.util.Holders
import groovy.sql.Sql
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.apache.commons.dbcp.BasicDataSource
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.context.ApplicationContext
import org.springframework.mock.web.MockFilterChain
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder

class BannerSelfServicePreAuthenticatedFilterIntegrationTests extends BaseIntegrationTestCase {

    def bannerPreAuthenticatedFilter
    def gobeaccUserName = 'GRAILS_USER'
    def conn
    def bannerPIDM

    public static final String UDC_IDENTIFIER = 'INTEGRATION_TEST_SAML'

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        ApplicationContext testSpringContext = createUnderlyingSsbDataSourceBean()
        dataSource.underlyingSsbDataSource = testSpringContext.getBean("underlyingSsbDataSource")
        conn = dataSource.getSsbConnection()
        bannerPIDM = getBannerPIDM()
    }

    @After
    public void tearDown() {
        super.tearDown()
        conn?.close()
    }

    @Test
    void testSelfServiceDoFilter() {
        Holders.config.ssbEnabled = true
        Holders?.config.grails.plugin.springsecurity.interceptUrlMap.remove("/**")
        Holders?.config.grails.plugin.springsecurity.interceptUrlMap.put('/ssb/**', ['ROLE_SELFSERVICE-STUDENT_BAN_DEFAULT_M', 'ROLE_SELFSERVICE-GUEST_BAN_DEFAULT_M'])

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/ssb/foo");

        request.addHeader("UDC_IDENTIFIER", UDC_IDENTIFIER)
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        bannerPreAuthenticatedFilter.doFilter(request, response, chain);

        assertEquals SecurityContextHolder.context.getAuthentication().user.pidm, Integer.valueOf(bannerPIDM.intValue())

        assertNotNull(SecurityContextHolder.context.getAuthentication())
        Holders.config.ssbEnabled = false
    }

    //----------------------------- Helper Methods ------------------------------

    private ApplicationContext createUnderlyingSsbDataSourceBean() {
        def bb = new BeanBuilder()
        bb.beans {
            underlyingSsbDataSource(BasicDataSource) {
                maxActive = 5
                maxIdle = 2
                defaultAutoCommit = "false"
                driverClassName = "${Holders.config.bannerSsbDataSource.driver}"
                url = "${Holders.config.bannerSsbDataSource.url}"
                password = "${Holders.config.bannerSsbDataSource.password}"
                username = "${Holders.config.bannerSsbDataSource.username}"
            }
        }
        ApplicationContext testSpringContext = bb.createApplicationContext()
        return testSpringContext
    }

    private def getBannerPIDM() {
        Sql sqlObj = new Sql(conn)
        ''
        String pidmQuery = """SELECT GOBEACC_PIDM FROM GOBEACC WHERE GOBEACC_USERNAME = ?"""
        def bPIDM = 0
        sqlObj.eachRow(pidmQuery, [gobeaccUserName]) { row ->
            bPIDM = row.GOBEACC_PIDM
        }
        bPIDM
    }
}



/*******************************************************************************
 Copyright 2009-2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

package net.hedtech.banner.security

import grails.util.Holders
import groovy.sql.Sql
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.mock.web.MockFilterChain
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder

class BannerPreAuthenticatedFilterIntegrationTests extends BaseIntegrationTestCase {

    def bannerPreAuthenticatedFilter
    def gobeaccUserName = 'GRAILS_USER'
    def conn
    def bannerPIDM
    def dataSource

    public static final String UDC_IDENTIFIER = 'INTEGRATION_TEST_SAML'

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        Holders.config.ssbEnabled = false
        Holders?.config.banner.sso.authenticationAssertionAttribute = "UDC_IDENTIFIER"
        Holders?.config.banner.sso.authenticationProvider = "external"
        conn = dataSource.getConnection()
        bannerPIDM = getBannerPIDM()
    }

    @After
    public void tearDown() {
        super.tearDown();
        conn?.close()
    }

    @Test
    void testAdminDoFilter() {
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
    }


    @Test
    void testAttributeNull() {
        Holders?.config.grails.plugin.springsecurity.interceptUrlMap.remove("/**")
        Holders?.config.grails.plugin.springsecurity.interceptUrlMap.put('/ssb/**', ['ROLE_SELFSERVICE-STUDENT_BAN_DEFAULT_M', 'ROLE_SELFSERVICE-GUEST_BAN_DEFAULT_M'])

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/ssb/foo");
        SecurityContextHolder.context?.authentication = null  //clear context
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        bannerPreAuthenticatedFilter.doFilter(request, response, chain);

        def msg = request.getSession()?.SPRING_SECURITY_LAST_EXCEPTION?.getMessage()
        assertEquals msg, "System is configured for external authentication and identity assertion UDC_IDENTIFIER is null"
        assertEquals response.getRedirectedUrl(), "/login/error"
    }


    @Test
    void testBannerUserNotFound() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        Holders?.config.grails.plugin.springsecurity.interceptUrlMap.remove("/**")
        Holders?.config.grails.plugin.springsecurity.interceptUrlMap.put('/ssb/**', ['ROLE_SELFSERVICE-STUDENT_BAN_DEFAULT_M', 'ROLE_SELFSERVICE-GUEST_BAN_DEFAULT_M'])

        def udc_identifier = "2"
        request.addHeader("UDC_IDENTIFIER", udc_identifier)
        SecurityContextHolder.context?.authentication = null  //clear context
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        bannerPreAuthenticatedFilter.doFilter(request, response, chain);

        assertEquals response.getRedirectedUrl(), "/login/error"

        def msg = request.getSession()?.SPRING_SECURITY_LAST_EXCEPTION?.getMessage()

        assertEquals msg, "System is configured for external authentication, identity assertion 2 does not map to a Banner user"
        assertNull(SecurityContextHolder.context.getAuthentication())

    }


    @Test
    void testFilterSkip() {
        Holders?.config.grails.plugin.springsecurity.interceptUrlMap.remove("/**")
        Holders?.config.grails.plugin.springsecurity.interceptUrlMap.put('/ssb/**', ['IS_AUTHENTICATED_ANONYMOUSLY'])

        MockHttpServletRequest request = new MockHttpServletRequest();

        request.setRequestURI("/ssb/foo");
        request.addHeader("UDC_IDENTIFIER", UDC_IDENTIFIER)

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        SecurityContextHolder.context?.authentication = null  //clear context

        bannerPreAuthenticatedFilter.doFilter(request, response, chain);

        assertNull(SecurityContextHolder.context.getAuthentication())
    }


    @Test
    void testFilterMultiAntUrlMatch() {
        Holders?.config.grails.plugin.springsecurity.interceptUrlMap.remove("/**")
        Holders?.config.grails.plugin.springsecurity.interceptUrlMap.put('/external/test/**', ['ROLE_SELFSERVICE-STUDENT_BAN_DEFAULT_M', 'ROLE_SELFSERVICE-GUEST_BAN_DEFAULT_M'])
        Holders?.config.grails.plugin.springsecurity.interceptUrlMap.put('/external/**', ['IS_AUTHENTICATED_ANONYMOUSLY'])
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/external/test/foo/super/somefile2.html");

        request.addHeader("UDC_IDENTIFIER", UDC_IDENTIFIER)

        SecurityContextHolder.context?.authentication = null  //clear context

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        bannerPreAuthenticatedFilter.doFilter(request, response, chain);

        assertNotNull(SecurityContextHolder.context.getAuthentication())

        assertEquals SecurityContextHolder.context.getAuthentication().user.pidm, Integer.valueOf(bannerPIDM.intValue())
    }

    //----------------------------- Helper Methods ------------------------------

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

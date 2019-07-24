/*******************************************************************************
 Copyright 2009-2019 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

package net.hedtech.banner.security

import grails.gorm.transactions.Rollback
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.testing.mixin.integration.Integration
import grails.util.Holders
import groovy.sql.Sql
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.springframework.mock.web.MockFilterChain
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.context.request.RequestContextHolder

@Integration
@Rollback
class BannerPreAuthenticatedFilterIntegrationTests extends BaseIntegrationTestCase {

    def bannerPreAuthenticatedFilter
    String gobeaccUserName = 'GRAILS_USER'
    def bannerPIDM

    public static final String UDC_IDENTIFIER = 'INTEGRATION_TEST_SAML'

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        Holders.config.ssbEnabled = true
        super.setUp()
        RequestContextHolder.currentRequestAttributes().request.session.servletContext.setAttribute('mepEnabled', false)
        Authentication auth = selfServiceBannerAuthenticationProvider.authenticate( new UsernamePasswordAuthenticationToken('INTGRN',111111))
        SecurityContextHolder.getContext().setAuthentication( auth )
        Holders.config.banner.sso.authenticationAssertionAttribute = "UDC_IDENTIFIER"
        Holders.config.banner.sso.authenticationProvider = "external"
        bannerPIDM = getBannerPIDM()
    }

    @After
    public void tearDown() {
        logout()
        super.tearDown()
    }

    @Test
    void testAdminDoFilter() {
        HashMap wildCardMap = new HashMap()
        wildCardMap.put('pattern','/**')
        wildCardMap.put('access',['IS_AUTHENTICATED_ANONYMOUSLY'])
        Holders.config.grails.plugin.springsecurity.interceptUrlMap.remove(wildCardMap)
        Map roleMap = new LinkedHashMap()
        roleMap.put('pattern','/ssb/**')
        roleMap.put('access',['ROLE_SELFSERVICE-STUDENT_BAN_DEFAULT_M', 'ROLE_SELFSERVICE-GUEST_BAN_DEFAULT_M'])
        Holders.config.grails.plugin.springsecurity.interceptUrlMap.add(roleMap)

        MockHttpServletRequest request = new MockHttpServletRequest()
        request.setRequestURI("/ssb/foo")
        request.addHeader("UDC_IDENTIFIER", UDC_IDENTIFIER)
        MockHttpServletResponse response = new MockHttpServletResponse()
        MockFilterChain chain = new MockFilterChain()

        bannerPreAuthenticatedFilter.doFilter(request, response, chain)

        println "User is " + SecurityContextHolder.context.getAuthentication().user
        assertEquals SecurityContextHolder.context.getAuthentication().user.pidm, Integer.valueOf(bannerPIDM.intValue())

        assertNotNull(SecurityContextHolder.context.getAuthentication())
        HashMap wildCardMap1 = new HashMap()
        wildCardMap1.put('pattern','/ssb/**')
        wildCardMap1.put('access',['ROLE_SELFSERVICE-STUDENT_BAN_DEFAULT_M', 'ROLE_SELFSERVICE-GUEST_BAN_DEFAULT_M'])
        Holders.config.grails.plugin.springsecurity.interceptUrlMap.remove(wildCardMap1)
    }


    @Test
    void testAttributeNull() {
        HashMap wildCardMap = new HashMap()
        wildCardMap.put('pattern','/**')
        wildCardMap.put('access',['IS_AUTHENTICATED_ANONYMOUSLY'])
        Holders.config.grails.plugin.springsecurity.interceptUrlMap.remove(wildCardMap)
        Map roleMap = new LinkedHashMap()
        roleMap.put('pattern','/ssb/**')
        roleMap.put('access',['ROLE_SELFSERVICE-STUDENT_BAN_DEFAULT_M', 'ROLE_SELFSERVICE-GUEST_BAN_DEFAULT_M'])
        Holders.config.grails.plugin.springsecurity.interceptUrlMap.add(roleMap)

        //Holders.config.grails.plugin.springsecurity.interceptUrlMap.put('/ssb/**', ['ROLE_SELFSERVICE-STUDENT_BAN_DEFAULT_M', 'ROLE_SELFSERVICE-GUEST_BAN_DEFAULT_M'])

        MockHttpServletRequest request = new MockHttpServletRequest()
        request.setRequestURI("/ssb/foo")
        SecurityContextHolder.context?.authentication = null  //clear context
        MockHttpServletResponse response = new MockHttpServletResponse()
        MockFilterChain chain = new MockFilterChain()

        bannerPreAuthenticatedFilter.doFilter(request, response, chain)

        def msg = request.getSession()?.SPRING_SECURITY_LAST_EXCEPTION?.getMessage()
        assertEquals msg, "System is configured for external authentication and identity assertion UDC_IDENTIFIER is null"
        assertEquals response.getRedirectedUrl(), "/login/error"

        HashMap wildCardMap1 = new HashMap()
        wildCardMap1.put('pattern','/ssb/**')
        wildCardMap1.put('access',['ROLE_SELFSERVICE-STUDENT_BAN_DEFAULT_M', 'ROLE_SELFSERVICE-GUEST_BAN_DEFAULT_M'])
        Holders.config.grails.plugin.springsecurity.interceptUrlMap.remove(wildCardMap1)

    }


    @Test
    void testBannerUserNotFound() {
        HashMap wildCardMap = new HashMap()
        wildCardMap.put('pattern','/**')
        wildCardMap.put('access',['IS_AUTHENTICATED_ANONYMOUSLY'])
        Holders.config.grails.plugin.springsecurity.interceptUrlMap.remove(wildCardMap)
        MockHttpServletRequest request = new MockHttpServletRequest()
        Map roleMap = new LinkedHashMap()
        roleMap.put('pattern','/ssb/**')
        roleMap.put('access',['ROLE_SELFSERVICE-STUDENT_BAN_DEFAULT_M', 'ROLE_SELFSERVICE-GUEST_BAN_DEFAULT_M'])
        Holders.config.grails.plugin.springsecurity.interceptUrlMap.add(roleMap)

        //Holders.config.grails.plugin.springsecurity.interceptUrlMap.put('/ssb/**', ['ROLE_SELFSERVICE-STUDENT_BAN_DEFAULT_M', 'ROLE_SELFSERVICE-GUEST_BAN_DEFAULT_M'])

        def udc_identifier = "2"
        request.addHeader("UDC_IDENTIFIER", udc_identifier)
        SecurityContextHolder.context?.authentication = null  //clear context
        MockHttpServletResponse response = new MockHttpServletResponse()
        MockFilterChain chain = new MockFilterChain();

        bannerPreAuthenticatedFilter.doFilter(request, response, chain);

        assertEquals response.getRedirectedUrl(), "/login/error"

        def msg = request.getSession()?.SPRING_SECURITY_LAST_EXCEPTION?.getMessage()

        assertEquals msg, "System is configured for external authentication, identity assertion 2 does not map to a Banner user"
        assertNull(SecurityContextHolder.context.getAuthentication())

        HashMap wildCardMap1 = new HashMap()
        wildCardMap1.put('pattern','/ssb/**')
        wildCardMap1.put('access',['ROLE_SELFSERVICE-STUDENT_BAN_DEFAULT_M', 'ROLE_SELFSERVICE-GUEST_BAN_DEFAULT_M'])
        Holders.config.grails.plugin.springsecurity.interceptUrlMap.remove(wildCardMap1)

    }


    @Test
    void testFilterSkip() {
        HashMap wildCardMap = new HashMap()
        wildCardMap.put('pattern','/**')
        wildCardMap.put('access',['IS_AUTHENTICATED_ANONYMOUSLY'])
        Holders.config.grails.plugin.springsecurity.interceptUrlMap.remove(wildCardMap)
        Map roleMap = new LinkedHashMap()
        roleMap.put('pattern','/ssb/**')
        roleMap.put('access',['IS_AUTHENTICATED_ANONYMOUSLY'])
        Holders.config.grails.plugin.springsecurity.interceptUrlMap.add(roleMap)

        MockHttpServletRequest request = new MockHttpServletRequest()

        request.setRequestURI("/ssb/foo");
        request.addHeader("UDC_IDENTIFIER", UDC_IDENTIFIER)

        MockHttpServletResponse response = new MockHttpServletResponse()
        MockFilterChain chain = new MockFilterChain()
        SecurityContextHolder.context.authentication = null  //clear context

        bannerPreAuthenticatedFilter.doFilter(request, response, chain)

        assertNull(SecurityContextHolder.context.getAuthentication())
        HashMap wildCardMap1 = new HashMap()
        wildCardMap1.put('pattern','/ssb/**')
        wildCardMap1.put('access',['IS_AUTHENTICATED_ANONYMOUSLY'])
        Holders.config.grails.plugin.springsecurity.interceptUrlMap.remove(wildCardMap1)

    }


    @Test
    void testFilterMultiAntUrlMatch() {
        HashMap wildCardMap = new HashMap()
        wildCardMap.put('pattern','/**')
        wildCardMap.put('access',['IS_AUTHENTICATED_ANONYMOUSLY'])
        Holders.config.grails.plugin.springsecurity.interceptUrlMap.remove(wildCardMap)
        Map roleMap = new LinkedHashMap()
        roleMap.put('pattern','/external/test/**')
        roleMap.put('access',['ROLE_SELFSERVICE-STUDENT_BAN_DEFAULT_M', 'ROLE_SELFSERVICE-GUEST_BAN_DEFAULT_M'])
        Holders.config.grails.plugin.springsecurity.interceptUrlMap.add(roleMap)

        Map roleMap1 = new LinkedHashMap()
        roleMap1.put('pattern','/external/**')
        roleMap1.put('access',['IS_AUTHENTICATED_ANONYMOUSLY'])
        Holders.config.grails.plugin.springsecurity.interceptUrlMap.add(roleMap1)

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/external/test/foo/super/somefile2.html");

        request.addHeader("UDC_IDENTIFIER", UDC_IDENTIFIER)

        SecurityContextHolder.context?.authentication = null  //clear context

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        bannerPreAuthenticatedFilter.doFilter(request, response, chain);

        assertNotNull(SecurityContextHolder.context.getAuthentication())

        assertEquals SecurityContextHolder.context.getAuthentication().user.pidm, Integer.valueOf(bannerPIDM.intValue())
        HashMap wildCardMap1 = new HashMap()
        wildCardMap1.put('pattern','/external/**')
        wildCardMap1.put('access',['IS_AUTHENTICATED_ANONYMOUSLY'])
        Holders.config.grails.plugin.springsecurity.interceptUrlMap.remove(wildCardMap1)

        HashMap wildCardMap2 = new HashMap()
        wildCardMap2.put('pattern','/external/test/**')
        wildCardMap2.put('access',['ROLE_SELFSERVICE-STUDENT_BAN_DEFAULT_M', 'ROLE_SELFSERVICE-GUEST_BAN_DEFAULT_M'])
        Holders.config.grails.plugin.springsecurity.interceptUrlMap.remove(wildCardMap2)
    }

    //----------------------------- Helper Methods ------------------------------

    private def getBannerPIDM() {
        Sql sqlObj
        def bPIDM = 0
        sqlObj = new Sql(sessionFactory.getCurrentSession().connection())
        String pidmQuery = """SELECT GOBEACC_PIDM FROM GOBEACC WHERE GOBEACC_USERNAME = ?"""
        sqlObj.eachRow(pidmQuery, [gobeaccUserName]) { row ->
            bPIDM = row.GOBEACC_PIDM
        }
        bPIDM
    }
}

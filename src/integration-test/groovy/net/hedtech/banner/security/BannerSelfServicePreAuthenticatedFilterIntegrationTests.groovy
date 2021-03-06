/*******************************************************************************
 Copyright 2009-2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.security

import grails.gorm.transactions.Rollback
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

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Integration
@Rollback
class BannerSelfServicePreAuthenticatedFilterIntegrationTests extends BaseIntegrationTestCase {

    def bannerPreAuthenticatedFilter
    def selfServiceBannerAuthenticationProvider
    String gobeaccUserName = 'GRAILS_USER'
    def bannerPIDM

    public static final String UDC_IDENTIFIER = 'INTEGRATION_TEST_SAML'

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        Holders.config.ssbEnabled = true
        super.setUp()
        bannerPIDM = getBannerPIDM()
        Authentication auth = selfServiceBannerAuthenticationProvider.authenticate( new UsernamePasswordAuthenticationToken('INTGRN',111111))
        SecurityContextHolder.getContext().setAuthentication( auth )
    }

    @After
    public void tearDown() {
        logout()
        super.tearDown()
    }


    @Test
    void testSelfServiceDoFilter() {
        Holders.config.ssbEnabled = true
        Holders.config.ssbOracleUsersProxied = true
        Map roleMap = new LinkedHashMap()
        roleMap.put('pattern','/ssb/**')
        roleMap.put('access',['ROLE_SELFSERVICE-STUDENT_BAN_DEFAULT_M', 'ROLE_SELFSERVICE-GUEST_BAN_DEFAULT_M'])
        Holders.config.grails.plugin.springsecurity.interceptUrlMap.add(roleMap)

        HttpServletRequest request = new MockHttpServletRequest()
        request.setRequestURI("/ssb/foo")

        request.addHeader("UDC_IDENTIFIER", UDC_IDENTIFIER)
        HttpServletResponse response = new MockHttpServletResponse()
        MockFilterChain chain = new MockFilterChain()

        bannerPreAuthenticatedFilter.doFilter(request, response, chain)

        assertEquals SecurityContextHolder.context.getAuthentication().user.pidm, Integer.valueOf(bannerPIDM.intValue())

        assertNotNull(SecurityContextHolder.context.getAuthentication())
        Holders.config.ssbEnabled = false
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



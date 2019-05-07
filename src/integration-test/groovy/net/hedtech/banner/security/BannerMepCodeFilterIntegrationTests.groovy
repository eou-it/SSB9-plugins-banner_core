/* ****************************************************************************
Copyright 2016-2019 Ellucian Company L.P. and its affiliates.
*******************************************************************************/
package net.hedtech.banner.security

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.mock.web.MockFilterChain
import org.springframework.web.context.request.RequestContextHolder

import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse

@Integration
@Rollback
class BannerMepCodeFilterIntegrationTests extends BaseIntegrationTestCase {

    def bannerMepCodeFilter
    MockFilterChain chain
    def servletRequest
    def servletResponse

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
    }

    @After
    public void tearDown() {
        super.tearDown()
    }

    @Test
    void testDoFilterWithoutMep() {
        servletRequest = RequestContextHolder.currentRequestAttributes().request
        servletResponse = RequestContextHolder.currentRequestAttributes().response
        chain = new MockFilterChain()
        servletRequest.removeParameter("mepCode")
        try {
            bannerMepCodeFilter.doFilter(servletRequest, servletResponse, chain)
            assertNull servletRequest.getSession().getAttribute('mep')
        } catch (ServletException se) {
            assertFalse(true)
        }
    }

    @Test
    void testLoggedInUserMepUnChanged(){
        servletRequest = RequestContextHolder.currentRequestAttributes().request
        servletResponse = RequestContextHolder.currentRequestAttributes().response
        logout()
        loginSSB("HOSH00001","111111")
        chain = new MockFilterChain()
        def mepCode1 = "BANNER"
        servletRequest.setParameter("mepCode", mepCode1)
        try {
            bannerMepCodeFilter.doFilter(servletRequest, servletResponse, chain)
            assertNotNull servletRequest.getSession().getAttribute('mep')
            ServletRequest servletRequest1 = RequestContextHolder.currentRequestAttributes().request
            ServletResponse servletResponse2 = RequestContextHolder.currentRequestAttributes().response
            MockFilterChain chain1 = new MockFilterChain()
            def mepCode2 = "MAIN"
            servletRequest1.setParameter("mepCode", mepCode2)
            bannerMepCodeFilter.doFilter(servletRequest1, servletResponse2, chain1)
            assertNotNull servletRequest1.getSession().getAttribute('mep')
            assertNotEquals(mepCode2,servletRequest1.getSession().getAttribute('mep'))
            assertEquals(mepCode1,servletRequest1.getSession().getAttribute('mep'))
        } catch (ServletException se) {
            assertFalse(true)
        }
    }

    @Test
    void testNotLoggedInMepCodeChanged(){
        servletRequest = RequestContextHolder.currentRequestAttributes().request
        servletResponse = RequestContextHolder.currentRequestAttributes().response
        logout()
        chain = new MockFilterChain()
        def mepCode1 = "BANNER"
        servletRequest.setParameter("mepCode", mepCode1)
        try {
            bannerMepCodeFilter.doFilter(servletRequest, servletResponse, chain)
            assertEquals("BANNER", servletRequest.getSession().getAttribute('mep'))
        } catch (ServletException se) {
            assertFalse(true)
        }
    }
}

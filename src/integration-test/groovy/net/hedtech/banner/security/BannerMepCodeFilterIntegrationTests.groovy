/* ****************************************************************************
Copyright 2016 Ellucian Company L.P. and its affiliates.
*******************************************************************************/

package net.hedtech.banner.security

import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.mock.web.MockFilterChain
import org.springframework.web.context.request.RequestContextHolder

import javax.servlet.ServletException

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
    void testDoFilter() {
        servletRequest = RequestContextHolder.currentRequestAttributes().request
        servletResponse = RequestContextHolder.currentRequestAttributes().response
        chain = new MockFilterChain()
        servletRequest.setParameter("mepCode", "BANNER")
        try {
            bannerMepCodeFilter.doFilter(servletRequest, servletResponse, chain)
            assertTrue(true)
        } catch (ServletException se) {
            assertFalse(true)
        }

    }

    @Test
    void testDoFilterWithoutMep() {
        servletRequest = RequestContextHolder.currentRequestAttributes().request
        servletResponse = RequestContextHolder.currentRequestAttributes().response
        chain = new MockFilterChain()
        servletRequest.removeParameter("mepCode")
        try {
            bannerMepCodeFilter.doFilter(servletRequest, servletResponse, chain)
            assertTrue(true)
        } catch (ServletException se) {
            assertFalse(true)
        }
    }
}

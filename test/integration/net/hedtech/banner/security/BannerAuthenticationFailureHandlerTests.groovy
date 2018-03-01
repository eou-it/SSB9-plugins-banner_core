/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

package net.hedtech.banner.security

import grails.util.Holders
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.web.context.request.RequestContextHolder

/**
 * Intergration test cases for Banner Authentication Failure Handler
 */

class BannerAuthenticationFailureHandlerTests extends BaseIntegrationTestCase {

    private BannerAuthenticationFailureHandler failureHandler

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        failureHandler = Holders.applicationContext.getBean("bannerAuthenticationFailureHandler")
        RequestContextHolder.currentRequestAttributes().request.session.servletContext.setAttribute('mepEnabled', false)
        }

    @After
    public void tearDown() {
        super.tearDown()
    }


    @Test
    public void testBannerAuthentiationWithSpecificUsage() {
        MockHttpServletRequest request = new MockHttpServletRequest()
        MockHttpServletResponse response = new MockHttpServletResponse()
        failureHandler.onAuthenticationFailure(request,response,new UsernameNotFoundException("User Not Found"));
    }

}

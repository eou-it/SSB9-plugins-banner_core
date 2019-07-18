/*******************************************************************************
Copyright 2015-2016 Ellucian Company L.P. and its affiliates.
*******************************************************************************/
package net.hedtech.banner.apisupport

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import grails.util.Holders
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletWebRequest

@Integration
@Rollback
class ApiUtilsIntegrationTests extends BaseIntegrationTestCase {

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
    public void testShouldCacheConnection () {
        assertTrue(ApiUtils.shouldCacheConnection())

        // Fail case
        def reqAtt = RequestContextHolder.getRequestAttributes()
        RequestContextHolder.setRequestAttributes(null);
        assertFalse(ApiUtils.shouldCacheConnection())

        RequestContextHolder.setRequestAttributes(reqAtt);
        RequestAttributes mockRequest = new ServletWebRequest(new MockHttpServletRequest("GET", null))
        RequestContextHolder.setRequestAttributes(mockRequest)
        assertTrue(ApiUtils.shouldCacheConnection())

        Holders.config.avoidSessionsFor = ""
        assertTrue(ApiUtils.shouldCacheConnection())

        RequestAttributes mRequest = new ServletWebRequest(new MockHttpServletRequest("GET", ""))
        RequestContextHolder.setRequestAttributes(mRequest)
        Holders.config.avoidSessionsFor = ["test"]
        assertTrue(ApiUtils.shouldCacheConnection())
    }

    @Test
    public void testShouldCacheConnectionAvoidCachingCase () {
        RequestAttributes mockRequest = new ServletWebRequest(new MockHttpServletRequest("GET", "/test/"))
        RequestContextHolder.setRequestAttributes(mockRequest)
        Holders.config.avoidSessionsFor = ["test"]
        assertFalse(ApiUtils.shouldCacheConnection())
    }

    @Test
    public void testShouldCacheConnectionAvoidCachingCase1 () {
        RequestAttributes mockRequest = new ServletWebRequest(new MockHttpServletRequest("GET", "test"))
        RequestContextHolder.setRequestAttributes(mockRequest)
        Holders.config.avoidSessionsFor = ['test']
        assertTrue(ApiUtils.shouldCacheConnection())
    }

    @Test
    public void testIsApiRequest () {
        assertFalse(ApiUtils.isApiRequest())
    }
}

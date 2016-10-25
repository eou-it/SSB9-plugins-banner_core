/*******************************************************************************
Copyright 2015-2016 Ellucian Company L.P. and its affiliates.
*******************************************************************************/
package net.hedtech.banner.apisupport

import grails.util.Holders
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.web.context.request.RequestContextHolder

/**
 * Tests that the ApiUtils is working as expected.
 *
 * Created by arunu on 10/21/2016.
 */
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
        RequestContextHolder.getRequestAttributes().getRequest().forwardURI = null
        assertTrue(ApiUtils.shouldCacheConnection())

        RequestContextHolder.getRequestAttributes().getRequest().forwardURI = null
        Holders.config.avoidSessionsFor = ""
        assertTrue(ApiUtils.shouldCacheConnection())

        RequestContextHolder.getRequestAttributes().getRequest().forwardURI = ""
        Holders.config.avoidSessionsFor = ["test"]
        assertTrue(ApiUtils.shouldCacheConnection())
    }

    @Test
    public void testShouldCacheConnectionAvoidCachingCase () {
        RequestContextHolder.getRequestAttributes().getRequest().forwardURI = '/test/'
        Holders.config.avoidSessionsFor = ["test"]
        assertFalse(ApiUtils.shouldCacheConnection())
    }

    @Test
    public void testShouldCacheConnectionAvoidCachingCase1 () {
        RequestContextHolder.getRequestAttributes().getRequest().forwardURI = 'test'
        Holders.config.avoidSessionsFor = ['test']
        assertTrue(ApiUtils.shouldCacheConnection())
    }

    @Test
    public void testIsApiRequest () {
        assertFalse(ApiUtils.isApiRequest())
    }
}

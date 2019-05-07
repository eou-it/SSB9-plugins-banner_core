/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

package net.hedtech.banner.db

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import grails.util.Holders
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.mock.web.MockHttpSession
import org.springframework.mock.web.MockServletContext

import javax.servlet.http.HttpSessionEvent

/**
 * Test cases for DbConnectionCacheSessionListener
 */
@Integration
@Rollback
class DbConnectionCacheSessionListenerIntegrationTests extends BaseIntegrationTestCase {


    @Before
    public void setUp(){
        formContext = ['GUAGMNU']
        super.setUp()
    }

    @After
    public void tearDown(){
        super.tearDown();
    }

    @Test
    void sessionCreated() {
        DbConnectionCacheSessionListener dbConnectionCacheSession = new DbConnectionCacheSessionListener();
        MockServletContext servletContext = new MockServletContext();
        MockHttpSession session = new MockHttpSession(servletContext);
        HttpSessionEvent event = new HttpSessionEvent(session);
        dbConnectionCacheSession.sessionCreated(event);
        assertTrue(Holders.grailsApplication.getMainContext().getBean('httpSessionService') != null);
    }

    @Test
    void sessionDestroyed() {
        DbConnectionCacheSessionListener dbConnectionCacheSession = new DbConnectionCacheSessionListener();
        MockServletContext servletContext = new MockServletContext();
        MockHttpSession session = new MockHttpSession(servletContext);
        HttpSessionEvent event = new HttpSessionEvent(session);
        dbConnectionCacheSession.sessionCreated(event);
        dbConnectionCacheSession.sessionDestroyed(event);
        assertTrue(Holders.grailsApplication.getMainContext().getBean('httpSessionService') != null);
    }
}

/*******************************************************************************
Copyright 2017 Ellucian Company L.P. and its affiliates.
*******************************************************************************/ 
package net.hedtech.banner.service

import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.codehaus.groovy.grails.plugins.testing.GrailsMockHttpServletRequest
import org.junit.After
import org.junit.Before
import org.junit.Test

import javax.servlet.http.HttpSession

class HttpSessionServiceIntegrationTests extends BaseIntegrationTestCase {

    def HttpSessionService

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
    void testSessionCreated() {
        GrailsMockHttpServletRequest request = new GrailsMockHttpServletRequest()
        HttpSession session = request.getSession()
        HttpSessionService.sessionCreated(session)
        assertNotNull session.id
    }

    @Test
    void testSessionDestroyed() {
        GrailsMockHttpServletRequest request = new GrailsMockHttpServletRequest()
        HttpSession session = request.getSession()
        HttpSessionService.sessionCreated(session)
        assertNotNull session.id
        HttpSessionService.sessionDestroyed(session)
    }


    @Test
    void testSessionCachedConnectionTrue() {
        GrailsMockHttpServletRequest request = new GrailsMockHttpServletRequest()
        def oldCachedConnection = request.getSession().getAttribute("cachedConnection")
        HttpSession session = request.getSession()
        session.setAttribute("cachedConnection", "true")
        HttpSessionService.sessionCreated(session)
        assertNotNull session.id
        HttpSessionService.sessionDestroyed(session)
        request?.getSession()?.setAttribute("cachedConnection", oldCachedConnection)
    }


}

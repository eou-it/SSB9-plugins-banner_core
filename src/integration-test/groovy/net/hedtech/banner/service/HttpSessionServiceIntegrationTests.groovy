/*******************************************************************************
 Copyright 2017-2019 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.service

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import net.hedtech.banner.testing.BaseIntegrationTestCase
//import org.codehaus.groovy.grails.plugins.testing.GrailsMockHttpServletRequest
import org.grails.plugins.testing.GrailsMockHttpServletRequest
import org.junit.After
import org.junit.Before
import org.junit.Test

import javax.servlet.http.HttpSession

@Integration
@Rollback
class HttpSessionServiceIntegrationTests extends BaseIntegrationTestCase {

    def HttpSessionService
    def dataSource

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
        String userName = session.getAttribute("SPRING_SECURITY_CONTEXT" )?.authentication?.user?.username
        if(userName!=null && HttpSessionService.cachedConnectionMap.containsKey(session.id))
            getHttpSessionService().sessionDestroyed(session.id)
    }


    @Test
    void testSessionDestroyed() {
        GrailsMockHttpServletRequest request = new GrailsMockHttpServletRequest()
        HttpSession session = request.getSession()
        HttpSessionService.sessionCreated(session)
        assertNotNull session.id
        String userName = session.getAttribute("SPRING_SECURITY_CONTEXT" )?.authentication?.user?.username
        if(userName!=null && HttpSessionService.cachedConnectionMap.containsKey(session.id))
            getHttpSessionService().sessionDestroyed(session.id)
    }


    @Test
    void testSessionCachedConnectionTrue() {
        GrailsMockHttpServletRequest request = new GrailsMockHttpServletRequest()
        HttpSession session = request.getSession()
        String userName = "Test"
        def conn = dataSource.getSsbConnection()
        HttpSessionService.cachedConnectionMap.put(userName,conn)
        HttpSessionService.sessionCreated(session)
        assertNotNull session.id
        if(userName!=null && HttpSessionService.cachedConnectionMap.containsKey(session.id))
                    getHttpSessionService().sessionDestroyed(session.id)
    }

}

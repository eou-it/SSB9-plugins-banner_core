/* ****************************************************************************
Copyright 2017-2020 Ellucian Company L.P. and its affiliates.
******************************************************************************/

package net.hedtech.banner.db

import grails.util.Environment
import grails.util.Holders
import net.hedtech.banner.service.HttpSessionService

import javax.servlet.http.HttpSession
import javax.servlet.http.HttpSessionEvent
import javax.servlet.http.HttpSessionListener

class DbConnectionCacheSessionListener implements HttpSessionListener {

    def httpSessionService  // won't be injected but we'll set lazily

    @Override
    void sessionCreated(HttpSessionEvent event) {
        HttpSession session = event.getSession()
        getHttpSessionService().sessionCreated(session)
    }


    @Override
    void sessionDestroyed(HttpSessionEvent event) {
        HttpSession session = event.getSession()
        // Ensure that the service is called only when the session contains a connection
        // Login causes the earlier established session to be destroyed and then no request
        // is avaiable and HTTPSerice sessionDestroyed throws an Exception
        String sessionId = session.id
        if(HttpSessionService.cachedConnectionMap.containsKey(sessionId) && (Environment.current != Environment.TEST))
            getHttpSessionService().sessionDestroyed(sessionId)
    }


    private synchronized HttpSessionService getHttpSessionService() {
        if (httpSessionService == null) {
            httpSessionService = (HttpSessionService) Holders.grailsApplication.getMainContext().getBean('httpSessionService')
        }
        httpSessionService
    }
}

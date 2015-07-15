/* ****************************************************************************
Copyright 2013 Ellucian Company L.P. and its affiliates.
******************************************************************************/

package net.hedtech.banner.db

import net.hedtech.banner.service.HttpSessionService

import grails.util.Holders

import javax.servlet.http.HttpSession
import javax.servlet.http.HttpSessionEvent
import javax.servlet.http.HttpSessionListener
import grails.util.Environment

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
        if(session.getAttribute("cachedConnection") && (Environment.current != Environment.TEST))
            getHttpSessionService().sessionDestroyed(session)
    }


    private synchronized HttpSessionService getHttpSessionService() {
        if (httpSessionService == null) {
            httpSessionService = (HttpSessionService) ApplicationHolder.getApplication().getMainContext().getBean('httpSessionService')
        }
        httpSessionService
    }
}
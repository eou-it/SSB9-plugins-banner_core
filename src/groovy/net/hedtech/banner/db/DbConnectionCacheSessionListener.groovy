/* ****************************************************************************
Copyright 2013 Ellucian Company L.P. and its affiliates.
******************************************************************************/

package net.hedtech.banner.db

import net.hedtech.banner.service.HttpSessionService

import org.codehaus.groovy.grails.commons.ApplicationHolder

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
        getHttpSessionService().sessionDestroyed(session)
    }


    private synchronized HttpSessionService getHttpSessionService() {
        if (httpSessionService == null) {
            httpSessionService = (HttpSessionService) ApplicationHolder.getApplication().getMainContext().getBean('httpSessionService')
        }
        httpSessionService
    }
}
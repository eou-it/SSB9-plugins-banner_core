/* ****************************************************************************
Copyright 2013 Ellucian Company L.P. and its affiliates.
******************************************************************************/

package net.hedtech.banner.service

import org.springframework.web.context.request.RequestContextHolder
import org.apache.log4j.Logger
import javax.servlet.http.HttpSession
import java.sql.Connection

class HttpSessionService {
    def dataSource     // injected by Spring
    private final Logger log = Logger.getLogger( getClass() )

    def sessionCreated(HttpSession session) {
        log.info("Session created: " + session.id)
    }

    def sessionDestroyed(HttpSession session) {
        log.info("Session destroyed: " + session.id)
        // SessionDestroyed event is fired for every session that is invalidated, which include explicit invalidation
        // during logout. We close connections during logout and need to only handle cases where session timeout
        def currentTime = System.currentTimeMillis()
        def timeElapsedSinceLastAccess = currentTime - session.lastAccessedTime
        if(timeElapsedSinceLastAccess > session.maxInactiveInterval * 1000)  {
            closeDBConnection()
        }
        RequestContextHolder.currentRequestAttributes().request.session.setAttribute("cachedConnection", null)
    }

    def closeDBConnection() {
        dataSource.removeConnection()
        try {
            Connection conn  = RequestContextHolder.currentRequestAttributes().request.session.getAttribute("cachedConnection")
            if (!conn)
                conn.close()
        }
        catch (e) {
            log.error("HttpSessionService.closeDBconnect connection close error $e")
        }
    }
}
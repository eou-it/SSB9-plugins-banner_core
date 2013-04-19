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
        closeDBConnection()
        RequestContextHolder.currentRequestAttributes().request.session.setAttribute("cachedConnection", null)
    }

    def closeDBConnection() {
        try {
            Connection conn  = RequestContextHolder.currentRequestAttributes().request.session.getAttribute("cachedConnection")
            if (conn)
                dataSource.removeConnection(conn)
        }
        catch (e) {
            log.error("HttpSessionService.closeDBconnect connection close error $e")
        }
    }
}
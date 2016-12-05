/* ****************************************************************************
Copyright 2013 Ellucian Company L.P. and its affiliates.
******************************************************************************/

package net.hedtech.banner.service

import org.apache.log4j.Logger
import javax.servlet.http.HttpSession
import java.sql.Connection

class HttpSessionService {
    def dataSource     // injected by Spring
    private static final Logger log = Logger.getLogger( getClass() )

    def sessionCreated(HttpSession session) {
        log.trace("Session created: " + session.id)
    }

    def sessionDestroyed(HttpSession session) {
        log.trace("Session destroyed: " + session.id)
        closeDBConnection(session)
    }

    def closeDBConnection(HttpSession session) {
        log.trace("HttpSessionService.closeDBConnection invoked")
        try {
            Connection conn  = session?.getAttribute("cachedConnection")
            log.trace("HttpSessionService.closeDBConnection invoked $conn cleaned up")
            if (conn)
                dataSource.removeConnection(conn)
        }
        catch (e) {
            log.trace(e)
        }
    }
}
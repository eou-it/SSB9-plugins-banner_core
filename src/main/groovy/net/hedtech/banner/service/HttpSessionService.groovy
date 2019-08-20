/*******************************************************************************
 Copyright 2017-2019 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

package net.hedtech.banner.service

import groovy.util.logging.Slf4j
import net.hedtech.banner.db.BannerConnection
import java.util.concurrent.ConcurrentHashMap
import javax.servlet.http.HttpSession
import java.sql.Connection

@Slf4j
class HttpSessionService {
    def dataSource     // injected by Spring
    public static ConcurrentHashMap<String,BannerConnection> cachedConnectionMap = new ConcurrentHashMap<String,BannerConnection>()

    def sessionCreated(HttpSession session) {
        log.trace("Session created: " + session.id)
    }

    def sessionDestroyed(String sessionId) {
        closeDBConnection(sessionId)
    }

    def closeDBConnection(String sessionId) {
        log.trace("HttpSessionService.closeDBConnection invoked")
        try {
            Connection conn = cachedConnectionMap.get(sessionId)
            log.debug("HttpSessionService.closeDBConnection invoked for $sessionId cleaned up")
            if (conn){
                dataSource.removeConnection(conn)
                cachedConnectionMap.remove(sessionId)
            }
        }
        catch (e) {
			log.trace(e.toString())
            log.error("Exception occured while closeDBConnection for $sessionId.")
        }
    }
}

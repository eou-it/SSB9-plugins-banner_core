/* ******************************************************************************
 Copyright 2020 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.db

import groovy.util.logging.Slf4j
import javax.servlet.http.HttpSessionEvent
import javax.servlet.http.HttpSessionListener

@Slf4j
public class SessionCounterListener implements HttpSessionListener {

    private static int totalActiveSessions

    public static int getTotalActiveSession(){
        return totalActiveSessions
    }

    @Override
    public void sessionCreated(HttpSessionEvent sessionEvent) {
        totalActiveSessions++
        log.debug("Total active session : {}",totalActiveSessions)
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent sessionEvent) {
        (totalActiveSessions > 0) ? totalActiveSessions-- : 0
        log.debug("Total active session : {}",totalActiveSessions)
    }
}

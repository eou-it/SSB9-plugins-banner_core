/*****************************************************************************
Copyright 2020 Ellucian Company L.P. and its affiliates.
*******************************************************************************/
package net.hedtech.banner.logback


import grails.util.Holders
import groovy.util.logging.Slf4j
import net.hedtech.banner.security.BannerUser
import org.slf4j.MDC

@Slf4j
class LogbackMDCInterceptor {

    //TODO check the precedence using constants
    int order = HIGHEST_PRECEDENCE + 60

    private static final String CORRELATION_ID = "correlationId"
    private static final String PRINCIPAL_ID = "principalId"
    private static final String CLIENT_IP = "clientIp"

    def springSecurityService

    LogbackMDCInterceptor() {
        match controller: '*', action: '*'
    }

    boolean before() {
        final String correlationId = UUID.randomUUID().toString()
        MDC.put( CORRELATION_ID, correlationId )
        if ( Holders.config.banner.logback.log.debug ) {
            if ( springSecurityService.isLoggedIn() ) {
                BannerUser principal = springSecurityService.principal
                MDC.put( PRINCIPAL_ID, principal.username )
            } else {
                MDC.put( CLIENT_IP, request.getRemoteAddr() )
            }
        }
        true
    }

    boolean after() {
        MDC.remove( CORRELATION_ID )
        if ( Holders.config.banner.logback.log.debug ) {
            if ( springSecurityService.isLoggedIn() ) {
                MDC.remove( PRINCIPAL_ID )
            } else {
                MDC.remove( CLIENT_IP )
            }
        }
        true
    }

    void afterView() {
        // no-op
    }

}

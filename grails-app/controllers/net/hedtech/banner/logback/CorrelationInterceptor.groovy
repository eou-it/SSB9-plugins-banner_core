/*****************************************************************************
Copyright 2020 Ellucian Company L.P. and its affiliates.
*******************************************************************************/
package net.hedtech.banner.logback


import groovy.util.logging.Slf4j
import org.slf4j.MDC

@Slf4j
class CorrelationInterceptor {

    //TODO check the precedence using constants
    int order = HIGHEST_PRECEDENCE + 60

    private static final String CORRELATION_ID = "correlationId";

    CorrelationInterceptor () {
        match controller: '*', action: '*'
    }

    boolean before() {
        final String correlationId = UUID.randomUUID().toString()
        MDC.put( CORRELATION_ID, correlationId )
        true
    }

    boolean after() {
        MDC.remove( CORRELATION_ID )
        true
    }

    void afterView() {
        // no-op
    }

}

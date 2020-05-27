/*****************************************************************************
Copyright 2020 Ellucian Company L.P. and its affiliates.
*******************************************************************************/
package net.hedtech.banner.logback


import groovy.json.JsonOutput
import groovy.util.logging.Slf4j
import org.slf4j.MDC

import javax.servlet.http.HttpServletRequest

@Slf4j
class CorrelationInterceptor {

    //TODO check the precedence using constants
    int order = HIGHEST_PRECEDENCE + 60

    // The X-Correlation-Id is used while sending the correlation id from the ui.
    // TODO Need to confirm whether we need to send the correlation id from ui.
    private static final String CORRELATION_ID_HEADER_NAME = "X-Correlation-Id";
    private static final String CORRELATION_DATA_LOG_VAR_NAME = "elcn_logging_correlationData";

    CorrelationInterceptor () {
        match controller: '*', action: '*'
    }

    boolean before() {
        final String correlationId = getCorrelationIdFromHeader(request)
        final Map<String, String> correlationIdMap = new HashMap<>()
        correlationIdMap.put( "name", "correlationId" )
        correlationIdMap.put( "value", correlationId )

        final Map<String, String> correlationLogLevelMap = new HashMap<>()
        correlationLogLevelMap.put( "name", "correlationLogLevel" )
        correlationLogLevelMap.put( "value", "%-5p" )

        final Map<String, String> correlationTenantMap = new HashMap<>()
        correlationTenantMap.put( "name", "correlationTenant" )
        correlationTenantMap.put( "value", "TheTenant" )

        final Map<String, String> correlationSomethingElseMap = new HashMap<>()
        correlationSomethingElseMap.put( "name", "correlationSomethingElse" )
        correlationSomethingElseMap.put( "value", "OtherValue" )

        final List<Map<String, String>> correlationData = new ArrayList<>()
        correlationData.add( correlationIdMap )
        correlationData.add( correlationLogLevelMap )
        correlationData.add( correlationTenantMap )
        correlationData.add( correlationSomethingElseMap )

        MDC.put( CORRELATION_DATA_LOG_VAR_NAME, JsonOutput.toJson( correlationData ) )
        true
    }

    boolean after() {
        MDC.remove( CORRELATION_DATA_LOG_VAR_NAME )
        true
    }

    void afterView() {
        // no-op
    }

    private String getCorrelationIdFromHeader(final HttpServletRequest request) {
        String correlationId = request.getHeader(CORRELATION_ID_HEADER_NAME)
        if ( !correlationId ) {
            correlationId = generateUniqueCorrelationId()
        }

        return correlationId;
    }

    private String generateUniqueCorrelationId() {
        return UUID.randomUUID().toString()
    }
}

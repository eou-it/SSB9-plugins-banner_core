/* *****************************************************************************
 Copyright 2016-2018 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.apisupport

import groovy.util.logging.Slf4j
import grails.util.Holders  as CH
import org.springframework.web.context.request.RequestContextHolder

@Slf4j
class ApiUtils {


    // Identifies URL parts for requests that represent API requests.
    //
    private static List apiUrlPrefixes = null

    // Identifies URL parts for requests where no session should be used.
    // This list will be used to identify connections that should not be cached
    // within the HTTP session. Note: To avoid creating an HTTP session, the spring
    // security filter chain must configured. This list should contain a subset of
    // the 'apiUrlPrefixes' list, for those API endpoints that are intended for
    // external consumption (versus used via Ajax on behalf of authenticated users).
    //
    private static List avoidSessionsFor = null

    public static boolean shouldCacheConnection() {
        boolean isWebRequest = RequestContextHolder.getRequestAttributes() != null

        // We'll only cache connections for web requests
        if (!isWebRequest) return false

        // and then only if the web request is not one configured to avoid sessions
        def forwardUri = RequestContextHolder.getRequestAttributes()?.getRequest().forwardURI

        // First, we'll cache the configured url parts that identify requests
        // that should not use HTTP sessions.
        if (avoidSessionsFor == null) {
            avoidSessionsFor = CH.config.avoidSessionsFor instanceof List ? CH.config.avoidSessionsFor : []
            if (avoidSessionsFor.size() > 0) {
                log.info "DB connections will not be cached in the HTTP session for URLs containing: ${avoidSessionsFor.join(',')}"
            }
        }

        // so we can check to see if our current request matches one of them
        boolean avoidCaching = avoidSessionsFor.any { forwardUri =~ "/$it/" }

        if (avoidCaching) {
            log.trace "shouldCacheConnection() returning 'false' (API requests are configured to not use sessions)"
            return false
        }
        log.trace "shouldCacheConnection() returning $isWebRequest"
        return isWebRequest
    }


    public static boolean isApiRequest() {

        if (!apiUrlPrefixes) {
            apiUrlPrefixes = CH.config?.apiUrlPrefixes instanceof List ? CH.config.apiUrlPrefixes  : []
            if (apiUrlPrefixes.size() > 0) {
                log.info "Configured to recognize API requests as URLs containing: ${apiUrlPrefixes.join(',')}"
            }
        }
        def forwardUri = RequestContextHolder.getRequestAttributes()?.getRequest()?.forwardURI
        boolean requestIsApi = apiUrlPrefixes.any { forwardUri =~ "/$it/" }
        requestIsApi
    }
}

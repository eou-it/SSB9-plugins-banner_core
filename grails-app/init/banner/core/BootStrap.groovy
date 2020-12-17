/*******************************************************************************
 Copyright 2018-2020 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package banner.core

import grails.util.Environment
import groovy.util.logging.Slf4j

@Slf4j
class BootStrap {
    def authenticationProcessingFilter
    def bannerAuthenticationFailureHandler
    def init = { servletContext ->
        log.debug("Current Env is = ${Environment.current}")
        authenticationProcessingFilter.authenticationFailureHandler = bannerAuthenticationFailureHandler
    }
    def destroy = {
    }
}

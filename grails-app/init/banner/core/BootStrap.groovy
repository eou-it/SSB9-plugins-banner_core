/*******************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package banner.core

class BootStrap {
    def authenticationProcessingFilter
    def bannerAuthenticationFailureHandler
    def init = { servletContext ->
        authenticationProcessingFilter.authenticationFailureHandler = bannerAuthenticationFailureHandler
    }
    def destroy = {
    }
}

/*******************************************************************************
 Copyright 2016  Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
class BannerCoreBootStrap {
    def authenticationProcessingFilter
    def bannerAuthenticationFailureHandler
    def init = {servletContext ->
        authenticationProcessingFilter.authenticationFailureHandler = bannerAuthenticationFailureHandler
    } }
/*******************************************************************************
Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
*******************************************************************************/ 

package net.hedtech.banner.security

class BannerAuthenticationEvent extends org.springframework.context.ApplicationEvent {

    def userName
    def isSuccess
    def message
    def date
    def module
    def severity

    BannerAuthenticationEvent( def userName, def isSuccess, def message, def module, def date, severity ) {
        super('BannerAuthenticationProvider')
        this.userName = userName
        this.isSuccess = isSuccess
        this.message = message
        this.date = date
        this.module = module
        this.severity = severity
    }
}

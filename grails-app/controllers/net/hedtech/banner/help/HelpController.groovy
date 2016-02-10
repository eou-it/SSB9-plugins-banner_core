/*******************************************************************************
Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
*******************************************************************************/ 
package net.hedtech.banner.help

import grails.util.Holders  as CH

class HelpController {

    def menuService

    def index = {
    }

    def url = {
        render(contentType: 'text/json') {
            def url = CH.config.onLineHelp.url
            help(url: url)
        }
    }
}

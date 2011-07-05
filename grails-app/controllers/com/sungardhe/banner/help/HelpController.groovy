package com.sungardhe.banner.help

import org.codehaus.groovy.grails.commons.ConfigurationHolder as CH

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
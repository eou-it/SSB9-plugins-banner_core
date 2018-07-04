/*******************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package banner.core

import grails.boot.*
import grails.boot.config.GrailsAutoConfiguration
import grails.plugins.metadata.*

@PluginSource
class Application extends GrailsAutoConfiguration {
    static void main(String[] args) {
        GrailsApp.run(Application, args)
    }
}
/*******************************************************************************
 Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */

import org.apache.log4j.Logger
import net.hedtech.banner.privacy.PrivacyPolicyFilter
import org.codehaus.groovy.grails.commons.ConfigurationHolder as CH

/**
 * Executes arbitrary code at bootstrap time.
 * Code executed includes:
 * -- Configuring the dataSource to ensure connections are tested prior to use
 * */
class BannerCoreBootStrap {

    def log = Logger.getLogger(this.getClass())

    def grailsApplication
    def institutionService
    def personalPreferenceService

    def init = { servletContext ->

        def dbInstanceName = institutionService.findByKey()?.instanceName
        servletContext.setAttribute("dbInstanceName", dbInstanceName)

        if (!isSsbEnabled()){
           def bannerInbUrl = personalPreferenceService.fetchPersonalPreference("MAGELLAN","SERVER_DESIGNATION","INB")[0]
           servletContext.setAttribute("bannerInbUrl", bannerInbUrl.value)
        }
    }

    def destroy = {
        // no-op
    }

    private def isSsbEnabled() {
        CH.config.ssbEnabled instanceof Boolean ? CH.config.ssbEnabled : false
    }

}

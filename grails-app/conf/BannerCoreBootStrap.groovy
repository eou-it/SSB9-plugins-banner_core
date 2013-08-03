/*******************************************************************************
 Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */

import org.apache.log4j.Logger
import net.hedtech.banner.privacy.PrivacyPolicyFilter


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

        def bannerInbUrl =""
        // def bannerInbUrl = personalPreferenceService.fetchPersonalPreference("MAGELLAN","SERVER_DESIGNATION","INB")[0]
        servletContext.setAttribute("bannerInbUrl", bannerInbUrl)
    }

    def destroy = {
        // no-op
    }

}

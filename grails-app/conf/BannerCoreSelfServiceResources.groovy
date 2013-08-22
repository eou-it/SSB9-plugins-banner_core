
/** *****************************************************************************
 Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */

modules = {

    'userAgreement' {
        dependsOn "bannerSelfService, i18n-core"
        defaultBundle environment == "development" ? false : "userAgreement"
        //defaultBundle false
        resource url: [plugin: 'banner-core', file: 'css/policy.css'], attrs: [media: 'screen, projection']
        resource url: [plugin: 'banner-core', file: 'css/timeout.css'], attrs: [media: 'screen, projection']
    }

}

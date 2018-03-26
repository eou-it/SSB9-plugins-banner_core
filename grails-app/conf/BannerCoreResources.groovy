/*******************************************************************************
 Copyright 2016-2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

modules = {

    'changePasswordCommon' {
        resource url: [plugin: 'banner-core', file: 'js/changeexpiredpassword.js']
    }

    'changePasswordLTR' {
        dependsOn "changePasswordCommon"
        resource url: [plugin: 'banner-core', file: 'css/changeexpiredpassword.css'], attrs: [media: 'screen, projection']
    }

    'changePasswordRTL' {
        dependsOn "changePasswordCommon"
        resource url: [plugin: 'banner-core', file: 'css/changeexpiredpassword-rtl.css'], attrs: [media: 'screen, projection']
    }

    'bannerCommon' {
        resource url: [plugin: 'banner-core', file: 'js/bannerCommon.js']
    }

    'eds' {
        resource url: 'https://cdn.elluciancloud.com/assets/1.3.0/css/ellucian-design-system-ltr.min.css'
    }

    'edsRTL' {
        resource url: 'https://cdn.elluciancloud.com/assets/1.3.0/css/ellucian-design-system-rtl.min.css'
    }

    'bannerCommonLTR' {
        dependsOn "bannerCommon , eds"
        resource url: [plugin: 'banner-core', file: 'css/bannerCommon.css'], attrs: [media: 'screen, projection']
        resource url: [plugin: 'banner-core', file: 'css/timeout.css'], attrs: [media: 'screen, projection']
    }

    'bannerCommonRTL' {
        dependsOn "bannerCommon, edsRTL"
        resource url: [plugin: 'banner-core', file: 'css/bannerCommon-rtl.css'], attrs: [media: 'screen, projection']
        resource url: [plugin: 'banner-core', file: 'css/timeout-rtl.css'], attrs: [media: 'screen, projection']
    }

}


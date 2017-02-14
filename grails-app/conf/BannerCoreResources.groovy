/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
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

    'bannerCommonLTR' {
        dependsOn "bannerCommon"
        resource url: [plugin: 'banner-core', file: 'css/bannerCommon.css'], attrs: [media: 'screen, projection']
        resource url: [plugin: 'banner-core', file: 'css/timeout.css'], attrs: [media: 'screen, projection']
    }

    'bannerCommonRTL' {
        dependsOn "bannerCommon"
        resource url: [plugin: 'banner-core', file: 'css/bannerCommon-rtl.css'], attrs: [media: 'screen, projection']
        resource url: [plugin: 'banner-core', file: 'css/timeout-rtl.css'], attrs: [media: 'screen, projection']
    }

}


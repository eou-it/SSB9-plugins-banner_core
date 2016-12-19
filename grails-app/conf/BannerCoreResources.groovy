/*******************************************************************************
 Copyright 2009-2016 Ellucian Company L.P. and its affiliates.
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

}


/*******************************************************************************
 Copyright 2017-2019 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.ui

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import grails.util.Holders
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

@Integration
@Rollback
class AnalyticsTagLibIntegrationTests extends BaseIntegrationTestCase {
    def grailsApplication
    def taglib


    @Before
    void setUp(){
        formContext = ['GUAGMNU']
        super.setUp()
        taglib = grailsApplication.mainContext.getBean(AnalyticsTagLib.class.name)
    }


    @After
    public void tearDown() {
        super.tearDown()
    }

    @Test
    void testTagLibWithNoTracking() {
        def thisTagArgs = [:]
        Holders.config.banner.analytics.allowEllucianTracker = false
        assertEquals '',taglib.analytics(thisTagArgs).toString()
        Holders.config.banner.analytics.remove("allowEllucianTracker")
    }


    @Test
    void testTagLibWithClientTrackerAndEllucianTracker() {
        def thisTagArgs = [:]
        Holders.config.banner.analytics.trackerId = "UA-84226422-1"
        assertEquals '<script>\n' +
                '\n' +
                '(function(i,s,o,g,r,a,m){i[\'GoogleAnalyticsObject\']=r;i[r]=i[r]||function(){\n' +
                '                        (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),\n' +
                '                    m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)\n' +
                '            })(window,document,\'script\',\'https://www.google-analytics.com/analytics.js\',\'ga\');\n' +
                'ga(\'create\', \'UA-84226422-1\', \'auto\');\n' +
                ' ga(\'send\', \'pageview\');ga(\'set\', \'anonymizeIp\',true);\n'+
                'ga(\'create\', \'UA-75215910-1\', \'auto\', \'Ellucian\');\n' +
                ' ga(\'Ellucian.send\', \'pageview\');</script>',taglib.analytics(thisTagArgs).toString()

        Holders.config.banner.analytics.remove("trackerId")


    }

    @Test
    void testTagLibWithClientTrackerAndNoEllucianTracker() {
        def thisTagArgs = [:]
        Holders.config.banner.analytics.trackerId = "UA-84226422-1"
        Holders.config.banner.analytics.allowEllucianTracker = false
        def expectedContent = '<script>\n' +
                '\n' +
                '(function(i,s,o,g,r,a,m){i[\'GoogleAnalyticsObject\']=r;i[r]=i[r]||function(){\n' +
                '                        (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),\n' +
                '                    m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)\n' +
                '            })(window,document,\'script\',\'https://www.google-analytics.com/analytics.js\',\'ga\');\n' +
                'ga(\'create\', \'UA-84226422-1\', \'auto\');\n' +
                ' ga(\'send\', \'pageview\');ga(\'set\', \'anonymizeIp\',true);\n' +
                '</script>'
        assertEquals expectedContent , taglib.analytics(thisTagArgs).toString()
        Holders.config.banner.analytics.remove("trackerId")
        Holders.config.banner.analytics.remove("allowEllucianTracker")
    }

    @Test
    void testTagLibanonymizeIpFlagFalse() {
        def thisTagArgs = [:]
        Holders.config.banner.analytics.trackerId = "UA-84226422-1"
        Holders.config.banner.analytics.allowEllucianTracker = false
        Holders.config.banner.analytics.anonymizeIp = false
        def expectedContent = '<script>\n' +
                '\n' +
                '(function(i,s,o,g,r,a,m){i[\'GoogleAnalyticsObject\']=r;i[r]=i[r]||function(){\n' +
                '                        (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),\n' +
                '                    m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)\n' +
                '            })(window,document,\'script\',\'https://www.google-analytics.com/analytics.js\',\'ga\');\n' +
                'ga(\'create\', \'UA-84226422-1\', \'auto\');\n' +
                ' ga(\'send\', \'pageview\');ga(\'set\', \'anonymizeIp\',false);\n' +
                '</script>'
        assertEquals expectedContent , taglib.analytics(thisTagArgs).toString()
        Holders.config.banner.analytics.remove("trackerId")
        Holders.config.banner.analytics.remove("allowEllucianTracker")
        Holders.config.banner.analytics.remove("anonymizeIp")
    }

    @Test
    void testTagLibWithDefault() {
        def thisTagArgs = [:]
        def expectedContent =   '<script>\n' +
                '\n' +
                '(function(i,s,o,g,r,a,m){i[\'GoogleAnalyticsObject\']=r;i[r]=i[r]||function(){\n' +
                '                        (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),\n' +
                '                    m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)\n' +
                '            })(window,document,\'script\',\'https://www.google-analytics.com/analytics.js\',\'ga\');\n' +
                'ga(\'set\', \'anonymizeIp\',true);\n'+
                'ga(\'create\', \'UA-75215910-1\', \'auto\', \'Ellucian\');\n' +
                ' ga(\'Ellucian.send\', \'pageview\');' +
                '</script>'
        assertEquals expectedContent ,taglib.analytics(thisTagArgs).toString()
    }

}

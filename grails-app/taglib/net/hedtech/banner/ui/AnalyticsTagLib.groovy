// Copyright 2013-2016 Ellucian Company L.P. and its affiliates.
package net.hedtech.banner.ui

import grails.util.Holders

class AnalyticsTagLib {
    //GrailsApplication grailsApplication
    def analytics = { attrs, body ->
        def text
        def clientTracker = ""
        def ellucianTracker = ""
        def clientTrackerId
        def allowEllucianTracker
        clientTrackerId = Holders.config.banner.analytics.trackerId
        allowEllucianTracker = Holders.config.banner.analytics.allowEllucianTracker instanceof Boolean ? Holders.config.banner.analytics.allowEllucianTracker  : true
        if (!clientTrackerId && allowEllucianTracker == false) {
            out << ""
        } else {
            def analytics = new StringBuffer();
            def analyticsBody = "<script>\n" +
                    "\n" +
                    "(function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){\n" +
                    "                        (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),\n" +
                    "                    m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)\n" +
                    "            })(window,document,'script','https://www.google-analytics.com/analytics.js','ga');\n";


            if (clientTrackerId) {
                clientTracker = "ga('create', '" + clientTrackerId + "', 'auto');\n" +
                        " ga('send', 'pageview');";
            }
            if (allowEllucianTracker != false) {
                ellucianTracker = "ga('create', 'UA-75215910-1', 'auto', 'Ellucian');\n" +
                        " ga('Ellucian.send', 'pageview');";
            }

            String scriptClose = "</script>"
            analytics.append(analyticsBody);
            analytics.append(clientTracker);
            analytics.append(ellucianTracker);
            analytics.append(scriptClose);
            out << analytics.toString()
        }
    }


}

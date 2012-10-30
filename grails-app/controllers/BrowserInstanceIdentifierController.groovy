import org.springframework.web.context.request.RequestContextHolder
import net.hedtech.banner.configuration.HttpRequestUtils

/*******************************************************************************
Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
*******************************************************************************/ 

class BrowserInstanceIdentifierController {

    def redirect = {
        String cookieName = HttpRequestUtils.getBrowserInstanceCookieName()

        if (HttpRequestUtils.isCookieEmpty(cookieName)){
            HttpRequestUtils.deleteCookie(cookieName, response)
        }

        render view: "ManageBrowserInstance",
                model: [
                    urlToRedirect: getUrlToRedirect (), cookieName: cookieName,
                    cookieValue: new Date(),
                    appContextName:HttpRequestUtils.appContextName
                ]
    }

    private def getUrlToRedirect () {
        def urlToRedirect = request.contextPath + "/banner.zul"
        if (RequestContextHolder.currentRequestAttributes()?.request?.getQueryString()) {
            urlToRedirect = urlToRedirect + "?" + RequestContextHolder.currentRequestAttributes()?.request?.getQueryString()
        }
        urlToRedirect
    }
}
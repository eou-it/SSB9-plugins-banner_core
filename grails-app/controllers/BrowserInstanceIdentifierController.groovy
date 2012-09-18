import org.springframework.web.context.request.RequestContextHolder

/*******************************************************************************
Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
*******************************************************************************/ 

class BrowserInstanceIdentifierController {

    def redirect = {
        render view: "ManageBrowserInstance", model: [urlToRedirect: getUrlToRedirect ()]
    }

    private def getUrlToRedirect () {
        def urlToRedirect = request.contextPath + "/banner.zul"
        if (RequestContextHolder.currentRequestAttributes()?.request?.getQueryString()) {
            urlToRedirect = urlToRedirect + "?" + RequestContextHolder.currentRequestAttributes()?.request?.getQueryString()
        }
        urlToRedirect
    }
}
import org.springframework.web.context.request.RequestContextHolder
import net.hedtech.banner.configuration.HttpRequestUtils

/*******************************************************************************
Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
*******************************************************************************/ 

class BrowserInstanceIdentifierController {

    def redirect = {
        String cookieName = HttpRequestUtils.getBrowserInstanceCookieName()

        render view: "ManageBrowserInstance",
                model: [
                        urlToRedirect: HttpRequestUtils.getUrlToRedirect (),
                        cookieName: cookieName,
                        cookieValue: cookieName,
                        appContextName: HttpRequestUtils.appContextName,
                        serverName: HttpRequestUtils.serverName(),
                        serverPort: HttpRequestUtils.serverPort()
                ]
    }

}
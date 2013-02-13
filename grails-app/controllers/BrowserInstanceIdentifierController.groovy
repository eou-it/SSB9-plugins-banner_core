import org.springframework.web.context.request.RequestContextHolder
import net.hedtech.banner.configuration.HttpRequestUtils

/*******************************************************************************
Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
*******************************************************************************/ 

class BrowserInstanceIdentifierController {

    def redirect = {
        String appContextName = HttpRequestUtils.appContextName
        String serverName =  HttpRequestUtils.serverName()
        String serverPort = HttpRequestUtils.serverPort()
        String prompt = message( code: "net.hedtech.banner.browserInstanceIdentifier.multiWindow.error", args: [appContextName, serverName, serverPort])
        String cookieName = HttpRequestUtils.getBrowserInstanceCookieName()

        render view: "ManageBrowserInstance",
                model: [
                        urlToRedirect: HttpRequestUtils.getUrlToRedirect (),
                        cookieName: cookieName,
                        cookieValue: cookieName,
                        appContextName: appContextName,
                        serverName: serverName,
                        serverPort: serverPort,
                        prompt : prompt
                ]
    }

}
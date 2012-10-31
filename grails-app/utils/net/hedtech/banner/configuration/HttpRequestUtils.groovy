/*******************************************************************************
Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
*******************************************************************************/

package net.hedtech.banner.configuration

import javax.servlet.http.Cookie
import org.springframework.web.context.request.RequestContextHolder

/**
 * Created by IntelliJ IDEA.
 * User: rajanandppk
 * Date: 10/25/12
 * Time: 3:59 PM
 * To change this template use File | Settings | File Templates.
 */
class HttpRequestUtils {

    public static URL getRequestUrlInfo () {
        new URL(request?.requestURL?.toString())
    }

    public static def getRequest () {
        RequestContextHolder.currentRequestAttributes()?.request
    }

    public static def getAppContextName () {
        request.contextPath
    }

    def static getBrowserInstanceCookieName () {
        def requestUrl = requestUrlInfo
        requestUrl.host + "_" + requestUrl.port + "_" + request.contextPath[1..(request.contextPath.size()-1)]
    }

    def static serverName () {
        requestUrlInfo.host
    }

    def static serverPort () {
        ""+requestUrlInfo.port
    }

    def static Cookie getCookie (String cookieName) {
        return getAllCookies()?.find { it.getName() == cookieName }
    }

    static def isCookieEmpty (String cookieName) {
        getCookie(cookieName)?.value == ""
    }

    static def getAllCookies() {
        request.getCookies()
    }

    def static getBrowserInstanceCookie () {
        getCookie(browserInstanceCookieName)
    }

    def static deleteCookie (String cookieName, response) {
        Cookie cookie = new Cookie(cookieName, null);
        cookie.setPath(appContextName);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    static def getUrlToRedirect () {
        def urlToRedirect = request.contextPath + "/banner.zul"
        String queryString = request?.getQueryString()
        if (queryString) {
            urlToRedirect = urlToRedirect + "?" + queryString
        }
        urlToRedirect
    }

}

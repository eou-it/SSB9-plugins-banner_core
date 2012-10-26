package net.hedtech.banner.configuration

import org.springframework.web.context.request.RequestContextHolder
import org.zkoss.zk.ui.Executions
import javax.servlet.http.Cookie

/**
 * Created by IntelliJ IDEA.
 * User: rajanandppk
 * Date: 10/25/12
 * Time: 3:59 PM
 * To change this template use File | Settings | File Templates.
 */
class HttpRequestUtils {

    public static URL getRequestUrlInfo () {
        new URL(getRequest()?.requestURL?.toString())
    }

    public static def getRequest () {
        RequestContextHolder.currentRequestAttributes()?.request
    }

    public static def getAppContextName () {
        getRequest().contextPath
    }

    def static getBrowserInstanceCookieName () {
        requestUrlInfo.host + "_" + requestUrlInfo.port + "_" + request.contextPath[1..(request.contextPath.size()-1)]

    }

    def static Cookie getCookie (String cookieName) {
        Cookie[] cookies = Executions.getCurrent().getNativeRequest().getCookies ()
        return cookies?.find { it.getName() == cookieName }
    }

    def static getBrowserInstanceCookie () {
        getCookie(HttpRequestUtils.getBrowserInstanceCookieName())
    }



}

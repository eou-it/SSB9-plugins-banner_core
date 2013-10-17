/*******************************************************************************
 Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */

package net.hedtech.banner.webutils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created with IntelliJ IDEA.
 * User: rajanandpk
 * Date: 14/10/13
 * Time: 11:15 AM
 * To change this template use File | Settings | File Templates.
 */
public class HttpServletRequestUtils {

    public static final String EMPTY_STRING = "";

    public static URL getRequestUrlInfo (HttpServletRequest request) throws MalformedURLException {
        StringBuffer requestURL = request.getRequestURL();
        String requestString = requestURL.toString();
        return new URL(requestString);
    }

    public static Cookie getCookie (String cookieName, HttpServletRequest request) {
        Cookie[] allCookies = getAllCookies(request);

        if (allCookies != null) {
            int arrayIndex = 0;
            while (arrayIndex < allCookies.length) {
                Cookie cookie = allCookies[arrayIndex];
                String currentCookieName = cookie.getName();
                if (!(EMPTY_STRING.equals(currentCookieName)) && currentCookieName.equals(cookieName)) {
                    return cookie;
                }
                arrayIndex ++;
            }
        }
        return null;
    }

    public static Cookie[] getAllCookies(HttpServletRequest request) {
        return request.getCookies();
    }
}

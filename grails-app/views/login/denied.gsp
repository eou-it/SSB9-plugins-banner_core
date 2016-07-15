<!--
/*******************************************************************************
Copyright 2009-2014 Ellucian Company L.P. and its affiliates.
*******************************************************************************/
-->
<%@ page contentType="text/html;charset=UTF-8" defaultCodec="none" %>
<html>
<head>
    <title><g:message code="net.hedtech.banner.productTitle"/></title>
    <link rel="stylesheet" href="${resource(plugin: 'bannerCore', dir: 'css', file: 'timeout.css')}"/>
    <g:set var="actionLabel" value="${g.message(code: 'net.hedtech.banner.custom.error.action')}"/>
    <g:set var="target" value="${request.contextPath}${uri}"/>
    <link rel="shortcut icon" href="${resource(plugin: 'bannerCore', dir: 'images', file: 'favicon.ico')}" type="image/x-icon"/>

    <g:set var="themeConfig" value="${grails.util.Holders.config.banner.theme}"/>
    <g:if test="${themeConfig.url}">
        <link rel="stylesheet" type="text/css" href="${themeConfig.url}/getTheme?name=${session.mep ?: themeConfig.name}&template=${themeConfig.template}&mep=${session.mep}">
    </g:if>
</head>

<body>
    <div class="header">
        <div class="institutionalBranding"></div>
        <div class="buttonBar">
            <div class="home"></div>
            <div class="menuArrow"></div>
            <div class="fauxBar"></div>
            <div class="fauxBarEndCap"></div>
        </div>
    </div>
    <div class="dialog">
        <div class="message"><g:message code="net.hedtech.banner.access.denied.message"/></div>
        <button onclick=location.href="${target}">${actionLabel}</button>
    </div>
    <div class="footer">
        <span class="logo"></span>
    </div>
</body>
</html>

<%--~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  Copyright 2014 Ellucian Company L.P. and its affiliates.
  ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~--%>

<%@ page contentType="text/html;charset=UTF-8" defaultCodec="none" %>
<html>
<head>
    <title><g:message code="net.hedtech.banner.productTitle"/></title>
    <link rel="stylesheet" href="${resource(plugin: 'bannerCore', dir: 'css', file: 'timeout.css')}"/>
    <g:set var="actionLabel" value="${g.message(code: 'net.hedtech.banner.custom.error.action')}"/>
    <g:set var="target" value="${request.contextPath}${uri}"/>

    <g:set var="themeConfig" value="${grails.util.Holders.config.banner.theme}"/>
    <g:if test="${themeConfig.url}">
        <link rel="stylesheet" type="text/css" href="${themeConfig.url}/getTheme?name=${session.mep ?: themeConfig.name}&template=${themeConfig.template}&mep=${session.mep}">
    </g:if>
</head>

<body>
<g:analytics/>
<div class="header">
    <div class="institutionalBranding"></div>
</div>
<div class="dialog">
    <div class="message">${msg}</div>
    <button onclick=location.href="${target}" autofocus="true">${actionLabel}</button>
</div>
<div class="footer">
    <span class="logo"></span>
</div>
</body>
</html>

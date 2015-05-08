<!--
/*******************************************************************************
Copyright 2009-2014 Ellucian Company L.P. and its affiliates.
*******************************************************************************/
-->
<%@ page contentType="text/html;charset=UTF-8" defaultCodec="none" %>
<html>
<head>
    <title><g:message code="net.hedtech.banner.productTitle"/></title>
    <link rel="stylesheet" href="${resource(plugin: 'bannerCore', dir: 'css', file: 'logout.css')}"/>
    <g:if test="${message(code: 'default.language.direction') == 'rtl'}">
        <link rel="stylesheet" href="${resource(plugin: 'bannerCore', dir: 'css', file: 'logout-rtl.css')}"/>
    </g:if>
</head>

<body>
<div class="header">
    <div class="institutionalBranding"></div>
</div>
<div class="dialog">
    <g:if test="${show}">
        <div class="LogoutSuccessImage"></div>
        <div class="message"><g:message code="net.hedtech.banner.logout.message"/></div>
        <div class="button-div">
            <input type="button" value="${g.message(code:'net.hedtech.banner.logout.backToHomeButton')}" class="btn-primary" autofocus="true"/>
        </div>
    </g:if>
    <g:if test="${logoutUri != null}">
        <div class="message"><g:message code="net.hedtech.banner.logout.options.message"/></div>
        <div class="button-div">
            <input type="button" value="${g.message(code:'net.hedtech.banner.logout.action')}" class="btn-primary" autofocus="true"/>
            <input type="button" value="${g.message(code:'net.hedtech.banner.logout.backToHomeButton')}" class="btn-default" />
        </div>
    </g:if>
    <g:if test="${logoutUri == null && !show}">
        <div class="message"><g:message code="net.hedtech.banner.logout.returnMessage"/></div>
        <div class="button-div">
            <input type="button" value="${g.message(code:'net.hedtech.banner.logout.backToHomeButton')}" class="btn-primary" autofocus="true"/>
        </div>
    </g:if>
</div>
<div class="footer">
    <span class="logo"></span>
</div>
</body>
</html>
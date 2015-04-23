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
</head>

<body>
<div class="header">
    <div class="institutionalBranding"></div>
</div>
<div class="dialog">
    <g:if test="${logoutUri == null}"><div class="message"><g:message code="net.hedtech.banner.logout.message"/></div></g:if>
    <a href="${uri}"><g:message code="net.hedtech.banner.logout.action"/></a><g:message code="net.hedtech.banner.logout.returnLinkMessage"/></br>
    <g:if test="${logoutUri != null}"><a href="${logoutUri}"><g:message code="net.hedtech.banner.logout.action"/></a><g:message code="net.hedtech.banner.logout.globalLogoutMessage"/></g:if>
</div>
<div class="footer">
    <span class="logo"></span>
</div>
</body>
</html>
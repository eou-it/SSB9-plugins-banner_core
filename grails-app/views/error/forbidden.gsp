<!--
/*******************************************************************************
Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
*******************************************************************************/
-->
<%@ page contentType="text/html;charset=UTF-8" defaultCodec="none" %>
<html>
    <head>
        <title><g:message code="net.hedtech.banner.productTitle"/></title>
        <link rel="stylesheet" href="${resource(plugin: 'bannerCore', dir: 'css', file: 'timeout.css')}"/>
        <g:set var="actionLabel" value="${g.message(code: 'net.hedtech.banner.access.denied.dialog.action')}"/>
        <g:set var="target" value="${request.contextPath}${uri}"/>
        <link rel="shortcut icon" href="${resource(plugin: 'bannerCore', dir: 'images', file: 'favicon.ico')}" type="image/x-icon"/>
    </head>
    <body>
        <div class="dialog">
            <div class="message"><g:message code="net.hedtech.banner.access.denied.message"/></div>
            <div class="actionMessage">${g.message(code: "net.hedtech.banner.logout.timeout.dialog.actionMessage", args: ["<a href=\"$target\">$actionLabel</a>"])}</div>
        </div>
    </body>
</html>

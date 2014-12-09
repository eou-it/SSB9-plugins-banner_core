<%--~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  Copyright 2014 Ellucian Company L.P. and its affiliates.
  ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~--%>

<%@ page contentType="text/html;charset=UTF-8" defaultCodec="none" %>
<html>
<head>
    <title><g:message code="net.hedtech.banner.productTitle"/></title>
    <link rel="stylesheet" href="${resource(plugin: 'bannerCore', dir: 'css', file: 'timeout.css')}"/>
    <g:set var="actionLabel" value="${g.message(code: 'net.hedtech.banner.errors.serverError.backToHomeButton.label')}"/>
    <g:set var="target" value="${request.contextPath}${uri}"/>
    <link rel="shortcut icon" href="${resource(plugin: 'bannerCore', dir: 'images', file: 'favicon.ico')}" type="image/x-icon"/>
</head>

<body>
<div class="header">
    <div class="institutionalBranding"></div>
</div>
<div class="dialog">
    <div class="message">${msg}</div>
    <button onclick=location.href="${target}">${actionLabel}</button>
</div>
<div class="footer">
    <span class="logo"></span>
</div>
</body>
</html>

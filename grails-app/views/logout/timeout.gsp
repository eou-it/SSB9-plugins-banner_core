<!--
/*******************************************************************************
Copyright 2009-2017 Ellucian Company L.P. and its affiliates.
*******************************************************************************/
-->
<%@ page contentType="text/html;charset=UTF-8" defaultCodec="none" %>
<!DOCTYPE html>
<html lang="${message(code: 'default.language.locale')}">
<head>
   <meta name="layout" content="bannerCommonPage"/>
    <g:set var="actionLabel" value="${g.message(code: 'net.hedtech.banner.logout.timeout.dialog.action')}"/>
    <g:set var="target" value="${request.contextPath}${uri}"/>
</head>

<body dir="auto">
    <div class="dialog-mask" role="main">
        <div class="dialog-wrapper">
            <div class="dialog">
                <div class="dialog-content" role="dialog" id="dialog-message">
                    <div class="title"><g:message code="net.hedtech.banner.logout.timeout.dialog.title"/></div>
                    <div class="message"><g:message code="net.hedtech.banner.logout.timeout.dialog.message"/></div>
                </div>
                <div class="dialog-sign" >
                    <button class="common-button-primary" aria-describedby="dialog-message" autofocus onclick=location.href="${target}">${actionLabel}</button>
                </div>
            </div>
        </div>
    </div>
</body>
</html>

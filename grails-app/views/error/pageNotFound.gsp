<!--
/*******************************************************************************
Copyright 2009-2020 Ellucian Company L.P. and its affiliates.
*******************************************************************************/
-->                                                                                                                                                         ,
<%@ page contentType="text/html;charset=UTF-8" defaultCodec="none" %>
<html>
<head>
    <meta name="layout" content="bannerCommonPage"/>
    <g:set var="actionLabel" value="${g.message(code: 'net.hedtech.banner.access.denied.dialog.action')}"/>
    <g:set var="returnHomeLinkAddress" value="${returnHomeLinkAddress}"/>
</head>
<body>

<div class="dialog-mask">
    <div class="dialog-wrapper">
        <div class="dialog">
            <div class="dialog-content" role="dialog" id="dialog-message">
                <div class="message"><g:message code="net.hedtech.banner.errors.serverError.pageNotFoundMessage"/></div>
            </div>
            <div class="dialog-sign">
                    <input type="button" aria-describedby="dialog-message" autofocus value="${g.message(code:'net.hedtech.banner.errors.serverError.backToHomeButton.label')}" class="common-button-primary" onclick='location.href="${returnHomeLinkAddress}"'/>
            </div>
        </div>
    </div>
</div>
</body>
</html>

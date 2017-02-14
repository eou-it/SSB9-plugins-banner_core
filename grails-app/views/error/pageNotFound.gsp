<!--
/*******************************************************************************
Copyright 2017 Ellucian Company L.P. and its affiliates.
*******************************************************************************/
-->                                                                                                                                                         ,
<%@ page contentType="text/html;charset=UTF-8" defaultCodec="none" %>
<html>
<head>
    <meta name="layout" content="bannerCommonPage"/>
    <g:set var="actionLabel" value="${g.message(code: 'net.hedtech.banner.access.denied.dialog.action')}"/>
</head>
<body>

<div class="dialog-mask">
    <div class="dialog-wrapper">
        <div class="dialog" role="main">
            <div class="dialog-content">
                <div class="message"><g:message code="net.hedtech.banner.errors.serverError.pageNotFoundMessage"/></div>
            </div>
            <div class="dialog-sign">
                <g:link uri="${returnHomeLinkAddress}">
                    <input type="button" value="${g.message(code:'net.hedtech.banner.errors.serverError.backToHomeButton.label')}" class="common-button-primary" />
                </g:link>
            </div>
        </div>
    </div>
</div>
</body>
</html>

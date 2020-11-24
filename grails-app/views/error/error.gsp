<!--
/*******************************************************************************
Copyright 2009-2020 Ellucian Company L.P. and its affiliates.
*******************************************************************************/
-->
<%@ page contentType="text/html;charset=UTF-8" defaultCodec="none" %>

<html>
    <head>
        <meta name="layout" content="bannerCommonPage"/>
        <g:set var="actionLabel" value="${g.message(code: 'net.hedtech.banner.access.denied.dialog.action')}"/>
    </head>
    <body>
        <div class="dialog-mask" role="main">
            <div class="dialog-wrapper">
                <div class="dialog">
                    <div class="dialog-content" role="dialog" id="dialog-message">
                        <div class="title">
                            <g:message code="net.hedtech.banner.errors.serverError.message"/>
                        </div>
                        <div class="title">
                            <g:message code="net.hedtech.banner.errors.serverError.error"/>
                        </div>
                        <div class="message">
                            ${exception?.message?.encodeAsHTML()}
                        </div>
                    </div>
                    <div class="dialog-sign">
                       <g:link uri="${returnHomeLinkAddress}">
                            <input type="button"  aria-describedby="dialog-message" autofocus value="${g.message(code:'net.hedtech.banner.errors.serverError.backToHomeButton.label')}" class="common-button-primary" />
                       </g:link>
                    </div>
                </div>
            </div>
        </div>
    </body>
</html>

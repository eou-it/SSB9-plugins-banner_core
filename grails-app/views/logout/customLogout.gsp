<!--
/*******************************************************************************
Copyright 2009-2017 Ellucian Company L.P. and its affiliates.
*******************************************************************************/
-->
<%@ page contentType="text/html;charset=UTF-8" defaultCodec="none" %>
<html>
<head>
    <meta name="layout" content="bannerCommonPage"/>
    <g:set var="targetHome" value="${uri}"/>
    <g:set var="targetLogout" value="${logoutUri}"/>
</head>

<body>
    <div class="dialog-mask">
        <div class="dialog-wrapper">
            <div class="custom-logout-dialog" role="dialog" id="dialog-message">
                <g:if test="${show}">
                    <div class="dialog-content"><div class="message"><g:message code="net.hedtech.banner.logout.message"/></div></div>
                    <div class="dialog-sign">
                        <input type="button" aria-describedby="dialog-message" autofocus value="${g.message(code:'net.hedtech.banner.logout.backToHomeButton')}" class="common-button-primary" onclick='location.href="${targetHome}"'/>
                    </div>
                </g:if>
                <g:if test="${logoutUri != null}">
                    <div class="dialog-content"><div class="message"><g:message code="net.hedtech.banner.logout.options.message"/></div></div>
                    <div class="dialog-sign">
                        <input type="button" aria-describedby="dialog-message" autofocus value="${g.message(code:'net.hedtech.banner.logout.action')}" class="common-button-primary" onclick='location.href="${targetLogout}"'/>
                        <input type="button" value="${g.message(code:'net.hedtech.banner.logout.backToHomeButton')}" class="common-button-primary" onclick='location.href="${targetHome}"' />
                    </div>
                </g:if>
                <g:if test="${logoutUri == null && !show}">
                    <div class="dialog-content"><div class="message"><g:message code="net.hedtech.banner.logout.returnMessage"/></div></div>
                    <div class="dialog-sign">
                        <input type="button"  aria-describedby="dialog-message" autofocus value="${g.message(code:'net.hedtech.banner.logout.backToHomeButton')}" class="common-button-primary" onclick='location.href="${targetHome}"'/>
                    </div>
                </g:if>
            </div>
        </div>
    </div>
</body>
</html>

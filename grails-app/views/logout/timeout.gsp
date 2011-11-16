<%@ page contentType="text/html;charset=UTF-8" defaultCodec="none" %>
<html>
    <head>
        <title><g:message code="com.sungardhe.banner.productTitle"/> - <g:message code="com.sungardhe.banner.logout.timeout.title"/></title>
        <link rel="stylesheet" href="${resource(dir: 'css', file: 'timeout.css')}"/>
        <g:set var="actionLabel" value="${g.message(code: 'com.sungardhe.banner.logout.timeout.dialog.action')}" />
        <g:set var="target" value="${request.contextPath}${uri}" />
    </head>
    <body>
        <div class="dialog">
            <div class="title"><g:message code="com.sungardhe.banner.logout.timeout.dialog.title"/></div>
            <div class="message"><g:message code="com.sungardhe.banner.logout.timeout.dialog.message"/></div>
            <div class="actionMessage">${g.message( code: "com.sungardhe.banner.logout.timeout.dialog.actionMessage", args: ["<a href=\"$target\">$actionLabel</a>"] )}</div>
        </div>
    </body>
</html>
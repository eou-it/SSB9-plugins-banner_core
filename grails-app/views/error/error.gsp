<%@ page contentType="text/html;charset=UTF-8" defaultCodec="none" %>
<% request.getSession().invalidate() %>

<html>
    <head>
        <title><g:message code="com.sungardhe.banner.productTitle"/></title>
        <link rel="stylesheet" href="${resource(plugin: 'bannerCore', dir: 'css', file: 'timeout.css')}"/>
        <link rel="stylesheet" href="${resource(plugin: 'bannerCore', dir: 'css', file: 'main.css')}"/>
        <g:set var="actionLabel" value="${g.message(code: 'com.sungardhe.banner.access.denied.dialog.action')}"/>
        <g:set var="target" value="${request.requestURL}"/>
        <link rel="shortcut icon" href="${resource(plugin: 'bannerCore', dir: 'images', file: 'favicon.ico')}" type="image/x-icon"/>
    </head>
    <body>
        <div class="error">
            <div class="errorBox">
              <div class="errorMessage"><g:message code="com.sungardhe.banner.errors.serverError.message"/>
                <br><br>
                <g:message code="com.sungardhe.banner.errors.serverError.error"/>
                ${exception.message?.encodeAsHTML()}
              </div>
              <div class="errorBackButton">
                <input type="button" value="${g.message(code:'com.sungardhe.banner.errors.serverError.backToHomeButton.label')}" class="buttons" onclick="javascript:window.location='${target.substring(0,target.lastIndexOf("/") + 1)}';"/>
              </div>
            </div>
        </div>
    </body>
</html>
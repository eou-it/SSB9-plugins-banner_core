<%@ page contentType="text/html;charset=UTF-8" %>
<html>
    <head>
        <title><g:message code="com.sungardhe.banner.productTitle"/> - <g:message code="com.sungardhe.banner.logout.timeout.title"/></title>
    </head>
    <body>
        <div class="dialog">
            <div class="message"><g:message code="com.sungardhe.banner.logout.timeout.message"/></div>
            <div class="action"><a href="${request.contextPath}${uri}"><g:message code="com.sungardhe.banner.logout.timeout.acknowledgement"/></a></div>
        </div>
    </body>
</html>
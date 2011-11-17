<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
   <title><g:message code="com.sungardhe.banner.forgotpin.forgotpassword.title"/></title>
   <meta name="layout" content="main"/>
   <link rel="stylesheet" href="${resource(dir: 'css', file: 'forgotpin.css')}"/>
</head>

<body>
<div id="mainContent" class="page-with-sidebar">
    <div class="ui-layout-center inner-content" id="inner-content">
        <div class="inner-center">
            <div id="forgotpin" class="ui-widget ui-widget-section">
                <div class="ui-widget-header"> <g:message code="com.sungardhe.banner.forgotpin.forgotpassword.title"/> </div>
                <div class="main-wrapper" >
                    <div class="ui-widget-panel">
                    <form  action="${postUrl}" method="post" id="answerForm">
                        <table cellpadding="5" cellspacing="10" class="input-table">
                           <g:if test="${flash.message}">
                               <tr><td colspan="2"> <div style="broder: 1px soild red; background-color: #ffffe0; color: red; font-weight: bold; text-align: center;">${flash.message}</div> </td> </tr>
                           </g:if>
                           <tr><td class="tabletext"> <g:message code="com.sungardhe.banner.forgotpin.username"/> :</td><td class="tabledata"><input type="text" readonly="readonly" value="${userName}" name="username" class="input-text"/> </td></tr>
                           <g:each in="${questions}">
                                <tr><td class="tabletext" ><g:message code="com.sungardhe.banner.forgotpin.question"/>:</td><td class="tabledata">  ${it[1]}</td></tr>
                                <tr><td class="tabletext" ><g:message code="com.sungardhe.banner.forgotpin.answer"/> : </td><td class="tabledata"><input type="text" name="answer${it[0]}" class="input-text"/> </td></tr>
                           </g:each>
                        </table>
                        <div class="button-bar-container">
                              <div class="button-bar">
                                  <button id="cancelButton1" class="ui-corner-all ui-button ui-widget"><g:message code="com.sungardhe.banner.forgotpin.button.cancel"/></button>
                                  <button id="createAccount1" class="ui-corner-all ui-button ui-widget" type="submit"><g:message code="com.sungardhe.banner.forgotpin.button.validate"/></button>
                              </div>
                        </div>
                     </form>
                </div>
                </div>
            </div>
        </div>
    </div>
</div>
</body>
</html>
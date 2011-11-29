<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
   <title><g:message code="com.sungardhe.banner.resetpassword.forgotpassword.title"/></title>
   <meta name="layout" content="main"/>
   <link rel="stylesheet" href="${resource(dir: 'css', file: 'resetpassword.css')}"/>
    <script language="javascript">
        function gotoLogin(){
            var form = document.getElementById('answerForm');
            form.action='${cancelUrl}';
            form.submit();
        }
    </script>
</head>

<body>
<div id="mainContent" class="page-with-sidebar">
    <div class="ui-layout-center inner-content" id="inner-content">
        <div class="inner-center">
            <div id="resetpassword" class="ui-widget ui-widget-section">
                <div class="ui-widget-header"> <g:message code="com.sungardhe.banner.resetpassword.forgotpassword.title"/> </div>
                <div class="main-wrapper" >
                    <div class="ui-widget-panel">
                    <form  action="${postUrl}" method="post" id="answerForm">
                        <table cellpadding="5" cellspacing="10" class="input-table">
                           <tr><td class="tabletext"> <g:message code="com.sungardhe.banner.resetpassword.username"/> :</td><td class="tabledata"><input type="text" readonly="readonly" value="${userName}" name="username" class="input-text disabled-state"/> </td></tr>
                            <g:if test="${questionValidationMap}">
                               <g:each in="${questions}">
                                    <tr><td class="tabletext" ><g:message code="com.sungardhe.banner.resetpassword.question"/>:</td><td class="tabledata">  ${it[1]}</td></tr>
                                    <g:if test="${questionValidationMap.get(it[0]) == ''}">
                                        <tr><td class="tabletext invalid" ><g:message code="com.sungardhe.banner.resetpassword.answer"/> : </td><td class="tabledata"><input type="text" name="answer${it[0]}" id="answer${it[0]}"class="input-text error-state"/> </td></tr>
                                    </g:if>
                                   <g:else>
                                        <tr><td class="tabletext" ><g:message code="com.sungardhe.banner.resetpassword.answer"/> : </td><td class="tabledata"><input type="text" name="answer${it[0]}" id="answer${it[0]}"class="input-text default-state" value='${questionValidationMap?.get(it[0])}'/> </td></tr>
                                   </g:else>
                               </g:each>
                           </g:if>
                            <g:else>
                                <g:each in="${questions}">
                                    <tr><td class="tabletext" ><g:message code="com.sungardhe.banner.resetpassword.question"/>:</td><td class="tabledata">  ${it[1]}</td></tr>
                                    <tr><td class="tabletext" ><g:message code="com.sungardhe.banner.resetpassword.answer"/> : </td><td class="tabledata"><input type="text" name="answer${it[0]}" id="answer${it[0]}"class="input-text default-state"/> </td></tr>
                                </g:each>
                            </g:else>
                        </table>
                       <div class="button-bar-container">
                              <div class="button-bar">
                                  <button id="cancelButton1" class="ui-corner-all ui-button ui-widget" onclick="gotoLogin()"><g:message code="com.sungardhe.banner.resetpassword.button.cancel"/></button>
                                  <button id="createAccount1" class="ui-corner-all ui-button ui-widget" type="submit"><g:message code="com.sungardhe.banner.resetpassword.button.validate"/></button>
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
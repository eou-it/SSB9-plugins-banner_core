<%--
  Created by IntelliJ IDEA.
  User: Vijendra.Rao
  Date: 3/11/11
  Time: 1:37 PM
  To change this template use File | Settings | File Templates.
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<html>
  <head>
      <title><g:message code="com.sungardhe.banner.resetpassword.resetpassword.title"/></title>
       <meta name="layout" content="main"/>
       <link rel="stylesheet" href="${resource(dir: 'css', file: 'resetpassword.css')}"/>
       <script language="javascript">
            function gotoLogin(){
                var form = document.getElementById('resetPinForm');
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
                  <div class="ui-widget-header"><g:message code="com.sungardhe.banner.resetpassword.resetpassword.title"/></div>
                  <div class="main-wrapper" >
                      <div class="ui-widget-panel">
                      <form action="${postBackUrl}" method="post" id="resetPinForm">
                          <table cellpadding="5" cellspacing="10" class="input-table">
                             <g:if test="${flash.message}">
                                 <tr><td colspan="2"> <div style="broder: 1px soild red; background-color: #ffffe0; color: red; font-weight: bold; text-align: center;">${flash.message}</div> </td> </tr>
                             </g:if>
                             <tr><td class="tabledata" colspan="2"><g:message code="com.sungardhe.banner.resetpassword.resetpassword.message"/></td></tr>
                             <tr><td class="tabletext"> <g:message code="com.sungardhe.banner.resetpassword.newpassword"/>:</td><td class="tabledata"><input type="password" name="password" class="input-text default-state"/> </td></tr>
                             <tr><td class="tabletext"> <g:message code="com.sungardhe.banner.resetpassword.repassword" /> :</td><td  class="tabledata"><input type="password" name="repassword" class="input-text default-state"/> </td></tr>
                          </table>
                          <div class="button-bar-container">
                                <div class="button-bar">
                                    <button id="cancelButton1" class="ui-corner-all ui-button ui-widget" onclick="gotoLogin()"><g:message code="com.sungardhe.banner.resetpassword.button.cancel"/></button>
                                    <button id="createAccount1" class="ui-corner-all ui-button ui-widget" type="submit"><g:message code="com.sungardhe.banner.resetpassword.button.submit"/></button>
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
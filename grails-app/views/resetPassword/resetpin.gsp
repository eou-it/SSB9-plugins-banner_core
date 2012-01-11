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
       <meta name="layout" content="bannerSelfServicePage"/>
       <link rel="stylesheet" href="${resource(dir: 'css', file: 'resetpassword.css')}"/>
       <script language="javascript">
            function gotoLogin(){
                var form = document.getElementById('resetPinForm');
                form.action='${cancelUrl}';
                form.submit();
            }
           $(document).ready(function (){
            setTimeout(function() {
                $(".error-state").each(function(i, element){
                    var errorNotification = new Notification({message: "${flash.message}", type: "error", id: $(element).attr("id")});
                    notifications.addNotification(errorNotification);
                })
            }, 500);

             $("input").blur(function(e){
                 var emptyErrorMessage = "${message( code:"com.sungardhe.banner.resetpassword.password.required.error" )}";
                 var passwordMatchError = "${message( code:"com.sungardhe.banner.resetpassword.password.match.error" )}";
                var element = $(e.currentTarget);
                if(element.val().trim() != "" && element.hasClass("error-state")){
                    element.removeClass("error-state");
                    element.addClass("default-state");
                    element.parent().prev().removeClass("invalid");
                    notifications.remove(notifications.get(element.attr("id")));
                }
                else if(element.val().trim() == ""){
                    element.addClass("error-state");
                    element.removeClass("default-state");
                    element.parent().prev().addClass("invalid");
                    if(notifications.get(element.attr("id"))){
                       notifications.remove(notifications.get(element.attr("id")))
                    }
                    var errorNotification = new Notification({message: emptyErrorMessage, type: "error", id: $(element).attr("id")});
                    notifications.addNotification(errorNotification);
                }
                if($("#password").val().trim().length != 0 && $("#repassword").val().trim().length){
                   notifications.remove(notifications.get("password"));
                   notifications.remove(notifications.get("repassword"));
                   if($("#password").val() != $("#repassword").val()){
                      var errorNotification = new Notification({message: passwordMatchError, type: "error", id: "match"});
                      notifications.addNotification(errorNotification);
                   }
                    else{
                       notifications.remove(notifications.get("match"));
                   }
                }

            });
            $("input").focus(function(e){
                var element = $(e.currentTarget);
                if( element.parent().prev().hasClass("invalid")){
                    element.parent().prev().removeClass("invalid");
                }
            });
        });
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
                             <tr><td class="tabledata" colspan="2"><g:message code="com.sungardhe.banner.resetpassword.resetpassword.message"/></td></tr>
                              <g:if test="${flash.message}">
                                    <tr><td class="tabletext"> <g:message code="com.sungardhe.banner.resetpassword.newpassword"/>:</td><td class="tabledata"><input type="password" id="password" name="password" class="input-text error-state"/> </td></tr>
                                    <tr><td class="tabletext"> <g:message code="com.sungardhe.banner.resetpassword.repassword" /> :</td><td  class="tabledata"><input type="password" id="repassword" name="repassword" class="input-text error-state"/> </td></tr>
                              </g:if>
                              <g:else>
                                 <tr><td class="tabletext"> <g:message code="com.sungardhe.banner.resetpassword.newpassword"/>:</td><td class="tabledata"><input type="password" id="password" name="password" class="input-text default-state"/> </td></tr>
                                 <tr><td class="tabletext"> <g:message code="com.sungardhe.banner.resetpassword.repassword" /> :</td><td  class="tabledata"><input type="password" id="repassword" name="repassword" class="input-text default-state"/> </td></tr>
                             </g:else>
                          </table>
                          <div class="button-bar-container">
                                <div class="button-bar">
                                    <button id="cancelButton1" class="secondary-button" onclick="gotoLogin()"><g:message code="com.sungardhe.banner.resetpassword.button.cancel"/></button>
                                    <button id="createAccount1" class="primary-button" type="submit"><g:message code="com.sungardhe.banner.resetpassword.button.submit"/></button>
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
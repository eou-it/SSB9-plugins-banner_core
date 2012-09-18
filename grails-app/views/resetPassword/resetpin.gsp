<!--
/*******************************************************************************
Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
*******************************************************************************/
-->

<%@ page contentType="text/html;charset=UTF-8" %>
<html>
  <head>
      <title><g:message code="net.hedtech.banner.resetpassword.resetpassword.title"/></title>
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
                    var errorMessageList = "${flash.message}".split("::::");
                    for(var i=0; i< errorMessageList.length; i++){
                        var error = errorMessageList[i].replace(/:/g, "");
                        var errorNotification = new Notification({message: error, type: "error", id: $(element).attr("id")});
                        notifications.addNotification(errorNotification);
                    }
                })
            }, 500);

             $("input").blur(function(e){
                 var emptyErrorMessage = "${message( code:"net.hedtech.banner.resetpassword.password.required.error" )}";
                 var passwordMatchError = "${message( code:"net.hedtech.banner.resetpassword.password.match.error" )}";
                var element = $(e.currentTarget);
                if(element.val().trim() != "" && element.hasClass("error-state")){
                    element.removeClass("error-state");
                    element.addClass("default-state");
                    element.parent().prev().removeClass("invalid");
                    while(notifications.length != 0){
                        notifications.remove(notifications.first());
                    }
                }
                else if(element.val().trim() == ""){
                    element.addClass("error-state");
                    element.removeClass("default-state");
                    element.parent().prev().addClass("invalid");
                    while(notifications.length != 0){
                       notifications.remove(notifications.first())
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

            setTimeout( function(){
                $('input:password').attr('value', '');
            }, 100);
        });
        </script>
  </head>
  <body>
  <div id="mainContent" class="page-with-sidebar">
      <div class="ui-layout-center inner-content" id="inner-content">
          <div class="inner-center">
              <div id="resetpassword" class="ui-widget ui-widget-section">
                  <div class="ui-widget-header"><g:message code="net.hedtech.banner.resetpassword.resetpassword.title"/></div>
                  <div class="main-wrapper" >
                      <div class="ui-widget-panel">
                      <form action="${postBackUrl}" method="post" id="resetPinForm">
                          <table cellpadding="5" cellspacing="10" class="input-table">
                             <tr><td class="tabledata" colspan="2"><g:message code="net.hedtech.banner.resetpassword.resetpassword.message"/></td></tr>
                              <g:if test="${flash.message}">
                                    <tr><td class="tabletext"> <g:message code="net.hedtech.banner.resetpassword.newpassword"/>:</td><td class="tabledata"><input type="password" id="password" name="password" class="input-text error-state" autocomplete="off"/> </td></tr>
                                    <tr><td class="tabletext"> <g:message code="net.hedtech.banner.resetpassword.repassword" /> :</td><td  class="tabledata"><input type="password" id="repassword" name="repassword" class="input-text error-state" autocomplete="off"/> </td></tr>
                              </g:if>
                              <g:else>
                                 <tr><td class="tabletext"> <g:message code="net.hedtech.banner.resetpassword.newpassword"/>:</td><td class="tabledata"><input type="password" id="password" name="password" class="input-text default-state" autocomplete="off"/> </td></tr>
                                 <tr><td class="tabletext"> <g:message code="net.hedtech.banner.resetpassword.repassword" /> :</td><td  class="tabledata"><input type="password" id="repassword" name="repassword" class="input-text default-state" autocomplete="off"/> </td></tr>
                             </g:else>
                          </table>
                          <div class="button-bar-container">
                                <div class="button-bar">
                                    <button id="cancelButton1" class="secondary-button" onclick="gotoLogin()"><g:message code="net.hedtech.banner.resetpassword.button.cancel"/></button>
                                    <button id="createAccount1" class="primary-button" type="submit"><g:message code="net.hedtech.banner.resetpassword.button.submit"/></button>
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

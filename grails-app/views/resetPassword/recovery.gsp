<!--
/*******************************************************************************
Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
*******************************************************************************/
-->
<%@ page contentType="text/html;charset=UTF-8" %>
<html>
  <head>
      <title><g:message code="net.hedtech.banner.resetpassword.recoverycode.title"/></title>
       <meta name="layout" content="bannerSelfServicePage"/>
       <meta name="menuBaseURL" content="${createLink(uri: '/ssb')}" />
      <g:if test="${message(code: 'default.language.direction') == 'rtl'}">
          <link rel="stylesheet" href="${resource(plugin: 'bannerCore', dir: 'css', file: 'resetpassword-rtl.css')}"/>
      </g:if>
      <g:else>
          <link rel="stylesheet" href="${resource(plugin: 'bannerCore', dir: 'css', file: 'resetpassword.css')}"/>
      </g:else>
       <script language="javascript">
            function gotoLogin(){
                var form = document.getElementById('recoveryForm');
                form.action='${cancelUrl}';
                form.submit();
            }
           $(document).ready(function (){
            setTimeout(function() {
                $(".error-state").each(function(i, element){
                    if($(element).attr("data-error-message").trim().length != 0){
                        var errorNotification = new Notification({message: $(element).attr("data-error-message"), type: "error", id: $(element).attr("id")});
                        notifications.addNotification(errorNotification);
                    }
                })
            }, 500);

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
                      <form action="${postUrl}" method="post" id="recoveryForm">
                          <table cellpadding="5" cellspacing="10" class="input-table">
                             <g:if test="${infoPage}">
                                 <tr><td class="tabledata"> A web page link has been sent to your e-mail address. Use the link to reset your password.</td> </tr>
                             </g:if>
                              <g:elseif test="${nonPidmIdm}">
                                  <input type="hidden" name="nonPidmId" value='${nonPidmIdm}'/>
                             <tr><td class="tabledata" colspan="2"><g:message code="net.hedtech.banner.resetpassword.recoverycode.message"/></td></tr>
                             <g:if test="${flash.message}">
                                <tr><td class="tabletext"> <g:message code="net.hedtech.banner.resetpassword.recoverycode"/>:</td><td class="tabledata"><input type="password" name="recoverycode" class="eds-text-field error-state" data-error-message="${flash.message}" autocomplete="off"/> </td></tr>
                             </g:if>
                             <g:else>
                                <tr><td class="tabletext"> <g:message code="net.hedtech.banner.resetpassword.recoverycode"/>:</td><td class="tabledata"><input type="password" name="recoverycode" class="eds-text-field default-state" autocomplete="off"/> </td></tr>
                             </g:else>
                             </g:elseif>
                          </table>
                          <g:if test="${infoPage}">
                              <div class="button-bar-container">
                              <div class="button-bar">
                                  <button id="closebutton" class="primary" onclick="gotoLogin()"> Close </button>
                              </div>
                              </div>
                          </g:if>
                          <g:else>
                          <div class="button-bar-container">
                              <div class="button-bar">
                                  <button id="cancelButton1" class="secondary" onclick="gotoLogin()"><g:message code="net.hedtech.banner.resetpassword.button.cancel"/></button>
                                  <button id="createAccount1" class="primary" type="submit"><g:message code="net.hedtech.banner.resetpassword.button.continue"/></button>
                              </div>
                       </div>
                       </g:else>
                       </form>
                  </div>
                  </div>
              </div>
          </div>
      </div>
  </div>
  </body>
</html>

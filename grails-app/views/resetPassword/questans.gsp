<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
   <title><g:message code="net.hedtech.banner.resetpassword.forgotpassword.title"/></title>
   <meta name="layout" content="bannerSelfServicePage"/>
   <link rel="stylesheet" href="${resource(dir: 'css', file: 'resetpassword.css')}"/>
    <script language="javascript">
        function gotoLogin(){
            var form = document.getElementById('answerForm');
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

             $("input").blur(function(e){
                var emptyErrorMessage = "${message( code:"net.hedtech.banner.resetpassword.question.answer.required.error" )}";
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
                    var errorNotification = new Notification({message: emptyErrorMessage+$(element).attr("id").charAt($(element).attr("id").length-1), type: "error", id: $(element).attr("id")});
                    notifications.addNotification(errorNotification);
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
                <div class="ui-widget-header"> <g:message code="net.hedtech.banner.resetpassword.forgotpassword.title"/> </div>
                <div class="main-wrapper" >
                    <div class="ui-widget-panel">
                    <form  action="${postUrl}" method="post" id="answerForm">
                        <table cellpadding="5" cellspacing="10" class="input-table">
                           <tr><td class="tabletext"> <g:message code="net.hedtech.banner.resetpassword.username"/> :</td><td class="tabledata"><input type="text" readonly="readonly" value="${userName}" name="username" class="input-text disabled-state"/> </td></tr>
                            <g:if test="${questionValidationMap}">
                               <g:each in="${questions}">
                                    <tr><td class="tabletext" ><g:message code="net.hedtech.banner.resetpassword.question"/>:</td><td class="tabledata">  ${it[1]}</td></tr>
                                   <g:if test='${questionValidationMap.get(it[0]).get("error")}'>
                                        <tr><td class="tabletext invalid" ><g:message code="net.hedtech.banner.resetpassword.answer"/>  * : </td><td class="tabledata"><input type="password" name="answer${it[0]}" id="answer${it[0]}" class="input-text error-state" data-error-message="${questionValidationMap.get(it[0]).get("message")}" autocomplete="off"/> </td></tr>
                                    </g:if>
                                   <g:else>
                                        <tr><td class="tabletext" ><g:message code="net.hedtech.banner.resetpassword.answer"/>  * : </td><td class="tabledata"><input type="password" name="answer${it[0]}" id="answer${it[0]}" class="input-text default-state" value='${questionValidationMap.get(it[0]).get("answer")}' autocomplete="off"/> </td></tr>
                                   </g:else>
                               </g:each>
                           </g:if>
                            <g:else>
                                <g:each in="${questions}">
                                    <tr><td class="tabletext" ><g:message code="net.hedtech.banner.resetpassword.question"/>:</td><td class="tabledata">  ${it[1]}</td></tr>
                                    <tr><td class="tabletext" ><g:message code="net.hedtech.banner.resetpassword.answer"/>  * : </td><td class="tabledata"><input type="password" name="answer${it[0]}" id="answer${it[0]}" class="input-text default-state" autocomplete="off"/> </td></tr>
                                </g:each>
                            </g:else>
                        </table>
                       <div class="button-bar-container">
                              <div class="button-bar">
                                  <button id="cancelButton1" class="secondary-button" onclick="gotoLogin()"><g:message code="net.hedtech.banner.resetpassword.button.cancel"/></button>
                                  <button id="createAccount1" class="primary-button" type="submit"><g:message code="net.hedtech.banner.resetpassword.button.continue"/></button>
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

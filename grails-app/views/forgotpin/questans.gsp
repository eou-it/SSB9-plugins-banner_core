<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
   <title>Forgot Password</title>
   <meta name="layout" content="main"/>
   <link rel="stylesheet" href="${resource(dir: 'css', file: 'forgotpin.css')}"/>
</head>

<body>
<div id="mainContent" class="page-with-sidebar">
    <div class="ui-layout-center inner-content" id="inner-content">
        <div class="inner-center">
            <div id="forgotpin" class="ui-widget ui-widget-section">
                <div class="ui-widget-header"> Forgot Password </div>
                <div class="main-wrapper" >
                    <div class="ui-widget-panel">
                    <form  action="${postUrl}" method="post" id="answerForm">
                        <table cellpadding="5" cellspacing="10" class="input-table">
                           <g:if test="${flash.message}">
                               <tr><td colspan="2"> <div style="broder: 1px soild red; background-color: #ffffe0; color: red; font-weight: bold; text-align: center;">${flash.message}</div> </td> </tr>
                           </g:if>
                           <tr><td class="tabletext"> User Name :</td><td class="tabledata"><input type="text" readonly="readonly" value="${userName}" name="username" class="input-text"/> </td></tr>
                           <g:each in="${questions}">
                                <tr><td class="tabletext" >Question:</td><td class="tabledata">  ${it[1]}</td></tr>
                                <tr><td class="tabletext" >Answer : </td><td class="tabledata"><input type="text" name="answer${it[0]}" class="input-text"/> </td></tr>
                           </g:each>
                        </table>
                        <div class="button-bar-container">
                              <div class="button-bar">
                                  <button id="cancelButton1" class="ui-corner-all ui-button ui-widget">Cancel</button>
                                  <button id="createAccount1" class="ui-corner-all ui-button ui-widget" type="submit">Validate</button>
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
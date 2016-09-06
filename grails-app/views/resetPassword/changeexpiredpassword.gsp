<!--
/*******************************************************************************
Copyright 2009-2016 Ellucian Company L.P. and its affiliates.
*******************************************************************************/
-->

<head>
    <title><g:message code="net.hedtech.banner.changepassword.title"/></title>
    <meta name="layout" content="bannerSelfServicePage"/>
    <meta name="menuEndPoint" content="${request.contextPath}/ssb/menu"/>
    <meta name="menuBaseURL" content="${request.contextPath}/ssb"/>
    <g:if test="${message(code: 'default.language.direction') == 'rtl'}">
        <r:require modules="changePasswordRTL"/>
    </g:if>
    <g:else>
        <r:require modules="changePasswordLTR"/>
    </g:else>
    <script>
    function gotoLogin() {
        var form = document.getElementById('changePasswordForm');
        form.action = ${cancelUrl};
        form.submit();
    }
    $(document).ready(function () {

        setTimeout(function () {
            $(".error-state").each(function (i, element) {

                var errorMessageList = "${flash.message}".split("::::");
                console.log("len" + errorMessageList.length);
                for (var i = 0; i < errorMessageList.length; i++) {
                    while (notifications.length != 0) {
                        notifications.remove(notifications.first())
                    }
                    var error = errorMessageList[i].replace(/:/g, "");
                    var errorNotification = new Notification({
                        message: error,
                        type: "error",
                        component: $(this),
                        id: $(element).attr("id"),
                        elementToFocus: $(this)
                    });
                    notifications.addNotification(errorNotification);
                }

            })
        }, 500);
    })
    </script>
</head>

<body>
<div id="content" role="main" class="page-with-sidebar">
    <div class="ui-layout-center inner-content" id="inner-content">
        <div class="inner-center">
            <div class="ui-widget ui-widget-section">
                <div class="ui-widget-header"><span class="tabletext"><g:message
                        code="net.hedtech.banner.changepassword.title"/></span></div>

                <div class="main-wrapper">
                    <div class="ui-widget-panel">
                        <span class="notification-icon"></span><span class="tabletext"><g:message
                            code="net.hedtech.banner.changepassword.expired.password"/></span>

                        <form action="${postBackUrl}" method="post" id="changePasswordForm">
                            <table cellpadding="5" cellspacing="10" class="input-table">

                                <g:if test="${flash.message}">

                                    <tr><td class="tabletext"><g:message
                                            code="net.hedtech.banner.changepassword.oldpassword"/>:</td>
                                        <td class="tabledata"><input type="password" id="oldpassword" name="oldpassword"
                                                                     class="input-text error-state"
                                                                     autocomplete="off"/></td></tr>

                                    <tr><td class="tabletext"><g:message
                                            code="net.hedtech.banner.resetpassword.newpassword"/>:</td><td
                                            class="tabledata"><input type="password" id="password" name="password"
                                                                     class="input-text error-state" autocomplete="off"/>
                                    </td></tr>
                                    <tr><td class="tabletext"><g:message
                                            code="net.hedtech.banner.changepassword.repassword"/> :</td><td
                                            class="tabledata"><input type="password" id="repassword" name="repassword"
                                                                     class="input-text error-state" autocomplete="off"/>
                                    </td></tr>
                                </g:if>
                                <g:else>
                                    <tr><td class="tabletext"><g:message
                                            code="net.hedtech.banner.changepassword.oldpassword"/>:</td><td
                                            class="tabledata"><input type="password" id="oldpassword" name="oldpassword"
                                                                     class="input-text default-state"
                                                                     autocomplete="off"/></td></tr>
                                    <tr><td class="tabletext"><g:message
                                            code="net.hedtech.banner.resetpassword.newpassword"/>:</td><td
                                            class="tabledata"><input type="password" id="password" name="password"
                                                                     class="input-text default-state"
                                                                     autocomplete="off"/></td></tr>
                                    <tr><td class="tabletext"><g:message
                                            code="net.hedtech.banner.changepassword.repassword"/> :</td><td
                                            class="tabledata"><input type="password" id="repassword" name="repassword"
                                                                     class="input-text default-state"
                                                                     autocomplete="off"/></td></tr>
                                </g:else>
                            </table>

                            <div class="button-bar-container">
                                <div class="button-bar">
                                    <button id="cancelButton1" class="secondary-button" onclick="gotoLogin()"><g:message
                                            code="net.hedtech.banner.resetpassword.button.cancel"/></button>
                                    <button id="createAccount1" class="primary-button" type="submit"><g:message
                                            code="net.hedtech.banner.resetpassword.button.submit"/></button>
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


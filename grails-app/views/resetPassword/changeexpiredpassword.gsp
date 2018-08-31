<!--
/*******************************************************************************
Copyright 2016-18 Ellucian Company L.P. and its affiliates.
*******************************************************************************/
-->
<html>
<head>
    <title><g:message code="changeExpiredPassword.title"/></title>
    <meta name="layout" content="bannerSelfServicePage"/>
    <meta name="menuEndPoint" content="${request.contextPath}/ssb/menu"/>
    <meta name="menuBaseURL" content="${request.contextPath}/ssb"/>
    <meta name="headerAttributes" content=""/>
    <g:if test="${message(code: 'default.language.direction') == 'rtl'}">
        <r:require modules="changePasswordRTL"/>
    </g:if>
    <g:else>
        <r:require modules="changePasswordLTR"/>
    </g:else>
    <meta name="headerAttributes" content=""/>
    <script type="application/javascript">
        var cancelUrl='${cancelUrl}';
        var flashMessage="${flash.message}";

        document.getElementsByName('headerAttributes')[0].content = JSON.stringify({
            "pageTitle": "<g:message code="changeExpiredPassword.title"/>",
            "breadcrumb": {
                "<g:message code="changeExpiredPassword.title"/>" : ""
            }
        });

    </script>
</head>

<body>
<div id="content" role="main" class="page-with-sidebar">
    <div class="ui-layout-center inner-content" id="inner-content">
        <div class="inner-center">
            <div class="ui-widget ui-widget-section">

                <div class="main-wrapper">
                    <div class="ui-widget-panel">

                        <span class="notification-icon"></span>
                        <span class="tableheader"><g:message code="changeExpiredPassword.expired.password"/></span>

                        <form action="${postBackUrl}" method="post" id="changePasswordForm">
                            <table align="center" class="input-table">
                                    <tr>
                                        <td class="tabletext">
                                           <label for="oldpassword"> <g:message code="changeExpiredPassword.old.password"/>:</label>
                                        </td>
                                        <td class="tabledata">
                                            <input type="password" id="oldpassword" name="oldpassword" class="eds-text-field default-state " autocomplete="off"/>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td class="tabletext">
                                            <label for="password"><g:message code="changeExpiredPassword.new.password"/>:</label>
                                        </td>
                                        <td class="tabledata">
                                            <input type="password" id="password" name="password" class="eds-text-field default-state" autocomplete="off"/>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td class="tabletext">
                                            <label for="repassword"><g:message code="changeExpiredPassword.re.password"/> :</label>
                                        </td>
                                        <td class="tabledata">
                                            <input type="password" id="repassword" name="repassword" class="eds-text-field default-state" autocomplete="off"/>
                                        </td>
                                    </tr>
                            </table>

                            <div class="button-bar-container">
                                <div class="button-bar">
                                    <input type="button" id="cancelChangePasswordButton" class="secondary" value='${message(code: 'changeExpiredPassword.button.cancel')}'  />
                                    <input type="submit" id="changePasswordButton" class="primary" value='${message(code: 'changeExpiredPassword.button.submit')}' />
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
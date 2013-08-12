<%@ page contentType="text/html;charset=UTF-8" defaultCodec="none"%>
<%--
/*******************************************************************************
Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
*******************************************************************************/
--%>
<html>
<head>
    <title><g:message code="net.hedtech.banner.termsofuse.title"/></title>
    <link rel="stylesheet" href="${resource(plugin: 'bannerCore', dir: 'css', file: 'timeout.css')}"/>
    <link rel="stylesheet" href="${resource(plugin: 'bannerCore', dir: 'css', file: 'policy.css')}"/>
    <script type="text/javascript">
        function moveToController () {
            window.location = "${createLink(controller: "userAgreement", action: "agreement")}";
        }
        function exit(){
            window.location = "${createLink(controller: "logout")}";
        }
    </script>
</head>
<body>
<div class="header">
    <div class="institutionalBranding"></div>
</div>

<div id="bodyContainer">
    <div id="pageheader">
        <div id="pagetitle"><g:message code="net.hedtech.banner.termsofuse.title"/></div>
    </div>
    <div id="pagebody" class="loginterms level">
        <div id="contentHolder">
            <div id="contentBelt"></div>
            <div class="pagebodydiv" style="display: block;">
                <div class="termstextdiv">
                    <table class="termstexttable">
                    <tbody>
                    <tr><td class="indefault"></td>
                        <td class="indefault">
                            <span class="termstext"></p>${infoText}<p><br></span>
                        </td>
                     </tr>
                     </tbody>
                    </table>
                </div>
                    <table>
                        <tr>
                            <td>
                                <button id="policy-continue" class="primary-button"
                                        onclick="moveToController()"/>
                                        <g:message code="net.hedtech.banner.termsofuse.button.continue"/>
                                </button>
                                <button id="policy-exit" class="secondary-button" data-endpoint="${createLink(controller: "logout")}"/>
                                    <g:message code="net.hedtech.banner.termsofuse.button.exit"/>
                               </button>
                            </td>
                        </tr>
                    </table>
            </div>
        </div>
    </div>
</div>
<div class="footer">
    <span class="logo"></span>
</div>
</body>
</html>


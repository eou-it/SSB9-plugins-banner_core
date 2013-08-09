<%@ page contentType="text/html;charset=UTF-8" defaultCodec="none"%>
<%--
/*******************************************************************************
Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
*******************************************************************************/
--%>
<html>
<head>
    <title>Term of Condition</title>
    %{--<meta name="layout" content="bannerSelfServicePage"/>--}%
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
        <div id="pagetitle">Terms of Usage</div>
    </div>
    <div id="pagebody" class="loginterms level4">
        <div id="contentHolder">
            <div id="contentBelt"></div>

            <div class="pagebodydiv" style="display: block;">
                <div class="infotextdiv">
                    <table class="infotexttable">

                    <tbody>
                    <tr><td class="indefault"></td>
                        <td class="indefault"><span
                                class="infotext">
                            </p>
                            ${infoText}
                            <p>
                                <br></p></span></td></tr>
                    </tbody>
                </table><p></p>
                </div>
                <table>
                    <tr>
                        <td>
                            <button id="policy-continue" class="primary-button"
                                    onclick="moveToController()"/>Continue</button>
                            <button id="policy-exit" class="secondary-button" onclick="exit()"/>Exit</button>
                        </td>
                    </tr>
                </table>
            </div>
        </div>
    </div>
</div>

</body>
</html>


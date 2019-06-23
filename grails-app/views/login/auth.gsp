<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<!--
/*******************************************************************************
Copyright 2009-2018 Ellucian Company L.P. and its affiliates.
*******************************************************************************/
-->
<html xmlns="http://www.w3.org/1999/xhtml" lang="${message(code: 'default.language.locale')}">
<head>
    <meta name="viewport" content="width=device-width, height=device-height,  initial-scale=1.0"/>
    <meta http-equiv="X-UA-Compatible" content="IE=10" />

    <asset:script>
        window.mepCode='${session.mep}';
    </asset:script>
    <asset:stylesheet href="login.css"/>
    <asset:stylesheet href="login-responsive.css"/>
    <g:if test="${message(code: 'default.language.direction') == 'rtl'}">
        <asset:stylesheet href="rtl-login.css"/>
        <asset:stylesheet href="login-rtl.css"/>
        <asset:stylesheet href="rtl-login-patch.css"/>
        <asset:stylesheet href="login-responsive-rtl.css"/>
    </g:if>
    <title><g:message code="net.hedtech.banner.login.title"/></title>
   %{-- <link rel="stylesheet" href="${resource(plugin: 'bannerCore', dir: 'css', file: 'login.css')}"/>
    <link rel="stylesheet" href="${resource(plugin: 'bannerCore', dir: 'css', file: 'login-responsive.css')}"/>
    <g:if test="${message(code: 'default.language.direction') == 'rtl'}">
        <link rel="stylesheet" href="${resource(plugin: 'bannerCore', dir: 'css', file: 'rtl-login.css')}"/>
        <link rel="stylesheet" href="${resource(plugin: 'bannerCore', dir: 'css', file: 'login-rtl.css')}"/>
        <link rel="stylesheet" href="${resource(plugin: 'bannerCore', dir: 'css', file: 'rtl-login-patch.css')}"/>
        <link rel="stylesheet" href="${resource(plugin: 'bannerCore', dir: 'css', file: 'login-responsive-rtl.css')}"/>
    </g:if>--}%
    <g:set var="themeConfig" value="${grails.util.Holders.config.banner.theme}"/>
    <g:if test="${themeConfig.url}">
        <g:if test="${session.mep}">
            <link rel="stylesheet" type="text/css" href="${themeConfig.url}/getTheme?name=${themeConfig.name + session.mep}&template=${themeConfig.template}&mepCode=${session.mep}">
        </g:if>
        <g:else>
            <link rel="stylesheet" type="text/css" href="${themeConfig.url}/getTheme?name=${themeConfig.name}&template=${themeConfig.template}">
        </g:else>
    </g:if>

    <asset:link rel="apple-touch-icon" sizes="57x57" href="eds/apple-touch-icon-57x57.png"/>
    <asset:link rel="apple-touch-icon" sizes="60x60" href="eds/apple-touch-icon-60x60.png"/>
    <asset:link rel="apple-touch-icon" sizes="72x72" href="eds/apple-touch-icon-72x72.png"/>
    <asset:link rel="apple-touch-icon" sizes="76x76" href="eds/apple-touch-icon-76x76.png"/>
    <asset:link rel="apple-touch-icon" sizes="114x114" href="eds/apple-touch-icon-114x114.png"/>
    <asset:link rel="apple-touch-icon" sizes="120x120" href="eds/apple-touch-icon-120x120.png"/>
    <asset:link rel="apple-touch-icon" sizes="144x144" href="eds/apple-touch-icon-144x144.png"/>
    <asset:link rel="apple-touch-icon" sizes="152x152" href="eds/apple-touch-icon-152x152.png"/>
    <asset:link rel="apple-touch-icon" sizes="180x180" href="eds/apple-touch-icon-180x180.png"/>
    <asset:link rel="shortcut icon" type="image/png" href="eds/favicon-32x32.png" sizes="32x32"/>
    <asset:link rel="shortcut icon" type="image/png" href="eds/android-chrome-192x192.png" sizes="192x192"/>
    <asset:link rel="shortcut icon" type="image/png" href="eds/favicon-96x96.png" sizes="96x96"/>
    <asset:link rel="shortcut icon" type="image/png" href="eds/favicon-16x16.png" sizes="16x16"/>
    <asset:link rel="shortcut icon"  sizes="57x57" href="eds/favicon.ico" type="image/x-icon"/>

</head>

<body class="pageBg">
<g:analytics/>
<g:pageAccessAudit/>
<div class="splashBg">
    <div class="ie-warning" id="ieWarningMessage">
        <div>
            <g:message code="net.hedtech.banner.login.warning"/>
        </div>
    </div>

    <div class="appName">Banner<span>&reg;</span></div>

    <div class="ellucianName"></div>

    <div class="loginMsg" id="loginMsg">
        <g:if test='${flash.message}'>
            <span class="icon-error"></span>${flash.message}
        </g:if>
        <g:elseif test="${flash.reloginMessage}">
            ${flash.reloginMessage}
        </g:elseif>
        <g:else>
            <g:message code="net.hedtech.banner.login.prompt"/>
        </g:else>
    </div>

    <div id="userNameTxt" style="display: none;">${message(code: 'net.hedtech.banner.login.username', default: 'User Name')}</div>

    <div id="passwordTxt" style="display: none;">${message(code: 'net.hedtech.banner.login.password', default: 'Password')}</div>


    <form action='${postUrl}' method='POST' id='loginForm'>
        <div class="logIn">
            <div class="textfield-wrapper">
                <g:if test='${userNameRequired}'>
                    <div class="error-state">
                        <span><input type='text' name='username' id='username' class="eds-text-field"
                                     placeholder="<g:message code="net.hedtech.banner.login.username"/>"
                                     aria-labelledby='userNameTxt'
                                     aria-describedby='loginMsg'/>
                        </span>
                    </div>

                    <div class="">
                        <span><input type='password' name='password' id='password' class="eds-text-field"
                                     placeholder="<g:message code="net.hedtech.banner.login.password"/>"
                                     autocomplete="off" aria-labelledby='passwordTxt'/>
                        </span>
                    </div>
                </g:if>
                <g:elseif test='${flash.message}'>
                    <div class="error-state">
                        <span><input type='text' name='username' id='username' class="eds-text-field"
                                     placeholder="<g:message code="net.hedtech.banner.login.username"/>"
                                     aria-labelledby='userNameTxt'
                                     aria-describedby='loginMsg'/>
                        </span>
                    </div>

                    <div class="error-state">
                        <span><input type='password' name='password' id='password' class="eds-text-field"
                                     placeholder="<g:message code="net.hedtech.banner.login.password"/>"
                                     autocomplete="off" aria-labelledby='passwordTxt'/>
                        </span>
                    </div>
                </g:elseif>
                <g:else>
                    <div class="">
                        <span><input type='text' id="userName" name='username' id='username' class="eds-text-field"
                                     placeholder="<g:message code="net.hedtech.banner.login.username"/>"
                                     aria-labelledby='userNameTxt' aria-describedby='loginMsg'/>
                        </span>
                    </div>

                    <div class="">
                        <span><input type='password' name='password' id='password' class="eds-text-field"
                                     placeholder="<g:message code="net.hedtech.banner.login.password"/>"
                                     autocomplete="off" aria-labelledby='passwordTxt'/></span>
                    </div>
                </g:else>

            </div>
        </div>

        <div class="logIn sign-in">
            <div class="textfield-wrapper">
                <div class="signin-button-wrapper">
                    <input type='submit'
                           value="${message(code: 'net.hedtech.banner.login.signin', default: 'Sign In')}"
                           id="sign-in-btn" onclick="submitForm()"
                           class="login-primary"/>
                </div>
            </div>
        </div>
        <g:if test="${grails.util.Holders.config.ssbPassword.reset.enabled == true || grails.util.Holders.config.ssbPassword.guest.reset.enabled == true}">
            <div class="forgotPasswordDiv"><a onclick="gotoForgotPassword()" href="#" id="forgotpasswordLink"
                                              class="forgotpassword">${message(code: 'net.hedtech.banner.resetpassword.resetpassword.link.message', default: 'Forgot Password')}</a>
            </div>
        </g:if>

    </form>

    <div class="copyright">
        <p>&copy; <g:message code="default.copyright.message"
                   args="${[g.message(code:'default.copyright.startyear'),
                            g.message(code:'default.copyright.endyear')]}"/>
        </p>

        <p><g:message code="net.hedtech.banner.login.copyright2"/></p>
    </div>
</div>

<script type='text/javascript'>
    (function () {
        document.forms['loginForm'].elements['username'].focus();

        if (isIe() && (getIEDocMode() < 8)) {
            document.getElementById("ieWarningMessage").style.visibility = "visible";
        }
        initializeHandlersForForgotPasswordLink();
    })();

    function isIe() {
        return (navigator.appName == 'Microsoft Internet Explorer');
    }

    function getIEDocMode() {
        // If we are in IE 8 (any mode) or previous versions of IE,
        // we check for the documentMode or compatMode for pre 8 versions
        return (document.documentMode)
            ? document.documentMode
            : (document.compatMode && document.compatMode == "CSS1Compat")
                ? 7
                : 5; // default to quirks mode IE5
    }

    function openWindow() {
        <g:set var="onLineHelpUrl" value="${grails.util.Holders.config.onLineHelp.url}" />

        window.open("${onLineHelpUrl}?productName=general&formName=login", '_blank');
        return false;
    }

    function gotoForgotPassword() {
        var form = document.getElementById('loginForm');
        form.action = '${forgotPasswordUrl}';
        form.submit();
    }

    function submitForm() {
        var form = document.getElementById('loginForm');
        form.action = '${postUrl}';
        form.submit();
    }

    function initializeHandlersForForgotPasswordLink() {
        var SPACE_KEY = 32;
        var ENTER_KEY = 13;
        var anchorLink = document.getElementById("forgotpasswordLink");
        if (anchorLink != undefined) {
            anchorLink.onkeypress = function (evt) {
                evt = evt || window.event;
                if (evt.which == SPACE_KEY || evt.which == ENTER_KEY) {
                    gotoForgotPassword();
                }
            }
        }
    }
</script>
</body>
</html>
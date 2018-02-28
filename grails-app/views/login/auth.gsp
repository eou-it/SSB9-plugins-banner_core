<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<!--
/*******************************************************************************
Copyright 2009-2017 Ellucian Company L.P. and its affiliates.
*******************************************************************************/
-->
<html xmlns="http://www.w3.org/1999/xhtml" lang="${message(code: 'default.language.locale')}">
<head>
    <script>
        window.mepCode='${session.mep}';
    </script>
    <title><g:message code="net.hedtech.banner.login.title"/></title>
    <link rel="shortcut icon" href="${resource(plugin: 'bannerCore', dir: 'images', file: 'favicon.ico')}"
          type="image/x-icon"/>
    <link rel="stylesheet" href="${resource(plugin: 'bannerCore', dir: 'css', file: 'login.css')}"/>
    <g:if test="${message(code: 'default.language.direction') == 'rtl'}">
        <link rel="stylesheet" href="${resource(plugin: 'bannerCore', dir: 'css', file: 'rtl-login.css')}"/>
    </g:if>
    <g:set var="themeConfig" value="${grails.util.Holders.config.banner.theme}"/>
    <g:if test="${themeConfig.url}">
        <g:if test="${session.mep}">
            <link rel="stylesheet" type="text/css" href="${themeConfig.url}/getTheme?name=${themeConfig.name + session.mep}&template=${themeConfig.template}&mepCode=${session.mep}">
        </g:if>
        <g:else>
            <link rel="stylesheet" type="text/css" href="${themeConfig.url}/getTheme?name=${themeConfig.name}&template=${themeConfig.template}">
        </g:else>
    </g:if>
</head>

<body class="pageBg">
<g:analytics/>
<div class="splashBg">
    <div class="ie-warning" id="ieWarningMessage">
        <div>
            <g:message code="net.hedtech.banner.login.warning"/>
        </div>
    </div>

    <div class="appName">Banner<span>&reg;</span></div>

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

    <div class="ellucianName">ellucian<span class="trademark">TM</span></div>

    <div id="userNameTxt" style="display: none;">User Name</div>

    <div id="passwordTxt" style="display: none;">Password</div>


    <form action='${postUrl}' method='POST' id='loginForm'>
        <div class="logIn">
            <div class="textfield-wrapper">
                <g:if test='${userNameRequired}'>
                    <div class="userName-error-state">
                        <span><input type='text' name='j_username' id='j_username'
                                   aria-labelledby='userNameTxt'
                                   aria-describedby='loginMsg'/>
                        </span>
                    </div>

                    <div class="password">
                        <span><input type='password' name='j_password' id='j_password'
                                   autocomplete="off" aria-labelledby='passwordTxt'/>
                        </span>
                    </div>
                </g:if>
                <g:elseif test='${flash.message}'>
                    <div class="userName-error-state">
                        <span><input type='text' name='j_username' id='j_username'
                                   aria-labelledby='userNameTxt'
                                   aria-describedby='loginMsg'/>
                        </span>
                    </div>

                    <div class="password-error-state">
                        <span><input type='password' name='j_password' id='j_password'
                                   autocomplete="off" aria-labelledby='passwordTxt'/>
                        </span>
                    </div>
                </g:elseif>
                <g:else>
                    <div class="userName">
                        <span><input type='text' id="userName" name='j_username' id='j_username'
                                  aria-labelledby='userNameTxt' aria-describedby='loginMsg'/>
                        </span>
                    </div>

                    <div class="password">
                        <span><input type='password' name='j_password' id='j_password'
                               autocomplete="off" aria-labelledby='passwordTxt'/></span>
                    </div>
                </g:else>
                <div class="signin-button-wrapper">
                    <input type='submit'
                          value="${message(code: 'net.hedtech.banner.login.signin', default: 'Sign In')}"
                          id="sign-in-btn" height="32px" onclick="submitForm()"
                          class="signin-button"/>
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
        <p>&copy; <g:message code="net.hedtech.banner.login.copyright1"/></p>

        <p><g:message code="net.hedtech.banner.login.copyright2"/></p>
    </div>
</div>

<script type='text/javascript'>
    (function () {
        document.forms['loginForm'].elements['j_username'].focus();

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

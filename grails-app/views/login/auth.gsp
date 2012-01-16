<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title><g:message code="com.sungardhe.banner.login.title"/></title>
    <link rel="shortcut icon" href="${resource(dir:'images',file:'favicon.ico')}" type="image/x-icon"/>
    <link rel="stylesheet" href="${resource(dir: 'css', file: 'login.css')}"/>
<!--<link rel="stylesheet" href="${resource(dir: 'css', file: 'rtl-login.css')}"/>-->
    <g:if test="${message(code: 'default.language.direction')  == 'rtl'}">
        <link rel="stylesheet" href="${resource(dir: 'css', file: 'rtl-login.css')}"/>
    </g:if>
<!--[if IE 7]>
	<link href="fix-ie7.css" rel="stylesheet" type="text/css" />

<![endif]-->
</head>

<body class="pageBg">

<div class="splashBg">
   <div class="ie-warning" id="ieWarningMessage">
       <div>
         <g:message code="com.sungardhe.banner.login.warning"/>
       </div>
    </div>
	<div class="appName">Banner<span> &reg;</span></div>
    <g:if test='${flash.message}'>
        <div class='loginMsg'><span class="icon-error"></span>${flash.message}</div>
    </g:if>
    <g:elseif test='${flash.message}'>
	  <div class="loginMsg"><g:message code="com.sungardhe.banner.login.prompt"/></div>
    </g:elseif>
    <g:else test="${flash.reloginMessage}">
      <div class="loginMsg">${flash.reloginMessage}</div>
    </g:else>
	<div class="logIn">
      <form action='${postUrl}' method='POST' id='loginForm'>
		<div class="textfield-wrapper">
            <g:if test='${userNameRequired}'>
				<div class="userName-error-state"><span><input type='text'  name='j_username' id='j_username' /></span></div>
				<div class="password"><span><input type='password' name='j_password' id='j_password' autocomplete="off" /></span></div>
			</g:if>
			<g:elseif test='${flash.message}'>
				<div class="userName-error-state"><span><input type='text'  name='j_username' id='j_username' /></span></div>
				<div class="password-error-state"><span><input type='password' name='j_password' id='j_password' autocomplete="off" /></span></div>
			</g:elseif>
			<g:else>
				<div class="userName"><span><input type='text'  name='j_username' id='j_username' /></span></div>
				<div class="password"><span><input type='password' name='j_password' id='j_password' autocomplete="off" /></span></div>
			</g:else>
			<div class="signin-button-wrapper"><input type='submit' value="${message(code: 'com.sungardhe.banner.login.signin', default: 'Sign In')}" id="sign-in-btn" height="32px"  onclick="submitForm()" class="signin-button"/></div>
            <g:if test="${org.codehaus.groovy.grails.commons.ConfigurationHolder.config.ssbEnabled == true}">
                <div class="forgotPasswordDiv"><a onclick="gotoForgotPassword()" href="#" class="forgotpassword">Forgot Password </a></div>
            </g:if>
		</div>
      </form>
	</div>
	<div class="copyright">
		<p>&copy; <g:message code="com.sungardhe.banner.login.copyright1"/></p>
		<p><g:message code="com.sungardhe.banner.login.copyright2"/></p>
	</div>
</div>

<script type='text/javascript'>
	(function(){
		document.forms['loginForm'].elements['j_username'].focus();

		if (isIe() && (getIEDocMode() < 8)) {
            document.getElementById("ieWarningMessage").style.visibility = "visible";
		}
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
	<g:set var="onLineHelpUrl" value="${org.codehaus.groovy.grails.commons.ConfigurationHolder.config.onLineHelp.url}" />

        window.open("${onLineHelpUrl}?productName=general&formName=login" , '_blank');
        return false;
    }

	function gotoForgotPassword(){
         var form = document.getElementById('loginForm');
         form.action = '${forgotPasswordUrl}';
         form.submit();
    }

    function submitForm(){
      var form = document.getElementById('loginForm');
         form.action = '${postUrl}';
         form.submit();
    }
</script>

</body>
</html>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>Login</title>
<link rel="stylesheet" href="${resource(dir: 'css', file: 'login.css')}"/>
<!--<link rel="stylesheet" href="${resource(dir: 'css', file: 'rtl-login.css')}"/>-->
<!--[if IE 7]>
	<link href="fix-ie7.css" rel="stylesheet" type="text/css" />
<![endif]-->
</head>

<body class="pageBg">

<div class="splashBg">
	<div class="appName">Banner 9.0</div>
    <g:if test='${flash.message}'>
        <div class='loginMsg'><span class="icon-error"></span>${flash.message}</div>
    </g:if>
    <g:else test='${flash.message}'>
	  <div class="loginMsg"><g:message code="com.sungardhe.banner.login.prompt"/></div>
    </g:else>
	<div class="logIn">
      <form action='${postUrl}' method='POST' id='loginForm'>
		<div class="textfield-wrapper">
			<g:if test='${flash.message}'>
				<div class="userName-error-state"><span><input type='text'  name='j_username' id='j_username' /></span></div>
				<div class="password-error-state"><span><input type='password' name='j_password' id='j_password' /></span></div>
			</g:if>
			<g:else>
				<div class="userName"><span><input type='text'  name='j_username' id='j_username' /></span></div>
				<div class="password"><span><input type='password' name='j_password' id='j_password' /></span></div>
			</g:else>
			<div class="signin-btn"><input type='submit' value='Sign In' id="sign-in-btn" height="32px"/></div>
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
})();
</script>

</body>
</html>

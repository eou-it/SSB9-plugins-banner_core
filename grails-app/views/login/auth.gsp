<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>Login</title>
<link rel="stylesheet" href="${resource(dir: 'css', file: 'login.css')}"/>
<!--[if IE 7]>
	<link href="fix-ie7.css" rel="stylesheet" type="text/css" />
<![endif]-->
</head>

<body class="pageBg">

<div class="splashBg">
	<div class="appName">Banner 9.0</div>
    <g:if test='${flash.message}'>
        <div class='loginMsg'>${flash.message}</div>
    </g:if>
    <g:else test='${flash.message}'>
	  <div class="loginMsg">Please login</div>
    </g:else>
	<div class="logIn">
      <form action='${postUrl}' method='POST' id='loginForm'>
		<div class="userName"><span><input type='text'  name='j_username' id='j_username' /></span></div>
		<div class="password"><span><input type='password' name='j_password' id='j_password' /></span></div>
		<div><input type='submit' value='Login' /></div>
      </form>
	</div>
	<div class="copyright">
		<p>&copy; 2000 - 2011 SunGard. All rights reserved.</p>
		<p>This software contains confidential and proprietary information of SunGard and its subsidiaries. Use of this software is limited to SunGard Higher Education licensees, and is subject to the terms and conditions of one or more written license agreements between SunGard Higher Education and the licensee in question.</p>
	</div>
</div>

<script type='text/javascript'>
(function(){
	document.forms['loginForm'].elements['j_username'].focus();
})();
</script>

</body>
</html>

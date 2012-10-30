<!--
/*******************************************************************************
Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
*******************************************************************************/
-->
<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <script type="text/javascript" src="../js/jquery/jquery-1.4.1.min.js"></script>
    </head>
    <body>
        <script type="text/javascript">
            $( document ).ready( function() {
                var browserInstanceCookieValue = getCookie('${cookieName}');
                if (browserInstanceCookieValue == null) { //first request so create cookie
                    initializeBrowserInstance ('${cookieName}', '${cookieName}');
                    $("#dummyForm").submit();
                    return;
                } else if ((window.name).indexOf(browserInstanceCookieValue) == -1) {
                    closeBrowserInstance ();
                    return;
                } else {
                    $("#dummyForm").submit();
                    return;
                }
            } );

            function closeBrowserInstance() {
                var prompt = '${message( code: "net.hedtech.banner.browserInstanceIdentifier.multiWindow.error")}';
                alert(prompt);
                closeWindow ();
            };

            function initializeBrowserInstance(cookieName, cookieValue) {
                setCookie(cookieName, cookieValue);
                window.name = window.name + cookieValue;
            };

            function closeWindow () {
                if ( $.browser.mozilla  ) {
                    window.top.opener=null;
                    window.close();
                } else if ( $.browser.msie  ) {
                    window.open('', '_self', '');
                    window.close();
                } else if ( $.browser.safari  ) {
                    window.open('', '_self', '');
                    window.close();
                } else {
                    window.open('', '_self', '');
                    window.close();
                }
            };

            //TODO once utils.js is available in banner-core, both the cookie
            //methods could be commented out.
            function getCookie(name){
                var cookies = document.cookie.split(';');
                for (var i = 0; i < cookies.length; i++) {
                    var cookie = cookies[i];
                    var eqPos = cookie.indexOf("=");
                    if (eqPos > -1) {
                        var data = cookie.split("=");
                        if (name == $.trim(data[0])) {
                            return data[1];
                        }
                    }
                }
                return null;
            }

            function setCookie(name, value){
                document.cookie = name + "=" + value + "; path="+'${appContextName}';
            }

        </script>
        <form action="${urlToRedirect}" method='POST' id='dummyForm'>
            <g:hiddenField name="instanceVerified" value="true" />
        </form>
    </body>
</html>

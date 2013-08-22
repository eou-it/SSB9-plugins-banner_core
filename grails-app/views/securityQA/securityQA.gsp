<%@ page contentType="text/html;charset=UTF-8" %>
<%--
/*******************************************************************************
Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
*******************************************************************************/
--%>
<html>
<head>
    <title>Security Question and Answer</title>
    <meta name="layout" content="bannerSelfServicePage"/>
    <r:require modules="securityQA"/>
</head>

<body>
<div id="content">
    <div id="bodyContainer" class="ui-layout-center inner-center">
        <div id="pageheader" class="level4">
            <div id="pagetitle"><g:message code="survey.title" /></div>
        </div>
        <div id="pagebody" class="level4">
            <div id="contentHolder">
                <div id="contentBelt"></div>
                <div class="pagebodydiv" style="display: block;">
                    <div id="errorMessage">

                    </div>
                    <form action='${createLink(controller: "securityQA", action: "save")}' id='securityForm' method='POST'>
                        <div class="button-area">
                            <input type='button' value="Cancel" id="security-cancel-btn" class="secondary-button" data-endpoint="${createLink(controller: "logout")}"/>
                            <input type='button' value="Continue" id="security-save-btn" class="primary-button"/>
                        </div>
                    </form>
                </div>
            </div>
        </div>
   </div>
</div>
</body>
</html>
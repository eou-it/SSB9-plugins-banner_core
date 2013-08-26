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
<script>
    var prev = ""
    function updateSelection(elm) {
        var selectElements = $('select#question');
        if (elm.value != "null") {
            for (var i = 0; i < selectElements.length; i++) {
                if (prev != elm.value) {
                    $($('select#question')[i]).find('option[value="' + prev + '"]').show();
                }
                if (!($('select#question')[i] == elm)) {
                    $($('select#question')[i]).find('option[value="' + elm.value + '"]').hide();
                }
            }
        }
        else {
            for (var i = 0; i < selectElements.length; i++) {
                $($('select#question')[i]).find('option[value="' + prev + '"]').show();
            }
        }
    }

    function handleClick(elm) {
        prev = $(elm).find(":selected").val();
    }

</script>

<div id="content">
    <div id="bodyContainer" class="ui-layout-center inner-center">
        <div id="pageheader" class="level4">
            <div id="pagetitle"><g:message code="securityQA.title"/></div>
        </div>

        <div id="pagebody" class="level4">
            <div id="contentHolder">
                <div id="contentBelt"></div>

                <div class="pagebodydiv" style="display: block;">
                    <div id="errorMessage">

                    </div>

                    <div>
                        <label><g:message code="securityQA.information"/></label>
                    </div>
                    <br/>
                    <br/>
                    <form action='${createLink(controller: "securityQA", action: "save")}' id='securityForm'
                          method='POST'>
                        <div id="wrapper">
                            <div id="confirmpinlabel">
                                <label><g:message code="securityQA.confirmpin.label"/></label>
                            </div>
                            <div id="textLabel">
                                <g:field name="pin" type="password"></g:field>
                            </div>
                        </div>
                    </br>
                        <g:each in="${1..noOfquestions}" status="i" var="ques">
                            <div id="wrapper">
                                <div id="questionLabel"><label><g:message code="securityQA.question.label"
                                                                          args="[i + 1]"/></label></div>
                                <div>
                                    <g:select name="question" from="${questions}"
                                              noSelection="${['null': 'Not Selected']}"
                                              onchange="updateSelection(this)" onclick="handleClick(this)"/>
                                </div>
                                <g:if test="${userDefinedQuesFlag == 'Y'}">
                                    <div id="label"><label><g:message code="securityQA.or.label"/></label></div>

                                    <div id="wrapper">
                                        <div id="userquestionLabel"><label><g:message
                                                code="securityQA.userdefinedquestion.label"
                                                args="[i + 1]"/></label></div>

                                        <div><g:textField name="userDefinedQuestion"></g:textField></div>
                                    </div>
                                </g:if>
                            </div>
                            <br/>

                            <div id="wrapper">
                                <div id="questionLabel"><label><g:message code="securityQA.answer.label"
                                                                          args="[i + 1]"/></label></div>
                                <g:textField name="answer"></g:textField>
                            </div>
                            <br/>
                        </g:each>
                        <div class="button-area">
                            <input type='button' value="Cancel" id="security-cancel-btn" class="secondary-button"
                                   x data-endpoint="${createLink(controller: "logout")}"/>
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

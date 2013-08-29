$(document).ready(function () {

    EventDispatcher.addEventListener(Application.events.initialized, function () {
        if (window.securityQAInitErrors && window.securityQAInitErrors.notification && window.securityQAInitErrors.notification.length > 0) {

            var n = new Notification({message:window.securityQAInitErrors.notification, type:"error"});
            notifications.addNotification(n);
        }
    });
    var notificationMessages = new Array();

    $('select#question').each(function (j, ielm) {
        $($(ielm).find('option[value="' + selectedQues[j] + '"]')).selected();
    });

    $("#security-save-btn").click(function () {
        notificationMessages = [];
        validateForm();
        if (notificationMessages && notificationMessages.length > 0) {
            _.each(notificationMessages, function (message) {
                var n = new Notification({message:message, type:"error"});

                /* n.addPromptAction($.i18n.prop("js.notification.ok"), function() {
                 notifications.remove(n);
                 });*/

                notifications.addNotification(n);
            });
        }
        else {
            var form = document.getElementById('securityForm');
            form.submit();
        }
    });

    function validateForm() {
        notifications.clearNotifications()
        validatePin();

        $('input#userDefinedQuestion').each(function (j, selectElm) {
            var enteredText = $(selectElm).val();
            var invalidcharacter = $.i18n.prop("securityQA.invalid.question");
            var invalidqusetionlength = $.i18n.prop("securityQA.invalid.length.question", [questionMinimumLength]);
            if ((enteredText.length > 0) && (enteredText.match('<') || enteredText.match('>'))) {
                $(selectElm).parent().addClass("notification-error");
                notificationMessages.push(invalidcharacter);
            }
            if (enteredText.length > 0 && enteredText.length < questionMinimumLength) {
                $(selectElm).parent().addClass("notification-error");
                notificationMessages.push(invalidqusetionlength);
            }
        });

        $('select#question').find('option:selected').each(function (j, ielm) {
            $(ielm).closest("div .section-wrapper").removeClass("notification-error");
            var index = parseInt($(ielm).val().substring("question".length));
            var userDefinedQuestion = $('input#userDefinedQuestion')[j].value;
            if (index != 0 && userDefinedQuestion.length > 0) {
                var error = $.i18n.prop("securityQA.invalid.number.question");
                $(ielm).closest("div .section-wrapper").addClass("notification-error");
                notificationMessages.push(error);
            }
            else if (index == 0 && userDefinedQuestion.length == 0) {
                var error = $.i18n.prop("securityQA.error");
                $(ielm).closest("div .section-wrapper").addClass("notification-error");
                notificationMessages.push(error);
            }
        });

        $('input#answer').each(function (j, ielm) {
            $(ielm).parent().removeClass("notification-error");

            var enteredText = $(ielm).val();
            if (enteredText.length == 0) {
                var error = $.i18n.prop("securityQA.error");
                notificationMessages.push(error);
                $(ielm).parent().addClass("notification-error");
            }

            if ((enteredText.length > 0) && (enteredText.match('<') || enteredText.match('>'))) {
                var invalidcharacter = $.i18n.prop("securityQA.invalid.answer");
                $(ielm).parent().addClass("notification-error");
                notificationMessages.push(invalidcharacter);
            }
            if (enteredText.length > 0 && enteredText.length < answerMinimumLength) {
                var invalidanswerlength = $.i18n.prop("securityQA.invalid.length.answer", [answerMinimumLength]);
                $(ielm).parent().addClass("notification-error");
                notificationMessages.push(invalidanswerlength);
            }
        });
    }


    $("#security-cancel-btn").click(function () {
        var href = $(this).attr("data-endpoint")
        window.location = href;
    });


    $('select#question').live('change', function () {
        updateSelects();
    });

    function updateSelects() {
        $('select#question').each(
            function (j, elem) {
                var $selected = $(elem).find("option:selected");
                var $opts = $("<div>");

                var index = parseInt($($selected).val().substring("question".length));
                if (index != 0) {
                    $($selected).parent().removeClass("notification-error");
                }

                var newArray = new Array();
                for (var i = 0; i < questions.length; i++) {
                    newArray.push(questions[i]);
                }

                $('select#question').find('option:selected').each(function (j, ielm) {
                    var index = parseInt($(ielm).val().substring("question".length));
                    if (elem != $(ielm).parent()[0]) {
                        newArray[index] = "";
                    }

                });

                $opts.append('<option value=question0>' + questions[0] + '</option>');
                for (var i = 1; i < newArray.length; i++) {
                    if (newArray[i] != "") {
                        $opts.append('<option value=question' + i + '>' + questions[i] + '</option>');
                    }
                }

                $(elem).html($opts.html());
                if ($selected.length) {
                    $(elem).val($selected.val());
                }
            });
    }


    function validatePin() {
        var error = $.i18n.prop("securityQA.invaild.pin");
        var blankPinAnswerNotification = new Notification({message:error, type:"error"});
        if ($('input#pin').val().length == 0) {
            notificationMessages.push(error);
            $('input#pin').parent().addClass("notification-error");
        } else {
            notificationMessages.splice(notificationMessages.indexOf(error));
            $('input#pin').parent().removeClass("notification-error");
        }
    }
})

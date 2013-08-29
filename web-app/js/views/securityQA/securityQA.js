$(document).ready(function () {
    EventDispatcher.addEventListener(Application.events.initialized, function() {
        if (window.securityQAInitErrors && window.securityQAInitErrors.notification && window.securityQAInitErrors.notification.length > 0) {

                var n = new Notification({message: window.securityQAInitErrors.notification, type:"error"});
                notifications.addNotification(n);
        }
    });

    $("#security-save-btn").click(function () {
        var validForm = true;
        var notificationMsgs = new Array();

        notificationMsgs = validateForm();

        if(notificationMsgs && notificationMsgs.length > 0) {
            _.each(notificationMsgs, function(message) {
                var n = new Notification({message: message.message, type:message.type, flash: true});

                notifications.addNotification(n);
            });
        }
        else {
            var form = document.getElementById('securityForm');
            form.submit();
        }
    });

    function validateForm() {
        var notificationMsgs = new Array();
        $('select#question').each(function(j, selectElm){
            $(selectElm).removeClass("notification-error");
        });

        $('select#question').find('option:selected').each(function(j, ielm) {
            var index = parseInt($(ielm).val().substring("question".length));
            if(index == 0) {
                notificationMsgs.push({message: "Fill Form", type: "error"});
                $(ielm).parent().addClass("notification-error");
            }
        });

        return notificationMsgs;
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
                if(index != 0) {
                    $($selected).parent().removeClass("notification-error");
                }

                var newArray = new Array();
                for (var i = 0; i < questions.length; i++) {
                    newArray.push(questions[i]);
                }

                $('select#question').find('option:selected').each(function(j, ielm) {
                    var index = parseInt($(ielm).val().substring("question".length));
                    if(elem != $(ielm).parent()[0]) {
                        newArray[index] = "";
                    }

                });

                $opts.append('<option value=question0>' + questions[0] + '</option>');
                for (var i = 1; i < newArray.length; i++) {
                    if(newArray[i] != "") {
                        $opts.append('<option value=question' + i + '>' + questions[i] + '</option>');
                    }
                }

                $(elem).html($opts.html());
                if ($selected.length) {
                    $(elem).val($selected.val());
                }
            });
    }
})

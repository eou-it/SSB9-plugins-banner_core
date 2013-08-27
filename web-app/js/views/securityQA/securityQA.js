$(document).ready(function () {
    $("#security-save-btn").click(function () {
        var validForm = true;
        var notificationMsgs = new Array();

        notificationMsgs = validateForm();

        if(notificationMsgs && notificationMsgs.length > 0) {
            _.each(notificationMsgs, function(message) {
                var n = new Notification({message: message.message, type:message.type, flash: true});

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
        var notificationMsgs = new Array();

        $('select#question').find('option:selected').each(function(j, ielm) {
            var index = parseInt($(ielm).val().substring("question".length));
            if(index == 0) {
                notificationMsgs.push({message: "Fill Form", type: "error"});
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

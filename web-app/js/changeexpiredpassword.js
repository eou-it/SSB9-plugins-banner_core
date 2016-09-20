/*********************************************************************************
 Copyright 2012-2016 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/

$(document).ready(function () {

    $("input").blur(function (e) {
        var element = $(e.currentTarget);
        validateForm(element);
    });

    $("#changePasswordButton").click(function (e) {

        $("#changePasswordForm").find('input[type=password]').each(function () {
            var element = $(this);
            if (!validateForm(element))
                e.preventDefault();
        });
    });

    function validateForm(element) {
        var validatedForm = true;
        var emptyErrorMessage = $.i18n.prop("changeExpiredPassword.password.required.error");
        var passwordMatchError = $.i18n.prop("changeExpiredPassword.password.match.error");
        if (element.val().trim() != "" && element.hasClass("error-state")) {
            element.removeClass("error-state");
            element.addClass("default-state");
            element.parent().prev().removeClass("invalid");
            while (notifications.length != 0) {
                notifications.remove(notifications.first());
            }
        }
        else if (element.val().trim() == "") {
            element.addClass("error-state");
            element.removeClass("default-state");
            element.parent().prev().addClass("invalid");
            while (notifications.length != 0) {
                notifications.remove(notifications.first())
            }
            var errorNotification = new Notification({
                message: emptyErrorMessage,
                type: "error",
                component: element,
                elementToFocus: element
            });
            notifications.addNotification(errorNotification);
            validatedForm = false;
        }
        if ($("#password").val().trim().length != 0 && $("#repassword").val().trim().length) {
            notifications.remove(notifications.get("password"));
            notifications.remove(notifications.get("repassword"));
            if ($("#password").val() != $("#repassword").val()) {
                var errorNotification = new Notification({
                    message: passwordMatchError,
                    type: "error",
                    id: "match",
                    component: $('#repassword'),
                    elementToFocus: $('#repassword')
                });
                notifications.addNotification(errorNotification);
                validatedForm = false;
            }
            else {
                notifications.remove(notifications.get("match"));
            }
        }
        return validatedForm;
    }

    $("input").focus(function (e) {
        var element = $(e.currentTarget);
        if (element.parent().prev().hasClass("invalid")) {
            element.parent().prev().removeClass("invalid");
        }
    });

    $("#cancelChangePasswordButton").click(function () {
        var form = document.getElementById('changePasswordForm');
        form.action = cancelUrl;
        form.submit();
    });

    if (flashMessage != "") {
        $("#changePasswordForm").find('input[type=password]').each(function () {
            var element = $(this);
            element.addClass("error-state");
            element.removeClass("default-state");
            element.parent().prev().addClass("invalid");
        });
    }

    setTimeout(function () {
        $(".error-state").each(function (i, element) {

            var errorMessageList = flashMessage.split("::::");
            for (var i = 0; i < errorMessageList.length; i++) {
                while (notifications.length != 0) {
                    notifications.remove(notifications.first())
                }
                var error = errorMessageList[i].replace(/:/g, "");
                var errorNotification = new Notification({
                    message: error,
                    type: "error",
                    component: $("#oldpassword"),
                    id: $(element).attr("id"),
                    elementToFocus: $("#oldpassword")
                });
                notifications.addNotification(errorNotification);
            }
        })
    }, 500);

    setTimeout(function () {
        $('input:password').attr('value', '');
    }, 100);
});

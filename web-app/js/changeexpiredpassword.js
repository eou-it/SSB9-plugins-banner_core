/*********************************************************************************
 Copyright 2012-2016 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/

$(document).ready(function () {

    $("input").blur(function (e) {
        var emptyErrorMessage =$.i18n.prop("net.hedtech.banner.changepassword.password.required.error");
        var passwordMatchError=$.i18n.prop("net.hedtech.banner.resetpassword.password.match.error");
        var element = $(e.currentTarget);
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
                component: $(this),
                elementToFocus: $(this)
            });
            notifications.addNotification(errorNotification);
        }
        if ($("#password").val().trim().length != 0 && $("#repassword").val().trim().length) {
            notifications.remove(notifications.get("password"));
            notifications.remove(notifications.get("repassword"));
            if ($("#password").val() != $("#repassword").val()) {
                var errorNotification = new Notification({message: passwordMatchError, type: "error", id: "match",component: $('#repassword'),elementToFocus: $('#repassword')});
                notifications.addNotification(errorNotification);
            }
            else {
                notifications.remove(notifications.get("match"));
            }
        }

    });
    $("input").focus(function (e) {
        var element = $(e.currentTarget);
        if (element.parent().prev().hasClass("invalid")) {
            element.parent().prev().removeClass("invalid");
        }
    });

    setTimeout(function () {
        $('input:password').attr('value', '');
    }, 100);
});

$(document).ready(function () {
    $("#security-save-btn").click(function () {
            var form = document.getElementById('securityForm')
            form.submit()
    });

    $("#security-cancel-btn").click(function () {
        var href = $(this).attr("data-endpoint")
        window.location = href
    });

})

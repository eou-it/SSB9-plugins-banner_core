$(document).ready(function () {
    $("#policy-continue").click(function () {
        var href = $(this).attr("data-endpoint")
        window.location = href
    });
    $("#policy-exit").click(function () {
        var href = $(this).attr("data-endpoint")
        window.location = href
    });

})

$(document).ready(function () {
    $("#security-save-btn").click(function () {
        var form = document.getElementById('securityForm')
        form.submit()
    });

    $("#security-cancel-btn").click(function () {
        var href = $(this).attr("data-endpoint")
        window.location = href
    });


    $('select#question').live('change', function () {
        updateSelects();
    });

    function updateSelects() {
        $('select#question').each(
            function (j, elem) {
                var $selected = $(elem).find("option:selected");
                var $opts = $("<div>")

                var newArray = new Array();
                for (var i = 0; i < questions.length; i++) {
                    newArray.push(questions[i])
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

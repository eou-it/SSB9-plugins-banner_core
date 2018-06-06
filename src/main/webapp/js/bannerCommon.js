/*********************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/

var path = document.querySelector('meta[name=menuBaseURL]').content || document.location.href;
var appUrl = path.substring(0, path.indexOf('/ssb'));
document.getElementById("branding").setAttribute('href', appUrl);
// define a handler
function goToHome(e) {
    // this would go to Home page of Application=> key is Home and the Shift key at the same time
    if (e.ctrlKey && e.altKey && e.keyCode == 36) {
       document.getElementById("branding").click();
    }
}
// register the handler
document.addEventListener('keyup', goToHome, false);



/*********************************************************************************
 Copyright 2018-2021 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/

var path = document.querySelector('meta[name=menuBaseURL]').content || document.location.href;
var appUrl = path.substring(0, path.indexOf('/ssb'));
appUrl=appUrl+"/";
document.getElementById("branding").setAttribute('href', appUrl);
// define a handler
function goToHome(e) {
    // this would go to Home page of Application=> key is Home and the Shift key at the same time
    if (e.altKey && e.keyCode == 81) {
       document.getElementById("branding").click();
    }
}
// register the handler
document.addEventListener('keyup', goToHome, false);
var element=document.getElementsByClassName('institutionalBranding');
var bgImage= window.getComputedStyle(element[0], null).getPropertyValue("background-image");
var brandinglogo='';
if(bgImage.indexOf('url')>=0) {
    brandinglogo=bgImage.replace('url(','').replace(')','').replace(/\"/gi, "");
    element[0].style.background="none";
}
else{
    brandinglogo=appUrl + "assets/eds/logo.svg";
}
document.getElementById('brandingImage').src=brandinglogo;


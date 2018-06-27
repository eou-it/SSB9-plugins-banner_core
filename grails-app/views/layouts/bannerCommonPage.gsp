<!--
/*******************************************************************************
Copyright 2017-2018 Ellucian Company L.P. and its affiliates.
*******************************************************************************/
-->

<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html lang="${message(code: 'default.language.locale')}">
<head>
    <script>
        window.mepCode='${session.mep}';
    </script>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="menuBaseURL" content="${request.contextPath}/ssb"/>
    <meta charset="${message(code: 'default.character.encoding')}"/>
    <title><g:layoutTitle default="Banner"/></title>
    <g:if test="${message(code: 'default.language.direction')  == 'rtl'}">
        <asset:javascript src="modules/bannerCommon-mf.js"/>
        <asset:stylesheet src="modules/bannerCommonRTL-mf.css"/>
    </g:if>
    <g:else>
        <asset:javascript src="modules/bannerCommon-mf.js"/>
        <asset:stylesheet src="modules/bannerCommonLTR-mf.css"/>
    </g:else>


    <g:layoutHead/>
    <g:customStylesheetIncludes/>

    <g:set var="themeConfig" value="${grails.util.Holders.config.banner.theme}"/>
    <g:if test="${themeConfig.url}">
        <g:if test="${session.mep}">
            <link rel="stylesheet" type="text/css" href="${themeConfig.url}/getTheme?name=${themeConfig.name + session.mep}&template=${themeConfig.template}&mepCode=${session.mep}">
        </g:if>
        <g:else>
            <link rel="stylesheet" type="text/css" href="${themeConfig.url}/getTheme?name=${themeConfig.name}&template=${themeConfig.template}">
        </g:else>
    </g:if>
        <asset:link rel="apple-touch-icon" sizes="57x57" href="eds/apple-touch-icon-57x57.png"/>
        <asset:link rel="apple-touch-icon" sizes="60x60" href="eds/apple-touch-icon-60x60.png"/>
        <asset:link rel="apple-touch-icon" sizes="72x72" href="eds/apple-touch-icon-72x72.png"/>
        <asset:link rel="apple-touch-icon" sizes="76x76" href="eds/apple-touch-icon-76x76.png"/>
        <asset:link rel="apple-touch-icon" sizes="114x114" href="eds/apple-touch-icon-114x114.png"/>
        <asset:link rel="apple-touch-icon" sizes="120x120" href="eds/apple-touch-icon-120x120.png"/>
        <asset:link rel="apple-touch-icon" sizes="144x144" href="eds/apple-touch-icon-144x144.png"/>
        <asset:link rel="apple-touch-icon" sizes="152x152" href="eds/apple-touch-icon-152x152.png"/>
        <asset:link rel="apple-touch-icon" sizes="180x180" href="eds/apple-touch-icon-180x180.png"/>
        <asset:link rel="shortcut icon" type="image/png" href="eds/favicon-32x32.png" sizes="32x32"/>
        <asset:link rel="shortcut icon" type="image/png" href="eds/android-chrome-192x192.png" sizes="192x192"/>
        <asset:link rel="shortcut icon" type="image/png" href="eds/favicon-96x96.png" sizes="96x96"/>
        <asset:link rel="shortcut icon" type="image/png" href="eds/favicon-16x16.png" sizes="16x16"/>
        <asset:link rel="shortcut icon" href="eds/favicon.ico" type="image/x-icon"/>

</head>

<body>

<g:analytics/>

    <header id='banner-header-main-section' class='banner-header-theme' role='banner'>
        <div id='banner-header-main-section-west-part'>
            <div id='brandingDiv' title="${message(code: 'aurora.areas_label_home_title')}" tabindex='-1'>
                <a id='branding' aria-label="${message(code: 'aurora.areas_label_home_description')}"
                   alt="${message(code: 'aurora.areas_label_branding')}" href='javascript:void(0);'
                    class='institutionalBranding'></a>
            </div>
        </div>
    </header>

    <g:layoutBody/>
    <g:customJavaScriptIncludes/>
    <asset:deferredScripts/>
</body>
</html>

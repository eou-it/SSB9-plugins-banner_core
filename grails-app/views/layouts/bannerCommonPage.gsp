<!--
/*******************************************************************************
Copyright 2017 Ellucian Company L.P. and its affiliates.
*******************************************************************************/
-->

<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html lang="${message(code: 'default.language.locale')}">
<head>
    <g:if test="${message(code: 'default.language.direction') == 'rtl'}">
        <r:require modules="bannerCommonRTL"/>
    </g:if>
    <g:else>
        <r:require modules="bannerCommonLTR"/>
    </g:else>

    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="menuBaseURL" content="${request.contextPath}/ssb"/>
    <meta charset="${message(code: 'default.character.encoding')}"/>
    <title><g:layoutTitle default="Banner"/></title>
    <link rel="shortcut icon" href="${resource(plugin: 'banner-core', dir:'images',file:'favicon.ico')}" type="image/x-icon"/>
    <r:layoutResources/>
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
    <r:layoutResources/>
    <g:customJavaScriptIncludes/>
</body>
</html>

<!--
/*******************************************************************************
Copyright 2018 Ellucian Company L.P. and its affiliates.
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
    <r:layoutResources/>
    <g:layoutHead/>
    <g:customStylesheetIncludes/>

    <g:set var="themeConfig" value="${grails.util.Holders.config.banner.theme}"/>
    <g:if test="${themeConfig.url}">
        <g:if test="${session.mep}">
            <link rel="stylesheet" type="text/css" href="${themeConfig.url}/getTheme?name=${themeConfig.name + session.mep}&template=${themeConfig.template}&mepCode=${session.mep}">
        </g:if>
        <g:elseif test="${mep}">
            <link rel="stylesheet" type="text/css" href="${themeConfig.url}/getTheme?name=${themeConfig.name + mep}&template=${themeConfig.template}&mepCode=${mep}">
        </g:elseif>
        <g:else>
            <link rel="stylesheet" type="text/css" href="${themeConfig.url}/getTheme?name=${themeConfig.name}&template=${themeConfig.template}">
        </g:else>
    </g:if>

    <link rel="apple-touch-icon" sizes="57x57" href="${resource(plugin: 'bannerCore', dir:'images/eds/',file:'apple-touch-icon-57x57.png')}"/>
    <link rel="apple-touch-icon" sizes="60x60" href="${resource(plugin: 'bannerCore', dir:'images/eds/',file:'apple-touch-icon-60x60.png')}"/>
    <link rel="apple-touch-icon" sizes="72x72" href="${resource(plugin: 'bannerCore', dir:'images/eds/',file:'apple-touch-icon-72x72.png')}"/>
    <link rel="apple-touch-icon" sizes="76x76" href="${resource(plugin: 'bannerCore', dir:'images/eds/',file:'apple-touch-icon-76x76.png')}"/>
    <link rel="apple-touch-icon" sizes="114x114" href="${resource(plugin: 'bannerCore', dir:'images/eds/',file:'apple-touch-icon-114x114.png')}"/>
    <link rel="apple-touch-icon" sizes="120x120" href="${resource(plugin: 'bannerCore', dir:'images/eds/',file:'apple-touch-icon-120x120.png')}"/>
    <link rel="apple-touch-icon" sizes="144x144" href="${resource(plugin: 'bannerCore', dir:'images/eds/',file:'apple-touch-icon-144x144.png')}"/>
    <link rel="apple-touch-icon" sizes="152x152" href="${resource(plugin: 'bannerCore', dir:'images/eds/',file:'apple-touch-icon-152x152.png')}"/>
    <link rel="apple-touch-icon" sizes="180x180" href="${resource(plugin: 'bannerCore', dir:'images/eds/',file:'apple-touch-icon-180x180.png')}"/>
    <link rel="shortcut icon" type="image/png" href="${resource(plugin: 'bannerCore', dir:'images/eds/',file:'favicon-32x32.png')}" sizes="32x32"/>
    <link rel="shortcut icon" type="image/png" href="${resource(plugin: 'bannerCore', dir:'images/eds/',file:'android-chrome-192x192.png')}" sizes="192x192"/>
    <link rel="shortcut icon" type="image/png" href="${resource(plugin: 'bannerCore', dir:'images/eds/',file:'favicon-96x96.png')}" sizes="96x96"/>
    <link rel="shortcut icon" type="image/png" href="${resource(plugin: 'bannerCore', dir:'images/eds/',file:'favicon-16x16.png')}" sizes="16x16"/>
    <link rel="shortcut icon" href="${resource(plugin: 'bannerCore', dir:'images/eds/',file:'favicon.ico')}" type="image/x-icon" />

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

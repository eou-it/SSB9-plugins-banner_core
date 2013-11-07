/* ****************************************************************************
Copyright 2013 Ellucian Company L.P. and its affiliates.
*******************************************************************************/
package net.hedtech.banner.security

import org.apache.log4j.Logger


class UrlBasedFormsIdentifier {

    private static final Logger log = Logger.getLogger( "net.hedtech.banner.security.UrlBasedFormsIdentifier" )

    public static List getTokensFor( String requestUrl ) {
        log.debug "getTokensFor will extract tokens for url $requestUrl"
        def queryParamIndex = requestUrl.indexOf('?')
        def url = (queryParamIndex > -1) ? requestUrl.substring(0, queryParamIndex) : requestUrl
        List tokens = url.toLowerCase().tokenize("/")
        log.debug "getTokensFor will return ${tokens.join(', ')}"
        tokens
    }


    public static int indexOfApiPrefix( List<String> urlParts, List<String> apiPrefixes ) {
        def index = urlParts.findIndexOf() { it in  apiPrefixes }
        index
    }


    public static List getFormsFor( String url,
                                    List<String> apiPrefixes,
                                    Map formControllerMap,
                                    String pageName = "mainpage" ) {

        if (url.toLowerCase().contains("banner.zul")) {
            log.debug "getFormsFor will identify form(s) for 'banner.zul' pageName=$pageName"
            def forms =  new ArrayList( formControllerMap[pageName])
            log.debug "getFormsFor will return forms: ${forms.join(", ")}"
            return forms
        }

        def urlParts = getTokensFor( url )
        log.debug "getFormsFor tokenized $url into ${urlParts.join(', ')}"
        def index = indexOfApiPrefix( urlParts, apiPrefixes )
        log.debug "getFormsFor identified API prefix at index $index"

        if (index > -1) { // we found an API prefix if the index is > -1
            log.debug "getFormsFor will identify form(s) for 'API' request: $url"
            log.debug "getFormsFor found forms ${formControllerMap[ urlParts[index + 1] ]}"
            def forms = new ArrayList( formControllerMap[ urlParts[index + 1] ] ?: [] )
            log.debug "getFormsFor will return forms: ${forms.join(", ")}"
            return forms
        }

        log.debug "getFormsFor will identify forms for a URL that is neither a ZK nor an API request"
        def forms = new ArrayList( formControllerMap[ pageName ] )
        log.debug "getFormsFor will return forms: ${forms.join(", ")}"
        return forms
    }

}


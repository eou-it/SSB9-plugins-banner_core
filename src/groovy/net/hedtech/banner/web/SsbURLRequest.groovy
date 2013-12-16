/*******************************************************************************
 Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */

package net.hedtech.banner.web

class SsbURLRequest {
    private static final String SSB_BASE_URL = "ssb"
    private static final String SLASH = "/"
    private static final int RELATIVE_SLASH_INDEX = 1

    public String getControllerNameFromPath(String url) {
        if (url != null && url.contains(SSB_BASE_URL)) {
            int ssbBaseUrlIndex = url.indexOf(SSB_BASE_URL)
            int ssbBaseUrlLength = SSB_BASE_URL.length()
            String controllerName = url.substring(ssbBaseUrlIndex + ssbBaseUrlLength + RELATIVE_SLASH_INDEX);
            if (controllerName.contains(SLASH)) {
                controllerName = controllerName.substring(0, controllerName.indexOf(SLASH))
            }
            return controllerName
        }
        else {
            return url
        }
    }
}

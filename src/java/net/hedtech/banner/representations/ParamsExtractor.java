/** *****************************************************************************
 Copyright 2010 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
package net.hedtech.banner.representations;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;


/**
 * A 'params extractor' is able to extract model properties from a custom representation.
 * This is needed when a custom representation is being used within a request, such that the
 * default 'Grails Converters' are not able to extract the body contents and populate model properties.
 */
public interface ParamsExtractor {

    /**
     * Extracts a map of name-value pairs representing the properties of the supported resource (aka model).
     * @param request the HTTP servlet request object representing the current request
     * @return Map a map of properties that will be added to the Grails 'params' object
     */
    public Map extractParams( HttpServletRequest request );
    
}

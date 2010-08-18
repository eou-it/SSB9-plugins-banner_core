/** *****************************************************************************
 Copyright 2010 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
package com.sungardhe.banner.representations;

import java.util.Map;


/**
 * A resource representation handler is responsible for both 'params extraction' and representation formatting
 * ready for rendering.  
 */
public interface ResourceRepresentationHandler {
    
    public String getRepresentationName();

    public Class getModelClass();

    public ParamsExtractor paramsExtractor();

    public RepresentationBuilder singleBuilder();

    public RepresentationBuilder collectionBuilder();

}

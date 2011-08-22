/** *****************************************************************************

 © 2011 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
package com.sungardhe.banner.service

import com.sungardhe.banner.testing.BaseIntegrationTestCase
import org.springframework.web.context.request.RequestContextHolder



class DefaultLoaderServiceIntegrationTests extends BaseIntegrationTestCase {

    def defaultLoaderService

     protected void setUp() {
        formContext = ['SCACRSE']
        super.setUp()
    }

    void testDefaultDataLoad(){
        defaultLoaderService.loadDefault('grails_user')
        assertNotNull( RequestContextHolder.currentRequestAttributes().request.session.getAttribute("DEFAULTS") )
    }

}
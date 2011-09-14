/*********************************************************************************
 Copyright 2009-2011 SunGard Higher Education. All Rights Reserved.
 This copyrighted software contains confidential and proprietary information of 
 SunGard Higher Education and its subsidiaries. Any use of this software is limited 
 solely to SunGard Higher Education licensees, and is further subject to the terms 
 and conditions of one or more written license agreements between SunGard Higher 
 Education and the licensee in question. SunGard is either a registered trademark or
 trademark of SunGard Data Systems in the U.S.A. and/or other regions and/or countries.
 Banner and Luminis are either registered trademarks or trademarks of SunGard Higher 
 Education in the U.S.A. and/or other regions and/or countries.
 **********************************************************************************/
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
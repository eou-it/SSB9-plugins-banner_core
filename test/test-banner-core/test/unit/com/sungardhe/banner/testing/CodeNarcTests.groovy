/** *****************************************************************************
 © 2010 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */

package com.sungardhe.banner.testing

import grails.test.GrailsUnitTestCase

/**
 * Runs code narc and fails if there is code that it finds that violates our rulesets.
 */
class CodeNarcTests extends GroovyTestCase {


    void testCodeNarcRun() {
        new CodeNarcHelper().runCodeNarc()
    }

}

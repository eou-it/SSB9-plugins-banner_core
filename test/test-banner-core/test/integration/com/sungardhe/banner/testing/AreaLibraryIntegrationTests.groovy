/** *****************************************************************************
 © 2010 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
package com.sungardhe.banner.testing

import com.sungardhe.banner.exceptions.ApplicationException
import com.sungardhe.banner.testing.BaseIntegrationTestCase

/**
 * Used to test reporting of an ORA-01465 exception.
 **/
class AreaLibraryIntegrationTests extends BaseIntegrationTestCase {


	protected void setUp() {
		formContext = ['SSAPREQ', 'STVLEVL'] // Since we are not testing a controller, we need to explicitly set this
		super.setUp()
	}


    // An underlying SQLException ORA-01465: invalid hex number is expected, however requires
    // additional support (as it is buried within hibernate and spring)
    void testExceptionCapture() {
        def entity = newAreaLibrary()
        assertTrue "Entity did not validate", entity.validate()
        try {
            entity.save()
            fail "AreaLibrary should not have been successfully saved, due to and expected 'invalid hex number' error"
        } catch (e) {
            def ae = new ApplicationException( AreaLibrary, e )
            assertTrue ('SQLException' == ae.type || 'UncategorizedSQLException' == ae.type)
            assertEquals 1465, ae.sqlExceptionErrorCode
            
            def localizer = new FooController().localizer // just using this as a quick way to get a localizer closure
            def returnMap = ae.returnMap( localizer )
            def msg = returnMap.underlyingErrorMessage
            assertTrue "Underlying error message not as expected but was ${returnMap.underlyingErrorMessage}", 
                        returnMap.underlyingErrorMessage.contains( "ORA-01465: invalid hex number" )
            assertTrue "Message not as expected but was ${returnMap.message}", 
                        returnMap.message.contains( "The following error(s) have occurred" )
        }
    }


    def newAreaLibrary() {
        new AreaLibrary( prescrUsageIndicator: 'Y', area: "oldArea",  areaDescription: "description of oldArea",
                         dynamicIndicator: true, printIndicator: 'N', complUsageIndicator: true, prerequisiteUsageIndicator: true,
                         lastModified: new Date(), lastModifiedBy: "test", dataOrigin: "Banner" )
    }

}
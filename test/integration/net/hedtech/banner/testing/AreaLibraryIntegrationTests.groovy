/*******************************************************************************
Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
*******************************************************************************/
package net.hedtech.banner.testing

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.Before
import org.junit.After

import org.junit.Test

/**
 * Used to test reporting of an ORA-01465 exception.N
 **/
class AreaLibraryIntegrationTests extends BaseIntegrationTestCase {


	@Before
    public void setUp() {
		formContext = ['GUAGMNU'] // Since we are not testing a controller, we need to explicitly set this
		super.setUp()
	}

	@After
    public void tearDown() {
        super.tearDown()
    }


    // An underlying SQLException ORA-01465: invalid hex number is expected, however requires
    // additional support (as it is buried within hibernate and spring)
    @Test
    void testExceptionCapture() {
        def entity = newAreaLibrary()
        assertTrue "Entity did not validate", entity.validate()
        assertNotNull(entity.toString())
        try {
            entity.save(flush: true)
            fail "AreaLibrary should not have been successfully saved, due to and expected 'invalid hex number' error"
        } catch (e) {
            def ae = new ApplicationException( AreaLibraryForTesting, e )
            assertTrue ('SQLException' == ae.type || 'UncategorizedSQLException' == ae.type)
            assertEquals 1465, ae.sqlExceptionErrorCode

            def localizer = new TermController().localizer // just using this as a quick way to get a localizer closure
            def returnMap = ae.returnMap( localizer )
            def msg = returnMap.underlyingErrorMessage
            assertTrue "Underlying error message not as expected but was ${returnMap.underlyingErrorMessage}",
                        returnMap.underlyingErrorMessage.contains( "ORA-01465: invalid hex number" )
            assertTrue "Message not as expected but was ${returnMap.message}",
                        returnMap.message.contains( "The following error(s) have occurred" )
        }
    }


    def newAreaLibrary() {
        new AreaLibraryForTesting( prescrUsageIndicator: 'Y', area: "oldArea",  areaDescription: "description of oldArea",
                         dynamicIndicator: true, printIndicator: 'N', complUsageIndicator: true, prerequisiteUsageIndicator: true,
                         lastModified: new Date(), lastModifiedBy: "test", dataOrigin: "Banner" )
    }

}

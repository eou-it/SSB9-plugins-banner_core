/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/


import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import net.hedtech.banner.exceptions.MepCodeNotFoundException
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

@Integration
@Rollback
class ErrorControllerIntegrationTests extends BaseIntegrationTestCase {

    def controller

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        controller = new ErrorController()
    }

    @After
    public void tearDown() {
        super.tearDown()
    }

    @Test
    void testAccessForbidden() {
        controller.accessForbidden()
        assertEquals(200, controller.response.status)
    }

    @Test
    void testPageNotFoundError() {
        controller.pageNotFoundError()
        assertEquals(200, controller.response.status)
    }

    @Test
    void testInternalServerErrorWithoutException() {
        controller.internalServerError()
        assertEquals(200, controller.response.status)
    }

    @Test
    void testInternalServerError() {
        Exception e = new Exception()
        Throwable cause = new MepCodeNotFoundException()
        e.initCause(cause)
        controller.request.exception = e
        controller.internalServerError()
        assertEquals(200, controller.response.status)
    }
    
}

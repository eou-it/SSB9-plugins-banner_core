package net.hedtech.banner.controllers

import net.hedtech.banner.testing.BaseIntegrationTestCase
import net.hedtech.banner.testing.TermController
import org.junit.After
import org.junit.Before
import org.junit.Test

class TermControllerIntegrationTests extends BaseIntegrationTestCase{
    def termController

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        termController= new TermController()
    }

    @After
    public void tearDown() {
        super.tearDown()
    }

    @Test
    void testMain() {
        termController.main()
    }

    @Test
    void testList() {
        termController.localizer(termController.localizer)
    }
}

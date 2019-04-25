package net.hedtech.banner.controllers

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import net.hedtech.banner.testing.BaseIntegrationTestCase
import net.hedtech.banner.testing.TermController
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired

@Integration
@Rollback
class TermControllerIntegrationTests extends BaseIntegrationTestCase{
    @Autowired
    TermController termController

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
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

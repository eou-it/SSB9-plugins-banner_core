package net.hedtech.banner.utility

import grails.util.Holders
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import net.hedtech.banner.utility.DateUtility

/**
 * Created by eshaank on 11/4/2016.
 */
class DateUtilityIntegrationTests extends BaseIntegrationTestCase {
    def dateUtility

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        dateUtility = new DateUtility()
        super.setUp()
    }

    @After
    public void tearDown() {
        super.tearDown()
    }

    @Test
    void testGetTodayDate(){
        def outcome
        outcome=dateUtility.getTodayDate()
        assertNotNull(outcome)
    }
}

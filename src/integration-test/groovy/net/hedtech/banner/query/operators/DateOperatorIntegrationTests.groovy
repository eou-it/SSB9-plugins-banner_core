/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.query.operators

import net.hedtech.banner.query.DynamicFinder
import net.hedtech.banner.query.criteria.CriteriaParam
import net.hedtech.banner.testing.BaseIntegrationTestCase
import net.hedtech.banner.testing.TermForTesting
import org.junit.After
import org.junit.Before
import org.junit.Test

class DateOperatorIntegrationTests extends BaseIntegrationTestCase {

    def term_start_date = "201610"


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
    void testDateEqualsOperator() {
        def query = """FROM TermForTesting a """
        TermForTesting term = TermForTesting.findWhere(code: term_start_date)
        def dynamicFinder = new DynamicFinder(TermForTesting.class, query, "a")
        def pagingAndSortParams = [sortColumn: "startDate", sortDirection: "ASC"]
        def paramMap = [startDate: term.startDate]
        def criteriaMap = [[key: "startDate", binding: "startDate", operator: "equals"]]

        def filterData = [params: paramMap, criteria: criteriaMap]
        def result = dynamicFinder.find(filterData, pagingAndSortParams)
        assertNotNull result
        result.each { it ->
            assertEquals term.startDate, it.startDate
        }
    }


    @Test
    void testDateBetweenOperator() {
        def query = """FROM TermForTesting a """
        TermForTesting term = TermForTesting.findWhere(code: term_start_date)
        def dynamicFinder = new DynamicFinder(TermForTesting.class, query, "a")
        def pagingAndSortParams = [sortColumn: "startDate", sortDirection: "ASC"]
        def paramMap = [startDate: new Date('01-JAN-14'), startDate_and: new Date('01-JAN-15')]
        def criteriaMap = [[key: "startDate", binding: "startDate", operator: "between"]]

        def filterData = [params: paramMap, criteria: criteriaMap]
        def result = dynamicFinder.find(filterData, pagingAndSortParams)
        assertNotNull result

        result.each { it ->
            assertTrue(new Date('01-JAN-14') <= it.startDate && new Date('01-JAN-15') >= it.startDate)
        }
    }


    @Test
    void testDateGreaterThanOperator() {
        def query = """FROM TermForTesting a """
        def dynamicFinder = new DynamicFinder(TermForTesting.class, query, "a")
        def pagingAndSortParams = [sortColumn: "startDate", sortDirection: "ASC"]
        def paramMap = [startDate: new Date('01-JAN-14')]
        def criteriaMap = [[key: "startDate", binding: "startDate", operator: "greaterthan"]]

        def filterData = [params: paramMap, criteria: criteriaMap]
        def result = dynamicFinder.find(filterData, pagingAndSortParams)
        assertNotNull result

        result.each { it ->
            assertTrue(new Date('01-JAN-14') < it.startDate)
        }
    }

    @Test
    void testDateGreaterThanEqualsOperator() {
        def query = """FROM TermForTesting a """
        def dynamicFinder = new DynamicFinder(TermForTesting.class, query, "a")
        def pagingAndSortParams = [sortColumn: "startDate", sortDirection: "ASC"]
        def paramMap = [startDate: new Date('01-JAN-14')]
        def criteriaMap = [[key: "startDate", binding: "startDate", operator: "greaterthanequals"]]

        def filterData = [params: paramMap, criteria: criteriaMap]
        def result = dynamicFinder.find(filterData, pagingAndSortParams)
        assertNotNull result

        result.each { it ->
            assertTrue(new Date('01-JAN-14') <= it.startDate)
        }
    }

    @Test
    void testDateGreaterThanEqualsOperatorWithTime() {
        def query = """FROM TermForTesting a """
        def dynamicFinder = new DynamicFinder(TermForTesting.class, query, "a")
        def pagingAndSortParams = [sortColumn: "startDate", sortDirection: "ASC"]

        CriteriaParam param = new CriteriaParam();
        param.data = new Date('01-JAN-14')
        param.addAttribute("timeEntered", true)
        def paramMap = [startDate: param]
        def criteriaMap = [[key: "startDate", binding: "startDate", operator: "greaterthanequals"]]

        def filterData = [params: paramMap, criteria: criteriaMap]
        def result = dynamicFinder.find(filterData, pagingAndSortParams)
        assertNotNull result

        result.each { it ->
            assertTrue(new Date('01-JAN-14') <= it.startDate)
        }
    }



    @Test
    void testDateLessThanOperator() {
        def query = """FROM TermForTesting a """
        def dynamicFinder = new DynamicFinder(TermForTesting.class, query, "a")
        def pagingAndSortParams = [sortColumn: "startDate", sortDirection: "ASC"]
        def paramMap = [startDate: new Date('01-JAN-14')]
        def criteriaMap = [[key: "startDate", binding: "startDate", operator: "lessthan"]]

        def filterData = [params: paramMap, criteria: criteriaMap]
        def result = dynamicFinder.find(filterData, pagingAndSortParams)
        assertNotNull result

        result.each { it ->
            assertTrue(new Date('01-JAN-14') > it.startDate)
        }
    }


    @Test
    void testDateLessThanEqualsOperator() {
        def query = """FROM TermForTesting a """
        def dynamicFinder = new DynamicFinder(TermForTesting.class, query, "a")
        def pagingAndSortParams = [sortColumn: "startDate", sortDirection: "ASC"]
        def paramMap = [startDate: new Date('01-JAN-14')]
        def criteriaMap = [[key: "startDate", binding: "startDate", operator: "lessthanequals"]]

        def filterData = [params: paramMap, criteria: criteriaMap]
        def result = dynamicFinder.find(filterData, pagingAndSortParams)
        assertNotNull result

        result.each { it ->
            assertTrue(new Date('01-JAN-14') >= it.startDate)
        }
    }
}

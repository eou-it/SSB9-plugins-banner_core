/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.query.operators

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import net.hedtech.banner.query.DynamicFinder
import net.hedtech.banner.query.criteria.CriteriaData
import net.hedtech.banner.testing.BaseIntegrationTestCase
import net.hedtech.banner.testing.TermForTesting
import net.hedtech.banner.testing.ZipForTesting
import org.junit.After
import org.junit.Before
import org.junit.Test
@Integration
@Rollback

class AlphanumericOperatorsIntegrationTests extends BaseIntegrationTestCase {

    private CriteriaData criteriaData

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        criteriaData = new CriteriaData()
        super.setUp()
    }

    @After
    public void tearDown() {
        super.tearDown()
    }

    @Test
    void testEqualsIgnoreCase() {
        def query = """FROM ZipForTesting a """
        def dynamicFinder = new DynamicFinder(ZipForTesting.class, query, "a")
        def pagingAndSortParams = [sortColumn: "city",sortDirection: "ASC"]
        def paramMap = [city: "Broomall TEST"]
        def criteriaMap = [[key: "city", binding: "city", operator: "equalsignorecase"]]
        def filterData = [params: paramMap,criteria: criteriaMap]
        def result = dynamicFinder.find(filterData, pagingAndSortParams)
        assertNotNull result
        assertTrue paramMap['city'].toLowerCase() == result[0].city.toLowerCase()
    }


    @Test
    void testEqualsCase() {
        def query = """FROM ZipForTesting a """
        def dynamicFinder = new DynamicFinder(ZipForTesting.class, query, "a")
        def pagingAndSortParams = [sortColumn: "city",sortDirection: "ASC"]
        def paramMap = [city: "Broomall TEST"]
        def criteriaMap = [[key: "city", binding: "city", operator: "equals"]]
        def filterData = [params: paramMap,criteria: criteriaMap]
        def result = dynamicFinder.find(filterData, pagingAndSortParams)
        assertTrue result.size() == 0
    }


    @Test
    void testNotEqualsCase() {
        def query = """FROM ZipForTesting a """
        def dynamicFinder = new DynamicFinder(ZipForTesting.class, query, "a")
        def pagingAndSortParams = [sortColumn: "city",sortDirection: "ASC"]
        def paramMap = [city: "Broomall TEST"]
        def criteriaMap = [[key: "city", binding: "city", operator: "notequals"]]
        def filterData = [params: paramMap,criteria: criteriaMap]
        def result = dynamicFinder.find(filterData, pagingAndSortParams)
        assertTrue result.size() > 1
    }

    @Test
    void testContainsCase() {
        def query = """FROM ZipForTesting a """
        def dynamicFinder = new DynamicFinder(ZipForTesting.class, query, "a")
        def pagingAndSortParams = [sortColumn: "city",sortDirection: "ASC"]
        def paramMap = [city: "Broomall TEST"]
        def criteriaMap = [[key: "city", binding: "city", operator: "contains"]]
        def filterData = [params: paramMap,criteria: criteriaMap]
        def result = dynamicFinder.find(filterData, pagingAndSortParams)
        assertNotNull result
        assertTrue paramMap['city'].toLowerCase() == result[0].city.toLowerCase()
    }

    @Test
    void testGreaterCase() {
        def query = """FROM ZipForTesting a """
        def dynamicFinder = new DynamicFinder(ZipForTesting.class, query, "a")
        def pagingAndSortParams = [sortColumn: "code",sortDirection: "ASC"]
        def paramMap = [code: "98119"]
        def criteriaMap = [[key: "code", binding: "code", operator: "greaterthan"]]
        def filterData = [params: paramMap,criteria: criteriaMap]
        def result = dynamicFinder.find(filterData, pagingAndSortParams)
        assertNotNull result
        assertTrue result.size() > 2
    }

    @Test
    void testLessCase() {
        def query = """FROM ZipForTesting a """
        def dynamicFinder = new DynamicFinder(ZipForTesting.class, query, "a")
        def pagingAndSortParams = [sortColumn: "code",sortDirection: "ASC"]
        def paramMap = [code: "98119"]
        def criteriaMap = [[key: "code", binding: "code", operator: "lessthan"]]
        def filterData = [params: paramMap,criteria: criteriaMap]
        def result = dynamicFinder.find(filterData, pagingAndSortParams)
        assertNotNull result
        assertTrue result.size() > 2
    }


    @Test
    void testGreaterThanEqualCase() {
        def query = """FROM ZipForTesting a """
        def dynamicFinder = new DynamicFinder(ZipForTesting.class, query, "a")
        def pagingAndSortParams = [sortColumn: "code",sortDirection: "ASC"]
        def paramMap = [code: "98119"]
        def criteriaMap = [[key: "code", binding: "code", operator: "greaterthanequals"]]
        def filterData = [params: paramMap,criteria: criteriaMap]
        def result = dynamicFinder.find(filterData, pagingAndSortParams)
        assertNotNull result
        assertTrue result.size() > 2
    }

    @Test
    void testLessThanEqualCase() {
        def query = """FROM TermForTesting a """
        def dynamicFinder = new DynamicFinder(ZipForTesting.class, query, "a")
        def pagingAndSortParams = [sortColumn: "code",sortDirection: "ASC"]
        def paramMap = [code: "98190"]
        def criteriaMap = [[key: "code", binding: "code", operator: "lessthanequals"]]
        def filterData = [params: paramMap,criteria: criteriaMap]
        def result = dynamicFinder.find(filterData, pagingAndSortParams)
        assertNotNull result
        assertTrue result.size() > 2
    }

    @Test
    void testIsNullCase() {
        def query = """FROM TermForTesting a """
        def dynamicFinder = new DynamicFinder(TermForTesting.class, query, "a")
        def pagingAndSortParams = [sortColumn: "startDate",sortDirection: "ASC"]
        def paramMap = [:]
        def criteriaMap = [[key: "financialAidTerm", binding: "financialAidTerm", operator: "isnull"]]
        def filterData = [params: paramMap, criteria: criteriaMap]
        def result = dynamicFinder.find(filterData, pagingAndSortParams)
        assertNotNull result
        result.each { it ->
            assertEquals(null, it.financialAidTerm)
        }
    }

    @Test
    void testIsNotNullCase() {
        def query = """FROM TermForTesting a """
        def dynamicFinder = new DynamicFinder(TermForTesting.class, query, "a")
        def pagingAndSortParams = [sortColumn: "startDate",sortDirection: "ASC"]
        def paramMap = [:]
        def criteriaMap = [[key: "financialAidTerm", binding: "financialAidTerm", operator: "isnotnull"]]
        def filterData = [params: paramMap,criteria: criteriaMap]
        def result = dynamicFinder.find(filterData, pagingAndSortParams)
        assertNotNull result
        result.each { it ->
            assertNotNull it.financialAidTerm
        }
    }



}

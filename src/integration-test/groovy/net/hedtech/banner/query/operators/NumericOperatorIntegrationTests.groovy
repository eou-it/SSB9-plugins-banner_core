package net.hedtech.banner.query.operators

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import net.hedtech.banner.query.DynamicFinder
import net.hedtech.banner.query.criteria.CriteriaData
import net.hedtech.banner.query.criteria.CriteriaParam
import net.hedtech.banner.testing.BaseIntegrationTestCase
import net.hedtech.banner.testing.TermForTesting
import org.junit.After
import org.junit.Before
import org.junit.Test

@Integration
@Rollback
class NumericOperatorIntegrationTests extends BaseIntegrationTestCase{
    private NumericBetweenOperator numericBetweenOperator
    private CriteriaData criteriaData

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        numericBetweenOperator = new NumericBetweenOperator()
        criteriaData = new CriteriaData()
        super.setUp()
    }


    @After
    public void tearDown() {
        super.tearDown()
    }


    @Test
    void testNumericBetweenOperator() {
        def query = """FROM TermForTesting a """
        def dynamicFinder = new DynamicFinder(TermForTesting.class, query, "a")
        def pagingAndSortParams = [sortColumn: "financialAidPeriod",sortDirection: "ASC"]
        def paramMap = [financialAidPeriod: 6,financialAidPeriod_and:9]
        def criteriaMap = [[key: "financialAidPeriod", binding: "financialAidPeriod", operator: "between"]]
        def filterData = [params: paramMap, criteria: criteriaMap]
        def result = dynamicFinder.find(filterData, pagingAndSortParams)
        assertNotNull result
        result.each { it ->
            assertTrue( it.financialAidPeriod >= paramMap.financialAidPeriod && it.financialAidPeriod <= paramMap.financialAidPeriod_and)
        }
    }

    @Test
    void testNumericGreaterthanOperator() {
        def query = """FROM TermForTesting a """
        def dynamicFinder = new DynamicFinder(TermForTesting.class, query, "a")
        def pagingAndSortParams = [sortColumn: "financialAidPeriod",sortDirection: "ASC"]
        def paramMap = [financialAidPeriod: 6]
        def criteriaMap = [[key: "financialAidPeriod", binding: "financialAidPeriod", operator: "greaterthan"]]
        def filterData = [params: paramMap, criteria: criteriaMap]
        def result = dynamicFinder.find(filterData, pagingAndSortParams)
        assertNotNull result
        result.each { it ->
            assertTrue( it.financialAidPeriod > paramMap['financialAidPeriod'])
        }
    }

    @Test
    void testNumericLessthanOperator() {
        def query = """FROM TermForTesting a """
        def dynamicFinder = new DynamicFinder(TermForTesting.class, query, "a")
        def pagingAndSortParams = [sortColumn: "financialAidPeriod",sortDirection: "ASC"]
        def paramMap = [financialAidPeriod: 9]
        def criteriaMap = [[key: "financialAidPeriod", binding: "financialAidPeriod", operator: "lessthan"]]
        def filterData = [params: paramMap, criteria: criteriaMap]
        def result = dynamicFinder.find(filterData, pagingAndSortParams)
        assertNotNull result
        result.each { it ->
            assertTrue( it.financialAidPeriod < paramMap['financialAidPeriod'])
        }
    }

    @Test
    void testNumericEqualsOperator() {
        def query = """FROM TermForTesting a """
        def dynamicFinder = new DynamicFinder(TermForTesting.class, query, "a")
        def pagingAndSortParams = [sortColumn: "financialAidPeriod",sortDirection: "ASC"]
        def paramMap = [financialAidPeriod: 9]
        def criteriaMap = [[key: "financialAidPeriod", binding: "financialAidPeriod", operator: "equals"]]
        def filterData = [params: paramMap, criteria: criteriaMap]
        def result = dynamicFinder.find(filterData, pagingAndSortParams)
        assertNotNull result
        result.each { it ->
            assertTrue( it.financialAidPeriod == paramMap['financialAidPeriod'])
        }
    }

    @Test
    void testNumericNullOperator() {
        def query = """FROM TermForTesting a """
        def dynamicFinder = new DynamicFinder(TermForTesting.class, query, "a")
        def pagingAndSortParams = [sortColumn: "financialAidPeriod",sortDirection: "ASC"]
        def paramMap = [:]
        def criteriaMap = [[key: "financialAidPeriod", binding: "financialAidPeriod", operator: "isnull"]]
        def filterData = [params: paramMap, criteria: criteriaMap]
        def result = dynamicFinder.find(filterData, pagingAndSortParams)
        assertNotNull result
        result.each { it ->
            assertEquals(null, it.financialAidPeriod)
        }
    }
    @Test
    void testNumericNotNullOperator() {
        def query = """FROM TermForTesting a """
        def dynamicFinder = new DynamicFinder(TermForTesting.class, query, "a")
        def pagingAndSortParams = [sortColumn: "financialAidPeriod",sortDirection: "ASC"]
        def paramMap = [:]
        def criteriaMap = [[key: "financialAidPeriod", binding: "financialAidPeriod", operator: "isnotnull"]]
        def filterData = [params: paramMap, criteria: criteriaMap]
        def result = dynamicFinder.find(filterData, pagingAndSortParams)
        assertNotNull result
        result.each { it ->
            assertNotNull(it.financialAidPeriod);
        }

    }
}

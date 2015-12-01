package net.hedtech.banner.query

import net.hedtech.banner.testing.BaseIntegrationTestCase
import net.hedtech.banner.testing.ZipForTesting
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * ZipForTesting domain is mapped to GTVZIPC which is
 * base table that comes with the template. So, the data
 * is going to be consistent across the environments.
 * So the test case does not have to create a record
 * to ensure the consistent execution.
 *
 * Created by vimalm on 11/25/2015.
 */
class DynamicFinderIntegrationTests extends BaseIntegrationTestCase {

    def zipForTestingObject
    def filterData

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        zipForTestingObject = new ZipForTesting(code: '')

        filterData = [:]
    }

    @After
    public void tearDown() {
        super.tearDown()
    }

    @Test
    void testRetrievalSingleRecord(){
        def query = """FROM  ZipForTesting a WHERE (a.code = :zipcode) """
        filterData.params = ["zipcode":"19426"]
        def pagingAndSortParams = [:]

        def dynamicFinder = new DynamicFinder(zipForTestingObject.class, query, "a")
        def result = dynamicFinder.find(filterData,pagingAndSortParams) ;
        assertEquals 1, result.size()
        assertEquals "Collegeville",  result.first().city
    }

    @Test
    void testSortAscending(){
        def query = """FROM  ZipForTesting a WHERE (a.code like :zipcode) """
        filterData.params = ["zipcode":"%19%"]
        def pagingAndSortParams = [sortColumn: "city", sortDirection: "ASC"]

        def dynamicFinder = new DynamicFinder(zipForTestingObject.class, query, "a")
        def result = dynamicFinder.find(filterData,pagingAndSortParams) ;
        assertEquals 28, result.size()
        assertEquals "Broomall test",  result.first().city
        assertEquals "Ypsilanti",  result.last().city
    }

    @Test
    void testSortDescending(){
        def query = """FROM  ZipForTesting a WHERE (a.code like :zipcode) """
        filterData.params = ["zipcode":"%19%"]
        def pagingAndSortParams = [sortColumn: "city", sortDirection: "DESC"]

        def dynamicFinder = new DynamicFinder(zipForTestingObject.class, query, "a")
        def result = dynamicFinder.find(filterData,pagingAndSortParams) ;
        assertEquals 28, result.size()
        assertEquals "Ypsilanti",  result.first().city
        assertEquals "Broomall test",  result.last().city
    }

    @Test
    void testFirstRecordOfPage2WithPageSize5(){
        int PAGE_SIZE = 5;
        int START_PAGE = 2;

        def query = """FROM  ZipForTesting a WHERE (a.code like :zipcode) """
        filterData.params = ["zipcode":"%19%"]
        def pagingAndSortParams = [sortColumn: "city", sortDirection: "ASC", "max": PAGE_SIZE, "offset": (START_PAGE-1)*PAGE_SIZE]

        def dynamicFinder = new DynamicFinder(zipForTestingObject.class, query, "a")
        def result = dynamicFinder.find(filterData,pagingAndSortParams) ;
        assertEquals PAGE_SIZE, result.size()
        assertEquals "Danvers",  result.first().city
        assertEquals "Lansdale",  result.last().city
    }

}

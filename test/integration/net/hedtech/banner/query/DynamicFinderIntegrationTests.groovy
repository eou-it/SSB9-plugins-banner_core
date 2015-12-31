/*******************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.query

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.i18n.MessageHelper
import net.hedtech.banner.testing.BaseIntegrationTestCase
import net.hedtech.banner.testing.ZipForTesting
import net.hedtech.banner.testing.TermForTesting
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.core.convert.ConversionFailedException

/**
 * ZipForTesting domain is mapped to GTVZIPC which is
 * base table that comes with the template. So, the data
 * is going to be consistent across the environments.
 * So the test case does not have to create a record
 * to ensure the consistent execution.
 *
 */
class DynamicFinderIntegrationTests extends BaseIntegrationTestCase {

    def zipForTestingObject
    def filterData
    def termForTestingObject

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        zipForTestingObject = new ZipForTesting(code: '')
        termForTestingObject = new TermForTesting()
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

    @Test
    void testSortCriteria(){
        def query = """FROM  ZipForTesting a WHERE (a.code like :zipcode) """
        filterData.params = ["zipcode": "%19%"]
        def pagingAndSortParams = [sortCriteria :  [
                ["sortColumn": "city", "sortDirection": "asc"],
                ["sortColumn": "code", "sortDirection": "asc"],
        ]]

        def dynamicFinder = new DynamicFinder(zipForTestingObject.class, query, "a")
        def result = dynamicFinder.find(filterData,pagingAndSortParams) ;
        assertEquals 28, result.size()
        assertEquals "31904",  result.get(3).code
        assertEquals "31907",  result.get(4).code
    }

    @Test
    void testFetchAll(){
        def query = """FROM  ZipForTesting a WHERE (a.code like :zipcode) """
        filterData.params = ["zipcode": "%19%"]
        def pagingAndSortParams = [sortCriteria :  [
                ["sortColumn": "city", "sortDirection": "asc"],
                ["sortColumn": "code", "sortDirection": "asc"],
        ]]

        def result = DynamicFinder.fetchAll(zipForTestingObject.class, query, "a", filterData,pagingAndSortParams) ;
        assertEquals 28, result.size()
        assertEquals "31904",  result.get(3).code
        assertEquals "31907",  result.get(4).code
    }

    @Test
    void testSortByColumnInRelationalDomainClass(){
        def query = """FROM  TermForTesting a WHERE (a.code like :code) """
        filterData.params = ["code": "%19%"]
        def pagingAndSortParams = [sortCriteria :  [
                ["sortColumn": "academicYear.code", "sortDirection": "asc"],
        ]]

        def result = DynamicFinder.fetchAll(termForTestingObject.class, query, "a", filterData,pagingAndSortParams) ;
        assertEquals 67, result.size()
        assertEquals "197410",  result.get(3).code
        assertEquals "198010",  result.get(4).code
    }

    @Test
    void testSortByColumnID(){
        def query = """FROM  TermForTesting a WHERE (a.code like :code) """
        filterData.params = ["code": "%20%"]
        def pagingAndSortParams = [sortCriteria :  [
                ["sortColumn": "id", "sortDirection": "asc"],
        ]]
        def result = DynamicFinder.fetchAll(termForTestingObject.class, query, "a", filterData,pagingAndSortParams) ;
        assertEquals 375, result.size()
        assertEquals "200211", result.get(0).code
        assertEquals "200255", result.get(4).code
    }

    @Test
    void testSortByColumnVersion(){
        def query = """FROM  TermForTesting a WHERE (a.code like :code) """
        filterData.params = ["code": "%201%"]
        def pagingAndSortParams = [sortCriteria :  [
                ["sortColumn": "version", "sortDirection": "asc"],
        ]]
        def result = DynamicFinder.fetchAll(termForTestingObject.class, query, "a", filterData,pagingAndSortParams) ;
        assertEquals 45, result.size()
        assertEquals "202010", result.get(1).code
        assertEquals "201440", result.get(3).code
    }


    @Test
    void testSoryByInvalidColumn(){
        def query = """FROM  TermForTesting a WHERE (a.code like :code) """
        filterData.params = ["code": "%19%"]
        def pagingAndSortParams = [sortCriteria :  [
                ["sortColumn": "version;delete from spriden;", "sortDirection": "asc"],
        ]]
        def dynamicFinder = new DynamicFinder(zipForTestingObject.class, query, "a")
        shouldFail(ApplicationException) {
            try {
                def result = dynamicFinder.find(filterData, pagingAndSortParams);
            } catch (ApplicationException ae) {
                assert MessageHelper.message("net.hedtech.banner.query.DynamicFinder.QuerySyntaxException"), ae.message
                throw ae
            }
        }
    }

    @Test
    void testInformationLeakPreventionForSortColumn(){
        def query = """FROM  ZipForTesting a WHERE (a.code like :zipcode) """
        filterData.params = ["zipcode":"%19%"]
        def pagingAndSortParams = [sortColumn: "citysome"]

        def dynamicFinder = new DynamicFinder(zipForTestingObject.class, query, "a")
        shouldFail(ApplicationException) {
            try {
                def result = dynamicFinder.find(filterData, pagingAndSortParams);
            } catch (ApplicationException ae) {
                assert MessageHelper.message("net.hedtech.banner.query.DynamicFinder.QuerySyntaxException"), ae.message
                throw ae
            }
        }
    }

    @Test
    void testInformationLeakPreventionforSortDirection(){
        def query = """FROM  ZipForTesting a WHERE (a.code like :zipcode) """
        filterData.params = ["zipcode":"%19%"]
        def pagingAndSortParams = [sortColumn: "city" , sortDirection: "xyz"]

        def dynamicFinder = new DynamicFinder(zipForTestingObject.class, query, "a")
        shouldFail(ApplicationException) {
            try {
                def result = dynamicFinder.find(filterData, pagingAndSortParams);
            } catch (ApplicationException ae) {
                assert MessageHelper.message("net.hedtech.banner.query.DynamicFinder.QuerySyntaxException"), ae.message
                throw ae
            }
        }
    }

    @Test
    void testInformationLeakPreventionforMax(){
        def query = """FROM  ZipForTesting a WHERE (a.code like :zipcode) """
        filterData.params = ["zipcode":"%19%"]
        def pagingAndSortParams = [max: "hjghjj", offset: 5]

        def dynamicFinder = new DynamicFinder(zipForTestingObject.class, query, "a")
        shouldFail(ConversionFailedException) {
            try {
                def result = dynamicFinder.find(filterData, pagingAndSortParams);
            } catch (ConversionFailedException ae) {
                assert "For input string: \"hjghjj\"", ae.message
                throw ae
            }
        }
    }

    @Test
    void testInformationLeakPreventionforOffset(){
        def query = """FROM  ZipForTesting a WHERE (a.code like :zipcode) """
        filterData.params = ["zipcode":"%19%"]
        def pagingAndSortParams = [max: 5, offset: "hjghjj"]

        def dynamicFinder = new DynamicFinder(zipForTestingObject.class, query, "a")
        shouldFail(ConversionFailedException) {
            try {
                def result = dynamicFinder.find(filterData, pagingAndSortParams);
            } catch (ConversionFailedException ae) {
                assert "For input string: \"hjghjj\"", ae.message
                throw ae
            }
        }
    }

    @Test
    void testHqlInjectionPreventionForSortColumn(){
        def query = """FROM  ZipForTesting a WHERE (a.code like :zipcode) """
        filterData.params = ["zipcode":"%19%"]
        def pagingAndSortParams = [sortColumn: "city;delete from spriden;"]

        def dynamicFinder = new DynamicFinder(zipForTestingObject.class, query, "a")
        shouldFail(ApplicationException) {
            try {
                def result = dynamicFinder.find(filterData, pagingAndSortParams);
            } catch (ApplicationException ae) {
                assert MessageHelper.message("net.hedtech.banner.query.DynamicFinder.QuerySyntaxException"), ae.message
                throw ae
            }
        }
    }
}

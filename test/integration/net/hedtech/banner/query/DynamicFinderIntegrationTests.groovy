/*******************************************************************************
 Copyright 2015- 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.query

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.testing.CommonMatchingSourceRuleForTesting
import net.hedtech.banner.i18n.MessageHelper
import net.hedtech.banner.testing.BaseIntegrationTestCase
import net.hedtech.banner.testing.TermForTesting
import net.hedtech.banner.testing.ZipForTesting
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.context.ApplicationContext
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
    def commonMatchingSourceRule

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        zipForTestingObject = new ZipForTesting(code: '')
        termForTestingObject = new TermForTesting()
        commonMatchingSourceRule = new CommonMatchingSourceRuleForTesting()
        filterData = [:]
    }

    @After
    public void tearDown() {
        super.tearDown()
    }

    @Test
    void testRetrievalSingleRecord(){
        def query = """FROM  ZipForTesting a WHERE (a.code = :zipcode and a.city = :city) """
        filterData.params = ["zipcode":"98119", "city":"Broomall test"]
        def pagingAndSortParams = [:]

        def dynamicFinder = new DynamicFinder(zipForTestingObject.class, query, "a")
        def result = dynamicFinder.find(filterData,pagingAndSortParams) ;
        assert result.size() > 0
        assertNotNull result?.find { it.city == "Broomall test" }?.city

        assertNotNull(dynamicFinder.count(filterData))

    }

    @Test
    void testSortAscending(){
        def query = """FROM  ZipForTesting a WHERE (a.code like :zipcode) """
        filterData.params = ["zipcode":"%19%"]
        def pagingAndSortParams = [sortColumn: "city", sortDirection: "ASC"]

        def dynamicFinder = new DynamicFinder(zipForTestingObject.class, query, "a")
        def result = dynamicFinder.find(filterData,pagingAndSortParams) ;
        assert result.size() > 0
        assertNotNull result?.find { it.city == "Broomall test" }?.city
        assertNotNull result?.find { it.city == "Ypsilanti" }?.city
    }

    @Test
    void testSortDescending(){
        def query = """FROM  ZipForTesting a WHERE (a.code like :zipcode) """
        filterData.params = ["zipcode":"%19%"]
        def pagingAndSortParams = [sortColumn: "city", sortDirection: "DESC"]

        def dynamicFinder = new DynamicFinder(zipForTestingObject.class, query, "a")
        def results = dynamicFinder.find(filterData,pagingAndSortParams) ;
        assert results.size() > 0
        assertNotNull results?.find { it.city == "Ypsilanti" }?.city
        assertNotNull results?.find { it.city == "Broomall test" }?.city
    }

    @Test
    void testFirstRecordOfPage2WithPageSize5(){
        int PAGE_SIZE = 10;
        int START_PAGE = 1;

        def query = """FROM  ZipForTesting a WHERE (a.code like :zipcode) """
        filterData.params = ["zipcode":"%19%"]
        def pagingAndSortParams = [sortColumn: "city", sortDirection: "ASC", "max": PAGE_SIZE, "offset": (START_PAGE-1)*PAGE_SIZE]

        def dynamicFinder = new DynamicFinder(zipForTestingObject.class, query, "a")
        def result = dynamicFinder.find(filterData,pagingAndSortParams) ;
        assertEquals PAGE_SIZE, result.size()
        assertNotNull  result?.find { it.city == "Broomall test" }?.city
        assertNotNull  result?.find { it.city == "Lansdale" }?.city
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
        assert result.size() > 0
        assertNotNull result?.find { it.code == "31904" }?.code
        assertNotNull result?.find { it.code == "31907" }?.code
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
        assert result.size() > 0
        assertNotNull result?.find { it.code == "31904" }?.code
        assertNotNull result?.find { it.code == "31907" }?.code
    }

    @Test
    void testSortByColumnInRelationalDomainClass(){
        def query = """FROM  TermForTesting a WHERE (a.code like :code) """
        filterData.params = ["code": "%19%"]
        def pagingAndSortParams = [sortCriteria :  [
                ["sortColumn": "academicYear.code", "sortDirection": "asc"],
        ]]

        def result = DynamicFinder.fetchAll(termForTestingObject.class, query, "a", filterData,pagingAndSortParams) ;
        assert result.size() > 0
        assertNotNull result?.find { it.code == "198830" }?.code
        assertNotNull result?.find { it.code == "198840" }?.code
    }

    @Test
    void testSortByColumnInRelationalDomainClassNLevel(){
        def query = """FROM  CommonMatchingSourceRuleForTesting a WHERE (a.dataOrigin like :dataOrigin) """
        filterData.params = ["dataOrigin": "%A%"]
        def pagingAndSortParams = [sortCriteria :  [
                ["sortColumn": "id", "sortDirection": "asc"],
        ]]

        def result = DynamicFinder.fetchAll(commonMatchingSourceRule.class, query, "a", filterData,pagingAndSortParams) ;
        assertTrue(result.size()>0)

    }

    @Test
    void testSortByMultipleColumns(){
        def query = """FROM  CommonMatchingSourceRuleForTesting a WHERE (a.dataOrigin like :dataOrigin) """
        filterData.params = ["dataOrigin": "%A%"]
        def pagingAndSortParams = [sortCriteria :  [
                ["sortColumn": "addressType,id,version", "sortDirection": "asc"],
        ]]

        def result = DynamicFinder.fetchAll(commonMatchingSourceRule.class, query, "a", filterData,pagingAndSortParams) ;
        assertTrue(result.size()>0)

    }

    @Test
    void testSortByMultipleColumnsWithRelationalDomainColumns(){
        def query = """FROM  CommonMatchingSourceRuleForTesting a WHERE (a.dataOrigin like :dataOrigin) """
        filterData.params = ["dataOrigin": "%A%"]
        def pagingAndSortParams = [sortCriteria :  [
                ["sortColumn": "id,version", "sortDirection": "asc"],
        ]]

        def result = DynamicFinder.fetchAll(commonMatchingSourceRule.class, query, "a", filterData,pagingAndSortParams) ;
        assertTrue(result.size()>0)
    }

    @Test
    void testSortForInvalidMultipleColumnsWithRelationalDomainColumns(){
        def query = """FROM  CommonMatchingSourceRuleForTesting a WHERE (a.dataOrigin like :dataOrigin) """
        filterData.params = ["dataOrigin": "%a%"]
        def pagingAndSortParams = [sortCriteria :  [
                ["sortColumn": "addressType.telephoneType.code,id,xyz", "sortDirection": "asc"],
        ]]
        shouldFail(ApplicationException) {
            try {
                DynamicFinder.fetchAll(commonMatchingSourceRule.class, query, "a", filterData,pagingAndSortParams) ;
            } catch (ApplicationException ae) {
                assert MessageHelper.message("net.hedtech.banner.query.DynamicFinder.QuerySyntaxException"), ae.message
                throw ae
            }
        }
    }
    @Test
    void testSortForMultipleColumnsWithInvalidRelationalDomainColumns(){
        def query = """FROM  CommonMatchingSourceRuleForTesting a WHERE (a.dataOrigin like :dataOrigin) """
        filterData.params = ["dataOrigin": "%a%"]
        def pagingAndSortParams = [sortCriteria :  [
                ["sortColumn": "addressType.xyz.code,id,version", "sortDirection": "asc"],
        ]]
        shouldFail(ApplicationException) {
            try {
                DynamicFinder.fetchAll(commonMatchingSourceRule.class, query, "a", filterData,pagingAndSortParams) ;
            } catch (ApplicationException ae) {
                assert MessageHelper.message("net.hedtech.banner.query.DynamicFinder.QuerySyntaxException"), ae.message
                throw ae
            }
        }
    }

    @Test
    void testSortByColumnInRelationalDomainClassNLevelWithoutRelation(){
        def query = """FROM  CommonMatchingSourceRuleForTesting a WHERE (a.dataOrigin like :dataOrigin) """
        filterData.params = ["dataOrigin": "%a%"]
        def pagingAndSortParams = [sortCriteria :  [
                ["sortColumn": "addressType.telephoneType.code.xyz", "sortDirection": "asc"],
        ]]
        shouldFail(ApplicationException) {
            try {
                DynamicFinder.fetchAll(commonMatchingSourceRule.class, query, "a", filterData,pagingAndSortParams) ;
            } catch (ApplicationException ae) {
                assert MessageHelper.message("net.hedtech.banner.query.DynamicFinder.QuerySyntaxException"), ae.message
                throw ae
            }
        }
    }

    @Test
    void testSortByColumnInRelationalDomainClassNLevelForInvalidColumn(){
        def query = """FROM  CommonMatchingSourceRuleForTesting a WHERE (a.dataOrigin like :dataOrigin) """
        filterData.params = ["dataOrigin": "%a%"]
        def pagingAndSortParams = [sortCriteria :  [
                ["sortColumn": "emailType.urlIndicator.xyz", "sortDirection": "asc"],
        ]]
        shouldFail(ApplicationException) {
            try {
                DynamicFinder.fetchAll(commonMatchingSourceRule.class, query, "a", filterData,pagingAndSortParams) ;
            } catch (ApplicationException ae) {
                assert MessageHelper.message("net.hedtech.banner.query.DynamicFinder.QuerySyntaxException"), ae.message
                throw ae
            }
        }
       }

    @Test
    void testSortByColumnInRelationalDomainClassNLevelWithoutAssociation(){
        def query = """FROM  CommonMatchingSourceRuleForTesting a WHERE (a.dataOrigin like :dataOrigin) """
        filterData.params = ["dataOrigin": "%a%"]
        def pagingAndSortParams = [sortCriteria :  [
                ["sortColumn": "emailType.urlIndicator.lastModified", "sortDirection": "asc"],
        ]]
        shouldFail(ApplicationException) {
            try {
                DynamicFinder.fetchAll(commonMatchingSourceRule.class, query, "a", filterData,pagingAndSortParams) ;
            } catch (ApplicationException ae) {
                assert MessageHelper.message("net.hedtech.banner.query.DynamicFinder.QuerySyntaxException"), ae.message
                throw ae
            }
        }
    }

    @Test
    void testSortByColumnID(){
        def query = """FROM  TermForTesting a WHERE (a.code like :code) """
        filterData.params = ["code": "%20%"]
        def pagingAndSortParams = [sortCriteria :  [
                ["sortColumn": "id", "sortDirection": "asc"],
        ]]
        def result = DynamicFinder.fetchAll(termForTestingObject.class, query, "a", filterData,pagingAndSortParams) ;
        assert result.size() > 0
        assertNotNull(result?.find { it.code == "200211" }?.code)
        assertNotNull(result?.find { it.code == "200010" }?.code)
    }

    @Test
    void testSortByColumnVersion(){
        def query = """FROM  TermForTesting a WHERE (a.code like :code) """
        filterData.params = ["code": "%201%"]
        def pagingAndSortParams = [sortCriteria :  [
                ["sortColumn": "version", "sortDirection": "asc"],
        ]]
        def result = DynamicFinder.fetchAll(termForTestingObject.class, query, "a", filterData,pagingAndSortParams) ;
        assert result.size() > 0
        assertNotNull result?.find { it.code == "201670" }?.code
        assertNotNull result?.find { it.code == "201440" }?.code
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
               dynamicFinder.find(filterData, pagingAndSortParams);
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
                dynamicFinder.find(filterData, pagingAndSortParams);
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
                dynamicFinder.find(filterData, pagingAndSortParams);
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
                dynamicFinder.find(filterData, pagingAndSortParams);
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
                dynamicFinder.find(filterData, pagingAndSortParams);
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
                dynamicFinder.find(filterData, pagingAndSortParams);
            } catch (ApplicationException ae) {
                assert MessageHelper.message("net.hedtech.banner.query.DynamicFinder.QuerySyntaxException"), ae.message
                throw ae
            }
        }
    }

    @Test
    public void testGetCriteriaParamsFromParams () {
        def query = """FROM  ZipForTesting a WHERE (a.code = :zipcode and a.city = :city) """
        def dynamicFinder = new DynamicFinder(zipForTestingObject.class, query, "a")
        filterData.params = ["zipcode":"%19%"]
        assertNotNull (dynamicFinder.getCriteriaParamsFromParams(filterData))
    }

    @Test
    public void testGetApplicationContext () {
        def query = """FROM  ZipForTesting a WHERE (a.code = :zipcode and a.city = :city) """
        def dynamicFinder = new DynamicFinder(zipForTestingObject.class, query, "a")
        assertTrue(dynamicFinder.getApplicationContext() instanceof ApplicationContext)
    }

}

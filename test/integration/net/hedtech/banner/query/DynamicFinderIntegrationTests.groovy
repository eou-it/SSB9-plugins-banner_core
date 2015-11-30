package net.hedtech.banner.query

import net.hedtech.banner.testing.BaseIntegrationTestCase
import net.hedtech.banner.testing.FacultyScheduleQueryViewForTesting
import org.apache.commons.io.input.TeeInputStream
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Created by vimalm on 11/25/2015.
 */
class DynamicFinderIntegrationTests extends BaseIntegrationTestCase {

    def domainClass

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        domainClass = new FacultyScheduleQueryViewForTesting()
        super.setUp()
    }

    @After
    public void tearDown() {
        super.tearDown()
    }

    @Test
    void testMaxSizeOffset(){

        def pagingAndSortParams = ["max": 5, "offset": 1]
        def filterData = [:]
        def param = [:]
        param."soundexLastName" = ""
        param."soundexFirstName" = ""
        filterData.params = param

        def query = """FROM  FacultyScheduleQueryViewForTesting a
          WHERE ((soundex(a.lastName) = soundex(:soundexLastName)) or :soundexLastName is null)
                and ((soundex(a.firstName) = soundex(:soundexFirstName)) or :soundexFirstName is null) """

        def dynamicFinder = new DynamicFinder(domainClass, query, "a")
        def result = dynamicFinder.find(filterData,pagingAndSortParams) ;
        assertTrue result.size() == 5
        assertTrue (((FacultyScheduleQueryViewForTesting)((ArrayList)result).get(0)).beginTime == "0900")
        assertTrue (((FacultyScheduleQueryViewForTesting)((ArrayList)result).get(0)).endTime == "1000")
    }

}

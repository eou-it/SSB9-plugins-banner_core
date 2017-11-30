/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.query.operators

import net.hedtech.banner.query.criteria.CriteriaData
import net.hedtech.banner.query.criteria.CriteriaParam
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

class DateBetweenOperatorIntegrationTests extends BaseIntegrationTestCase {

    private DateBetweenOperator dateBetweenOperator
    private CriteriaData criteriaData

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        dateBetweenOperator = new DateBetweenOperator()
        criteriaData = new CriteriaData()
        super.setUp()
    }


    @After
    public void tearDown() {
        super.tearDown()
    }

    @Test
    void testingDateBetweenOperator(){
        CriteriaParam param1 = new CriteriaParam();
        param1.addAttribute('bannerId','testing');
        CriteriaParam param2 = new CriteriaParam();
        param2.addAttribute('bannerId1','testing1');
        List<CriteriaParam> criteriaParamList = [param1, param2]
        criteriaData.addParams(criteriaParamList);
        assertTrue criteriaData.params.size() == 2
        criteriaData.tableAlias = "SPRIDEN_Record"
        criteriaData.tableBindingAttribute = "tableBindingAttribute"
        def result = dateBetweenOperator.getQueryString(criteriaData)
        assertNotNull result
    }

    @Test
    void timeEnteredAttribute(){
        CriteriaParam param1 = new CriteriaParam();
        param1.addAttribute('timeEntered',true);
        CriteriaParam param2 = new CriteriaParam();
        param2.addAttribute('timeEntered',true);
        List<CriteriaParam> criteriaParamList = [param1, param2]
        criteriaData.addParams(criteriaParamList);
        assertTrue criteriaData.params.size() == 2
        criteriaData.tableAlias = "SPRIDEN_Record"
        criteriaData.tableBindingAttribute = "tableBindingAttribute"
        def result = dateBetweenOperator.getQueryString(criteriaData)
        assertNotNull result
    }

    @Test
    void testNoCriteriaData(){
        criteriaData.tableAlias = "SPRIDEN_Record"
        criteriaData.tableBindingAttribute = "tableBindingAttribute"
        def result = dateBetweenOperator.getQueryString(criteriaData)
        assertTrue result == ""
    }

}

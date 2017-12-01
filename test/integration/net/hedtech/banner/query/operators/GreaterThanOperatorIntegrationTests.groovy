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

class GreaterThanOperatorIntegrationTests extends BaseIntegrationTestCase {

    private GreaterThanOperator greaterThanOperator
    private CriteriaData criteriaData

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        greaterThanOperator = new GreaterThanOperator()
        criteriaData = new CriteriaData()
        super.setUp()
    }


    @After
    public void tearDown() {
        super.tearDown()
    }

    @Test
    void testingGreaterThanOperator(){
        CriteriaParam param = new CriteriaParam();
        param.addAttribute('bannerId','testing');
        criteriaData.addParam(param);
        assertTrue criteriaData.params.size() == 1
        criteriaData.tableAlias = "SPRIDEN_Record"
        criteriaData.tableBindingAttribute = "tableBindingAttribute"
        def result = greaterThanOperator.getQueryString(criteriaData)
        assertNotNull result
    }

    @Test
    void timeEnteredAttribute(){
        CriteriaParam param = new CriteriaParam();
        param.addAttribute('timeEntered',true);
        criteriaData.addParam(param);
        assertTrue criteriaData.params.size() == 1
        criteriaData.tableAlias = "SPRIDEN_Record"
        criteriaData.tableBindingAttribute = "tableBindingAttribute"
        def result = greaterThanOperator.getQueryString(criteriaData)
        assertNotNull result
    }

    @Test
    void testNoCriteriaData(){
        criteriaData.tableAlias = "SPRIDEN_Record"
        criteriaData.tableBindingAttribute = "tableBindingAttribute"
        def result = greaterThanOperator.getQueryString(criteriaData)
        assertTrue result == ""
    }

}

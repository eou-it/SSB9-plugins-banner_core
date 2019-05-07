/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.query.operators

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import net.hedtech.banner.query.criteria.CriteriaData
import net.hedtech.banner.query.criteria.CriteriaParam
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

@Integration
@Rollback
class StartsWithOperatorIntegrationTests extends BaseIntegrationTestCase {

    private StartsWithOperator startsWithOperator
    private CriteriaData criteriaData

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        startsWithOperator = new StartsWithOperator()
        criteriaData = new CriteriaData()
        super.setUp()
    }


    @After
    public void tearDown() {
        super.tearDown()
    }

    @Test
    void testingStartsWithOperator(){
        CriteriaParam param = new CriteriaParam();
        param.addAttribute('bannerId','testing');
        criteriaData.addParam(param);
        assertTrue criteriaData.params.size() == 1
        criteriaData.tableAlias = "SPRIDEN_Record"
        criteriaData.tableBindingAttribute = "tableBindingAttribute"
        def result = startsWithOperator.getQueryString(criteriaData)
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
        def result = startsWithOperator.getQueryString(criteriaData)
        assertNotNull result
    }

    @Test
    void testNoCriteriaData(){
        criteriaData.tableAlias = "SPRIDEN_Record"
        criteriaData.tableBindingAttribute = "tableBindingAttribute"
        def result = startsWithOperator.getQueryString(criteriaData)
        assertTrue result == ""
    }

}

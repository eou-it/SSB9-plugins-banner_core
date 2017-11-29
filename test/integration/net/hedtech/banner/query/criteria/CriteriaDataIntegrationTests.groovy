/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.query.criteria

import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

class CriteriaDataIntegrationTests extends BaseIntegrationTestCase {

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
    public void addCriteriaParam(){
        CriteriaParam param = new CriteriaParam();
        param.addAttribute('bannerId','testing')
        criteriaData.addParam(param)
        assertTrue criteriaData.params.size() == 1
        assertTrue criteriaData.params[0].additionalAttributes['bannerId'] == 'testing'
    }

    @Test
    public void addCriteriaParamList(){
        CriteriaParam param1 = new CriteriaParam();
        param1.addAttribute('bannerId','testing');
        CriteriaParam param2 = new CriteriaParam();
        param2.addAttribute('bannerId1','testing1');
        List<CriteriaParam> criteriaParamList = [param1, param2]
        criteriaData.addParams(criteriaParamList)
        assertTrue criteriaData.params.size() == 2
        assertTrue criteriaData.params[0].additionalAttributes['bannerId'] == 'testing'
    }


}

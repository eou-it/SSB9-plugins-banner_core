/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.query.criteria

import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

class CriteriaParamUtilTests extends BaseIntegrationTestCase {

    private CriteriaParamUtil criteriaParamUtil

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        criteriaParamUtil = new CriteriaParamUtil()
        super.setUp()
    }


    @After
    public void tearDown() {
        super.tearDown()
    }

    @Test
    public  void testGetValue(){
        CriteriaParam param = new CriteriaParam();
        criteriaParamUtil.setValue(param, "firstName")
        assertTrue param.data == "firstName"
        def result = criteriaParamUtil.getValue(param)
        assertTrue result == "firstName"
        def obj = [:]
        obj.bannerId = "Testing"
        def res = criteriaParamUtil.getValue(obj)
        assertTrue res['bannerId'] == "Testing"
    }


}

/*******************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

package net.hedtech.banner.security

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Intergration test cases for Url Based Forms Identifier
 */
@Integration
@Rollback

class UrlBasedFormsIdentifierIntegrationTests extends BaseIntegrationTestCase {

    private UrlBasedFormsIdentifier urlBasedFormsIdentifier
    private List<String> apiList
    private Map formControllerMap
    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        urlBasedFormsIdentifier = new UrlBasedFormsIdentifier()
        formControllerMap = [
                'first': ['FIRST'],
                'second': ['SECOND'],
                'thirdandfourth': ['THIRD', 'FOURTH'],
        ]
        apiList = ["api","qapi"]
        super.setUp()
    }

    @After
    public void tearDown() {
        super.tearDown()
    }


    @Test
    public void testTokenForRequestUrl() {
        String requestUrl = "/menu?pageName=first"
        List<String> determinedForms = urlBasedFormsIdentifier.getFormsFor(requestUrl,apiList,formControllerMap,"first")
        assertTrue determinedForms.size() > 0
        assertTrue determinedForms[0] == "FIRST"
    }

    @Test
    public void testRequestUrlWithZul() {
        String requestUrl = "/banner.zul?page=second"
        List<String> determinedForms = urlBasedFormsIdentifier.getFormsFor(requestUrl,apiList,formControllerMap,"second")
        assertTrue determinedForms.size() > 0
        assertTrue determinedForms[0] == "SECOND"
    }

    @Test
    public void testRequestUrlIndex() {
        String requestUrl = "/api/thirdandfourth"
        List<String> determinedForms = urlBasedFormsIdentifier.getFormsFor(requestUrl,apiList,formControllerMap,"first")
        assertTrue determinedForms.size() == 2
        assertTrue determinedForms[0] == "THIRD"
        assertTrue determinedForms[1] == "FOURTH"
    }

    @Test
    public void testRequestUrlNoPageName() {
        String requestUrl = "/myMepCode/api"
        List<String> determinedForms = urlBasedFormsIdentifier.getFormsFor(requestUrl,apiList,formControllerMap,null)
        assertTrue determinedForms.size() == 0
    }


}

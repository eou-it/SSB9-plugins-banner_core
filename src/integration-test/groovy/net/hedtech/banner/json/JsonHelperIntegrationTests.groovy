/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.json

import net.hedtech.banner.testing.BaseIntegrationTestCase
//import org.codehaus.groovy.grails.web.json.JSONObject
import grails.converters.JSON
import org.grails.web.json.JSONObject
import org.junit.After
import org.junit.Before
import org.junit.Test
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration

@Integration
@Rollback
class JsonHelperIntegrationTests extends BaseIntegrationTestCase {

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
    }

    @After
    public void tearDown() {
        super.tearDown()
    }

    @Test
    public void replaceJSONObjectNULLSuccess() {
        //def jsonObject = new JSONObject().put("Entity", JSON.parse('{ "a": null }').a);
        def jsonObject = JSON.parse('{ "Entity": null }')
        jsonObject.put("Name", "Value")
        try {
            JsonHelper.replaceJSONObjectNULL(jsonObject)
            assertTrue(true)
        } catch (Exception e) {
            assertTrue(false)
        }

    }


}

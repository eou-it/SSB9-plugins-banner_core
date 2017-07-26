/*******************************************************************************
 Copyright 2015- 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner

import net.hedtech.banner.testing.BaseIntegrationTestCase
import net.hedtech.banner.SpringContextUtils
import org.junit.After
import org.junit.Before
import org.junit.Test


class SpringContextUtilsIntegrationTests extends BaseIntegrationTestCase {


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
    void getApplicationContext(){
        def appContext = SpringContextUtils.getApplicationContext()
        assertNotNull appContext
    }

    @Test
    void getGrailsApplication(){
        def grailsApp = SpringContextUtils.getGrailsApplication()
        assertNotNull grailsApp
    }

    @Test
    void getGrailsApplicationClassLoader(){
        def grailsAppClass = SpringContextUtils.getGrailsApplicationClassLoader()
        assertNotNull grailsAppClass
    }

}

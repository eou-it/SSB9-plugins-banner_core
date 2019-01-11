/*******************************************************************************
Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
*******************************************************************************/ 
package net.hedtech.banner.service

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.Before
import org.junit.After
import org.junit.Test
import org.springframework.web.context.request.RequestContextHolder


@Integration
@Rollback
class DefaultLoaderServiceIntegrationTests extends BaseIntegrationTestCase {

    def defaultLoaderService

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
    void testDefaultDataLoad(){
        defaultLoaderService.loadDefault('grails_user')
        assertNotNull( RequestContextHolder.currentRequestAttributes().request.session.getAttribute("DEFAULTS") )
    }

}

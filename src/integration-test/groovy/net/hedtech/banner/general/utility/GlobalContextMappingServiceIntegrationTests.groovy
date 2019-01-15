/*******************************************************************************
Copyright 2015-2016 Ellucian Company L.P. and its affiliates.
*******************************************************************************/
package net.hedtech.banner.general.utility

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import static groovy.test.GroovyAssert.shouldFail

/**
 * Tests that the GlobalContextMappingService is working as expected.
 *
 * Created by arunu on 10/24/2016.
 */
@Integration
@Rollback
class GlobalContextMappingServiceIntegrationTests extends BaseIntegrationTestCase {

    def globalContextMappingService

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
    public void testGetGlobalNameByContext () {
        def globalName = globalContextMappingService.getGlobalNameByContext('table')
        assertNotNull(globalName)
        assertEquals('global.table.lookup', globalName)
        // Fail case
        def sf = globalContextMappingService.sessionFactory
        globalContextMappingService.sessionFactory = null
        shouldFail {
            globalContextMappingService.getGlobalNameByContext('table')
        }

        globalContextMappingService.sessionFactory = sf
    }

    @Test
    public void testGetContextByGlobalName () {
        def context = globalContextMappingService.getContextByGlobalName('global.table.lookup')
        assertNotNull(context)
        assertEquals('table', context)
        // Fail case
        def sf = globalContextMappingService.sessionFactory
        globalContextMappingService.sessionFactory = null
        shouldFail {
            globalContextMappingService.getContextByGlobalName('global.table.lookup')
        }

        globalContextMappingService.sessionFactory = sf
    }
}

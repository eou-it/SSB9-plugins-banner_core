/*******************************************************************************
 Copyright 2009-2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.configuration

import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * ConfigInstanceIntegrationTest.
 */
class ConfigInstanceIntegrationTest extends BaseIntegrationTestCase {

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
    public void testFindAll() {
        ConfigInstance configInstance = getConfigInstance()
        configInstance.save(failOnError: true, flush: true)

        //Save
        def list = configInstance.findAll()
        assert (list.size() > 0)
        assert (list.getAt(0).userId == 'TEST_USER')

        //Update
        configInstance.setUserId('NEW_USER')
        configInstance.save(failOnError: true, flush: true)
        list = configInstance.findAll()
        assert (list.size() > 0)
        assert (list.getAt(0).userId == 'NEW_USER')

        //Delete
        configInstance.delete()
        list = configInstance.findAll()
        assert (list.size() >= 0)
    }

    private ConfigInstance getConfigInstance() {
        ConfigInstance configInstance = new ConfigInstance(
                version: 0,
                id: 1,
                activityDate: new Date(),
                env: 1,
                gubapplAppId: 1,
                url: 'TEST_URL',
                userId: 'TEST_USER'
        )
        return configInstance
    }

}

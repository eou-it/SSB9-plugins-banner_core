/*******************************************************************************
 Copyright 2009-2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.configuration

import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * ConfigUserPreferenceIntegrationTest.
 */
class ConfigUserPreferenceIntegrationTest extends BaseIntegrationTestCase {

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
        ConfigUserPreference configUserPreference = getConfigUserPreference()
        configUserPreference.save(failOnError: true, flush: true)

        //Save
        def list = configUserPreference.findAll()
        assert (list.size() > 0)
        assert (list.getAt(0).configName == 'CONFIG_TEST')

        //Update
        configUserPreference.setConfigName('TEST_CONFIG')
        configUserPreference.save(failOnError: true, flush: true)
        list = configUserPreference.findAll()
        assert (list.size() > 0)
        assert (list.getAt(0).configName == 'TEST_CONFIG')

        //Delete
        configUserPreference.delete()
        list = configUserPreference.findAll()
        assert (list.size() >= 0)
    }

    private ConfigUserPreference getConfigUserPreference() {
        ConfigUserPreference configUserPreference = new ConfigUserPreference(
                gubapplAppId: 1,
                userId: 'TEST_USER',
                activityDate: new Date(),
                configName: 'CONFIG_TEST',
                configType: 'TYPE_TEST',
                configValue: 'TEST_VALUE',
                id: 1,
                pidm: 1,
                version: 0
        )
        return configUserPreference
    }
}

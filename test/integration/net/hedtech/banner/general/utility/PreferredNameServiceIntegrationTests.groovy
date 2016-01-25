/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

package net.hedtech.banner.general.utility

import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

class PreferredNameServiceIntegrationTests extends BaseIntegrationTestCase   {
    int pidm
    def usage
    def preferredNameService

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        pidm = 2086
        usage = "DEFAULT"
    }

    @After
    public void tearDown() {
        super.tearDown()
    }

    @Test
    public void getPreferredNameDefaultUsage(){
        String defaultName = preferredNameService.getName([pidm:pidm, usage:usage])
        println defaultName
        assertEquals "Phrebb Lindblom", defaultName
    }

    @Test
    public void getPreferredNameInvalidUsage(){
        usage = "junk"
        String defaultName = preferredNameService.getName([pidm:pidm, usage:usage])
        println defaultName
        assertEquals "Phrebb Lindblom", defaultName
    }

    @Test
    public void getUsageDefault(){
        String defaultName = preferredNameService.getUsage("W4","Header")
        println defaultName
        assertEquals "2000DEFAULT", defaultName
    }
}

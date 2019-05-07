/*******************************************************************************
 Copyright 2016-2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.security

import grails.util.Holders
import groovy.sql.Sql
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration

/**
 * Integration test for the AuthenticationProviderUtility class.
 **/
@Integration
@Rollback
class AuthenticationProviderUtilitySSBTests  extends BaseIntegrationTestCase{

    def dataSource
    def usage

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
    }

    @After
    public void tearDown() {
        super.tearDown();
    }

    @Test
    void testRetrievalOfRoleBasedTimeouts() {
        def timeouts = AuthenticationProviderUtility.retrieveRoleBasedTimeOuts( dataSource )
        assertTrue timeouts.size() > 0
    }

    @Test
    void testGetUserFullNameWithDisplayNameRuleWithDefaultUsage(){
        Holders?.config.ssbEnabled = true
        Holders?.config.ssbOracleUsersProxied = false

        Holders?.config?.productName ="Student"
        Holders?.config?.banner.applicationName ="testApp"

        def authResults = AuthenticationProviderUtility.getMappedUserForUdcId("DSTERLIN", dataSource)
        def  bannerPidm1 = authResults.pidm
        def fullName=AuthenticationProviderUtility.getUserFullName(bannerPidm1,authResults["name"],dataSource)

        assertEquals "Mr. Steve A Jorden", fullName
    }

    @Test
    void testGetUserFullNameWithDisplayNameRuleWithLFMIUsage(){
        Holders?.config.ssbEnabled = true
        Holders?.config.ssbOracleUsersProxied = false

        Holders?.config?.productName ="testApp_LFMI"
        Holders?.config?.banner.applicationName ="testApp_LFMI"

        def authResults = AuthenticationProviderUtility.getMappedUserForUdcId("DSTERLIN", dataSource )
        def bannerPidm1 = authResults.pidm
        def fullName=AuthenticationProviderUtility.getUserFullName(bannerPidm1,authResults["name"],dataSource)

        assertEquals "Jorden, Steve A.", fullName
    }

    @Test
    void testGetUserFullNameWithDisplayNameRuleWithoutUsage(){
        Holders?.config.ssbEnabled = true
        Holders?.config.ssbOracleUsersProxied = false

        Holders?.config?.productName ="testApp"
        Holders?.config?.banner.applicationName = "testApp"

        def authResults = AuthenticationProviderUtility.getMappedUserForUdcId("DSTERLIN", dataSource )
        def bannerPidm1 = authResults.pidm
        def fullName=AuthenticationProviderUtility.getUserFullName(bannerPidm1,authResults["name"],dataSource)

        assertEquals "Mr. Steve A Jorden", fullName

    }

    @Test
    void testGetUserFullNameWithDisplayNameRuleWithoutRule(){
        Holders?.config.ssbEnabled = true
        Holders?.config.ssbOracleUsersProxied = false

        Holders?.config?.productName ="testApp";
        Holders?.config?.banner.applicationName ="testApp"

        def bannerUDCID1 = "DSTERLIN"
        def authResults = AuthenticationProviderUtility.getMappedUserForUdcId(bannerUDCID1, dataSource )
        def bannerPidm1 = authResults.pidm
        def fullName=AuthenticationProviderUtility.getUserFullName(bannerPidm1,authResults["name"],dataSource)

        assertEquals "Mr. Steve A Jorden", fullName

    }

    @Test
    void testGetMappedUserForUdcIdOracleNull() {
        Holders?.config.ssbEnabled = true
        Holders?.config.ssbOracleUsersProxied = false

        def bannerUDCID1 = "DSTERLIN"

        def authResults = AuthenticationProviderUtility.getMappedUserForUdcId(bannerUDCID1, dataSource )

        assertNotNull(authResults)
        assertNull(authResults["oracleUserName"])

    }

    @Test
    void testGetMappedUserForUdcIdOracleNotNull() {
        Holders?.config.ssbEnabled = true
        Holders?.config.ssbOracleUsersProxied = false

        def bannerUDCID1 = "271"
        def authResults = AuthenticationProviderUtility.getMappedUserForUdcId(bannerUDCID1, dataSource )

        assertNotNull(authResults)
        assertNotNull(authResults["oracleUserName"])
    }

}

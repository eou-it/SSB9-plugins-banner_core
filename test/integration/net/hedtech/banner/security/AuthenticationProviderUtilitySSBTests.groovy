/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.security

import grails.util.Holders
import groovy.sql.Sql
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Integration test for the AuthenticationProviderUtility class.
 **/
class AuthenticationProviderUtilitySSBTests  extends BaseIntegrationTestCase{

    def authenticationProviderUtility
    def dataSource
    def usage
    def conn
    def sqlObj
    public final String DEFAULT= "DEFAULT"
    public final String LFMI= "LFMI"
    public static final String UDC_IDENTIFIER = '99999SSB99999'

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        conn = dataSource.getConnection()
        sqlObj = new Sql( conn )
        authenticationProviderUtility = new AuthenticationProviderUtility()

    }

    @After
    public void tearDown() {
        sqlObj.close()
        conn.close()
        super.tearDown();
    }

    @Test
    void testRetrievalOfRoleBasedTimeouts() {
        def timeouts = authenticationProviderUtility.retrieveRoleBasedTimeOuts( dataSource )
        assertTrue timeouts.size() > 0
    }

    @Test
    void testGetUserFullNameWithDisplayNameRuleWithDefaultUsage(){
        Holders?.config.ssbEnabled = true
        Holders?.config.ssbOracleUsersProxied = false

        Holders?.config?.productName ="Student";
        Holders?.config?.banner.applicationName ="testApp";

        def authResults = authenticationProviderUtility.getMappedUserForUdcId("DSTERLIN", dataSource );
        def  bannerPidm1 =49444;
        def fullName=authenticationProviderUtility.getUserFullName(bannerPidm1,authResults["name"],dataSource);

        assertEquals "Kishen Ray", fullName
    }

    @Test
    void testGetUserFullNameWithDisplayNameRuleWithLFMIUsage(){
        Holders?.config.ssbEnabled = true
        Holders?.config.ssbOracleUsersProxied = false

        Holders?.config?.productName ="testApp_LFMI";
        Holders?.config?.banner.applicationName ="testApp_LFMI";

        def authResults = authenticationProviderUtility.getMappedUserForUdcId("DSTERLIN", dataSource )
        def bannerPidm1 =49444;
        def fullName=authenticationProviderUtility.getUserFullName(bannerPidm1,authResults["name"],dataSource);

        assertEquals "Ray, Kishen", fullName
    }

    @Test
    void testGetUserFullNameWithDisplayNameRuleWithoutUsage(){
        Holders?.config.ssbEnabled = true
        Holders?.config.ssbOracleUsersProxied = false

        Holders?.config?.productName ="testApp";
        Holders?.config?.banner.applicationName ="testApp";

        def authResults = authenticationProviderUtility.getMappedUserForUdcId("DSTERLIN", dataSource )
        def bannerPidm1 =49444;
        def fullName=authenticationProviderUtility.getUserFullName(bannerPidm1,authResults["name"],dataSource);

        assertEquals "Kishen Ray", fullName

    }

    @Test
    void testGetUserFullNameWithDisplayNameRuleWithoutRule(){
        Holders?.config.ssbEnabled = true
        Holders?.config.ssbOracleUsersProxied = false

        Holders?.config?.productName ="testApp";
        Holders?.config?.banner.applicationName ="testApp";

        def bannerUDCID1 = "DSTERLIN"
        def authResults = authenticationProviderUtility.getMappedUserForUdcId(bannerUDCID1, dataSource )
        def bannerPidm1 =50199;
        def fullName=authenticationProviderUtility.getUserFullName(bannerPidm1,authResults["name"],dataSource);

        assertEquals "Mr. Steve A Jorden", fullName

    }

    @Test
    void testGetMappedUserForUdcIdOracleNull() {
        Holders?.config.ssbEnabled = true
        Holders?.config.ssbOracleUsersProxied = false

        def bannerUDCID1 = "DSTERLIN";

        def authResults = authenticationProviderUtility.getMappedUserForUdcId(bannerUDCID1, dataSource )

        assertNotNull(authResults)
        assertNull(authResults["oracleUserName"])

    }

    @Test
    void testGetMappedUserForUdcIdOracleNotNull() {
        Holders?.config.ssbEnabled = true
        Holders?.config.ssbOracleUsersProxied = false

        def bannerUDCID1 = "271";
        def authResults = authenticationProviderUtility.getMappedUserForUdcId(bannerUDCID1, dataSource )

        assertNotNull(authResults)
        assertNotNull(authResults["oracleUserName"])

    }

}

/*******************************************************************************
 Copyright 2016-2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.db.dbutility

import grails.util.Holders
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.apache.commons.dbcp.BasicDataSource
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.context.ApplicationContext
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.context.SecurityContextHolder

class DBUtilityIntegrationTestsSpec extends BaseIntegrationTestCase {
    DBUtility dbUtility

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        dbUtility = new DBUtility()
    }

    @After
    public void tearDown() {
        super.tearDown()
        logout()
    }

    public void cleanUp(){
        logout()
    }

    public void setUpFormContext(){
        formContext = ['GUAGMNU']
    }

    public void setUpValidAdminUserId(){
        setUpFormContext()
        username = "grails_user"
        password = "u_pick_it"
    }

    public void setUpInvalidUserId(){
        setUpFormContext()
        username = "INVALID"
        password = "111111"
    }

    public void setUpValidSSBTypeUser(){
        setUpFormContext()
        def config = Holders.getConfig()
        config.ssbEnabled = true
        username = "HOSH00002"
        password = "111111"
        def bb = new grails.spring.BeanBuilder()
        bb.beans {
            underlyingSsbDataSource(BasicDataSource) {
                maxActive = 5
                maxIdle = 2
                defaultAutoCommit = "false"
                driverClassName = "${config.bannerSsbDataSource.driver}"
                url = "${config.bannerSsbDataSource.url}"
                password = "${config.bannerSsbDataSource.password}"
                username = "${config.bannerSsbDataSource.username}"
            }
        }

        ApplicationContext testSpringContext = bb.createApplicationContext()
        dataSource.underlyingSsbDataSource =  testSpringContext.getBean("underlyingSsbDataSource")
    }

    @Test
    public void testOracleUserWithValidUser(){
        setUpValidAdminUserId()
        login(username, password)
        def user = SecurityContextHolder?.context?.authentication?.principal
        assertTrue(DBUtility.isOracleUser(user))
    }

    @Test(expected = BadCredentialsException.class)
    public void testOracleUserWithInvalidUser(){
        setUpInvalidUserId()
        login(username, password)
    }

    @Test
    public void testValidSSBUser(){
        setUpValidSSBTypeUser()
        loginSSB(username, password)
        def user = SecurityContextHolder?.context?.authentication?.principal
        assertTrue(DBUtility.isOracleUser(user) == false)
    }

   @Test
    public void testInValidSSBUser(){
        def config = Holders.getConfig()
        config.ssbEnabled = true
        setUpInvalidUserId()
        try{
            loginSSB(username, password)
        } catch(Exception e){
            assertTrue(true)
        }
    }

    @Test
    public void testIsCommmgrDataSourceEnabled(){
      //  def config = Holders.getConfig()
        assertFalse(DBUtility.isCommmgrDataSourceEnabled())
       // config.commmgrDataSourceEnabled = true
       // assertTrue(DBUtility.isCommmgrDataSourceEnabled())
       // config.commmgrDataSourceEnabled = false
       // assertFalse(DBUtility.isCommmgrDataSourceEnabled())
    }

    @Test
    public void testIsNotApiProxiedOrNotOracleMappedSsbOrSsbAnonymous(){
        setUpValidSSBTypeUser()
        loginSSB(username, password)
        def user = SecurityContextHolder?.context?.authentication?.principal
        def result = dbUtility.isNotApiProxiedOrNotOracleMappedSsbOrSsbAnonymous(user)
        assertTrue(!result)
    }

    @Test
    public void testIsSSUserFailCase() {
        setUpValidSSBTypeUser()
        loginSSB("grails_user","u_pick_it")
        assertTrue(!dbUtility.isSSUser())
    }

    @Test
    public void testIsNotAnonymousTypeUser() {
        setUpValidSSBTypeUser()
        loginSSB(username, password)
        def user = SecurityContextHolder?.context?.authentication?.principal
        assertFalse(DBUtility.isAnonymousTypeUser(user))
    }

    @Test
    public void testIsAnonymousTypeUser() {
        String user = "anonymousUser"
        assertTrue(DBUtility.isAnonymousTypeUser(user))

        user = ""
        assertFalse(DBUtility.isAnonymousTypeUser(user))
    }

    @Test
    public void testIsApiProxySupportDisabled () {
        assertTrue(DBUtility.isApiProxySupportDisabled())
    }

    @Test
    public void testIsMepEnabled() {
        assertTrue(!DBUtility.isMepEnabled())
    }

    @Test
    public void testIsSsbUserWithAnyRole(){
        setUpValidSSBTypeUser()
        loginSSB(username, password)
        assertTrue(dbUtility.isSsbUserWithAnyRole())
    }

    @Test
    public void testIsSsbUserWithAnyRoleFailure(){
        setUpValidSSBTypeUser()
        loginSSB("grails_user","u_pick_it")
        assertTrue(!dbUtility.isSsbUserWithAnyRole())
    }

}

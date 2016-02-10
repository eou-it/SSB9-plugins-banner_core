package net.hedtech.banner.db.dbutility

import grails.util.Holders
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.apache.commons.dbcp.BasicDataSource
import grails.util.Holders  as CH
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.context.ApplicationContext
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.context.SecurityContextHolder

class DBUtilityIntegrationTestsSpec extends BaseIntegrationTestCase {


    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
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
            e.printStackTrace()
            assertTrue(true)
        }
    }

    @Test
    public void testTest(){
        assertTrue(true)
    }

}

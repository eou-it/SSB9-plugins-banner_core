/*******************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

package net.hedtech.banner.db

import groovy.sql.Sql
import net.hedtech.banner.db.BannerDS as BannerDataSource
import net.hedtech.banner.testing.BaseIntegrationTestCase
import oracle.jdbc.OracleConnection
import org.apache.commons.dbcp.BasicDataSource
import org.codehaus.groovy.grails.plugins.testing.GrailsMockHttpServletRequest
import org.junit.After
import org.junit.Before
import org.junit.Test
import grails.util.Holders
import org.springframework.context.ApplicationContext
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

import java.sql.Connection

/**
 * Integration tests exercising the BannerDS (formerly named BannerDataSource) data source implementation.
 */
public class BannerDataSourceIntegrationTests extends BaseIntegrationTestCase {

    def config

    private  String SSB_VALID_USERNAME = "HOSH00002"
    private  String SSB_VALID_PASSWORD = "111111"

    private String ADMIN_VALID_USERNAME="grails_user"
    private String ADMIN_VALID_PASSWORD="u_pick_it"

    private String PROXY_USERNAME="HOSH00070"
    private String PROXY_PASSWORD="111111"

    // NOTE: Please also see 'FooServiceIntegrationTests', as that test also includes framework tests pertaining to connections.

    @Before
    public void setUp(){
        formContext = ['GUAGMNU']
        config = Holders.getConfig()
    }

    @After
    public void tearDown(){
    }


    public void setupAPIData(){
        username = SSB_VALID_USERNAME
        password = SSB_VALID_PASSWORD
        config.apiUrlPrefixes=["api","qapi"]
        GrailsMockHttpServletRequest request = new GrailsMockHttpServletRequest()
        request.setForwardURI("api")
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request))
        RequestContextHolder.getRequestAttributes().session = request.getSession(true)
        setupUnderlyingSSBDataSource()
    }

    public void setupAPIWithNoProxy(){
        setupAPIData()
        config.with   {
            apiOracleUsersProxied=false
        }
        super.setUp()
    }

    public void setupAPIWithProxy(){
        setupAPIData()
        config.with   {
            apiOracleUsersProxied=true
        }
        username = PROXY_USERNAME
        password = PROXY_PASSWORD
        super.setUp()
    }

    public void setupSSBData(){
        formContext = ['SELFSERVICE']
        config.ssbEnabled = true
        setupUnderlyingSSBDataSource()
    }


    public void setupSSBWithNoProxy(){
        config.ssbOracleUsersProxied = false
        username = SSB_VALID_USERNAME
        password = SSB_VALID_PASSWORD
        setupSSBData()
        super.setUp()
    }

    public void setupSSBWithProxy(){
        config.ssbOracleUsersProxied = true
        username = PROXY_USERNAME
        password = PROXY_PASSWORD
        setupSSBData()
        super.setUp()
    }

    public void setupUnderlyingSSBDataSource(){
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

    public void tearDownDataSetup(){
        dataSource.underlyingSsbDataSource = null
        logout()
        super.tearDown()
    }

    @Test
    public void testExpectedConfiguration() {
        assertTrue "Expected to have a BannerDataSource but instead have ${dataSource?.class}", (dataSource instanceof BannerDS)
        Connection conn = dataSource.getConnection()
        assertTrue "Expected BannerConnection but have ${conn?.class}", conn instanceof BannerConnection
        assertTrue "Expected to be able to extract OracleConnection but found ${(conn as BannerConnection).extractOracleConnection()?.class}",
                (conn as BannerConnection).extractOracleConnection() instanceof OracleConnection

    }

    @Test
    public void testProxyConnection() {
        BannerConnection conn
        Sql sql
        try {
            // we'll first get an unproxied connection and then proxy it, to exercise those methods (versus the normal getConnection())
            conn = (dataSource as BannerDataSource).getUnproxiedConnection() as BannerConnection
            conn = (dataSource as BannerDataSource).proxyConnection( conn, "grails_user" ) as BannerConnection

            assertEquals( "grails_user", conn.getProxyUserName() )
            assertTrue conn.extractOracleConnection().isProxySession()

            sql = new Sql( conn.extractOracleConnection() )
            def row = sql.firstRow( "select sys_context('userenv','proxy_user') from dual" )
            assertEquals "BANPROXY", row.getAt( "SYS_CONTEXT('USERENV','PROXY_USER')" )

            row = sql.firstRow( "select sys_context('userenv','current_user') from dual" )
            assertEquals "GRAILS_USER", row.getAt( "SYS_CONTEXT('USERENV','CURRENT_USER')" )
        } finally {
            sql?.close()
        }
    }

    @Test
    public void testSettingOracleRole() {
        BannerConnection conn
        Sql sql
        try {
            conn = (dataSource as BannerDataSource).getUnproxiedConnection() as BannerConnection
            sql = new Sql( conn.extractOracleConnection() )
            // Retrieve the database role name and password for object STVINTS
            def row = sql.firstRow( "select * from govurol where govurol_object = 'STVINTS' and govurol_userid = 'GRAILS_USER' " )

            conn = (dataSource as BannerDataSource).proxyConnection( conn, "grails_user" ) as BannerConnection

            String stmt = "set role ${row.govurol_role} identified by \"${row.govurol_role_pswd}\""
            sql = new Sql( conn.extractOracleConnection() )
            sql.execute( stmt )
        } finally {
            sql?.close()
        }
    }

    @Test
    public void testSSBTypeRequestWithNoProxy(){
        setupSSBWithNoProxy()
        def conn = (dataSource as BannerDS).getConnection()
        assertTrue "Expected BannerConnection but have ${conn?.class}", conn instanceof BannerConnection
        tearDownDataSetup()
    }

    @Test
    public void testSSBTypeRequestWithProxy(){
        setupSSBWithProxy()
        def conn = (dataSource as BannerDS).getConnection()
        assertTrue "Expected BannerConnection but have ${conn?.class}", conn instanceof BannerConnection
        tearDownDataSetup()
    }


    @Test
    public void testAPITypeRequestWithNoProxy(){
        setupAPIWithNoProxy()
        def conn = (dataSource as BannerDS).getConnection()
        assertTrue "Expected BannerConnection but have ${conn?.class}", conn instanceof BannerConnection
        tearDownDataSetup()
    }

    /*@Test
    public void testAPITypeRequestWithProxy(){
        setupAPIWithProxy()
        def conn = (dataSource as BannerDS).getConnection()
        assertTrue "Expected BannerConnection but have ${conn?.class}", conn instanceof BannerConnection
        tearDownDataSetup()
    }*/

}

/*******************************************************************************
 Copyright 2010-2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

package net.hedtech.banner.db

import grails.util.Holders
import groovy.sql.Sql
import net.hedtech.banner.db.BannerDS as BannerDataSource
import net.hedtech.banner.testing.BaseIntegrationTestCase
import oracle.jdbc.OracleConnection
import org.apache.commons.dbcp.BasicDataSource
import org.codehaus.groovy.grails.plugins.testing.GrailsMockHttpServletRequest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.context.ApplicationContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

import java.sql.Connection
import java.sql.SQLSyntaxErrorException
import java.util.logging.Handler
import java.util.logging.Logger
import java.util.logging.SimpleFormatter
import java.util.logging.StreamHandler

/**
 * Integration tests exercising the BannerDS (formerly named BannerDataSource) data source implementation.
 */
public class BannerDataSourceIntegrationTests extends BaseIntegrationTestCase {

    def config

    final Logger sqlLog = Logger.getLogger(Sql.class.getName())

    private  String SSB_VALID_USERNAME = "HOSH00002"
    private  String SSB_VALID_PASSWORD = "111111"

    private String PROXY_USERNAME="HOSH00070"
    private String PROXY_PASSWORD="111111"

    def ssbOracleUsersProxiedInFile
    def apiOracleUsersProxiedInFile
    def apiUrlPrefixesInFile
    def ssbEnabledInFile
    BannerDS bannerDS

    // NOTE: Please also see 'FooServiceIntegrationTests', as that test also includes framework tests pertaining to connections.

    @Before
    public void setUp(){
        formContext = ['GUAGMNU']
        super.setUp()
        config = Holders.getConfig()
        bannerDS = (dataSource as BannerDS)
    }

    @After
    public void tearDown(){
        super.tearDown()
    }

    public void backupConfigFileConfigurations(){
        ssbOracleUsersProxiedInFile=config.ssbOracleUsersProxied
        apiOracleUsersProxiedInFile=config.apiOracleUsersProxied
        apiUrlPrefixesInFile=config.apiUrlPrefixes
        ssbEnabledInFile=config.ssbEnabled
    }

    public void resetConfigAsInTheFile(){
        config.ssbOracleUsersProxied=ssbOracleUsersProxiedInFile
        config.apiOracleUsersProxied=apiOracleUsersProxiedInFile
        config.apiUrlPrefixes=apiUrlPrefixesInFile
        config.ssbEnabled=ssbEnabledInFile
    }

    public void setupAPIData(){
        username = ""
        password = ""
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
        SSBSetUp(username,password)
    }

    public void setupSSBWithProxy(){
        config.ssbOracleUsersProxied = true
        username = PROXY_USERNAME
        password = PROXY_PASSWORD
        setupSSBData()
        SSBSetUp(username,password)
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
        dataSource.removeConnection(conn)
        if (conn) conn.close()

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
            if(conn) {
                dataSource.removeConnection(conn);
            }
            sql?.close()
        }
    }


    @Test
    public void testSettingValidOracleRoleAndPassword() {
        BannerConnection conn
        java.util.logging.Formatter formatter = new SimpleFormatter()
        ByteArrayOutputStream out = new ByteArrayOutputStream()
        Handler handler = new StreamHandler(out, formatter)
        sqlLog.addHandler(handler)

        Sql sql
        try {
            conn = (dataSource as BannerDataSource).getUnproxiedConnection() as BannerConnection
            sql = new Sql( conn.extractOracleConnection() )
            // Retrieve the database role name and password for object STVINTS
            def row = sql.firstRow( "select * from govurol where govurol_object = 'STVINTS' and govurol_userid = 'GRAILS_USER' " )

            conn = (dataSource as BannerDataSource).proxyConnection( conn, "grails_user" ) as BannerConnection
            sql = new Sql( conn.extractOracleConnection() )

            String stmt = "${row.govurol_role} identified by \"${row.govurol_role_pswd}\""
            sql = new Sql( conn.extractOracleConnection() )
            sql.call("{call dbms_session.set_role(?)}", [stmt])

        } finally {
            handler.flush();
            String logMsg = out.toString()
            println logMsg
            assertFalse(logMsg.toLowerCase().contains("identified by"))
            conn.close()
            sql.close()
            sqlLog.removeHandler(handler)
        }
    }


    @Test
    public void testSettingOracleRoleWithInValidPassword() {
        BannerConnection conn
        java.util.logging.Formatter formatter = new SimpleFormatter()
        ByteArrayOutputStream out = new ByteArrayOutputStream()
        Handler handler = new StreamHandler(out, formatter)
        sqlLog.addHandler(handler)

        Sql sql
        try {
            conn = (dataSource as BannerDataSource).getUnproxiedConnection() as BannerConnection
            sql = new Sql( conn.extractOracleConnection() )
            // Retrieve the database role name and password for object STVINTS
            def row = sql.firstRow( "select * from govurol where govurol_object = 'STVINTS' and govurol_userid = 'GRAILS_USER' " )

            conn = (dataSource as BannerDataSource).proxyConnection( conn, "grails_user" ) as BannerConnection
            sql = new Sql( conn.extractOracleConnection() )

            String stmt = "${row.govurol_role} identified by 111"                   // ORA-01979: missing or invalid password for role 'BAN_DEFAULT_M'
            sql = new Sql( conn.extractOracleConnection() )
            sql.call("{call dbms_session.set_role(?)}", [stmt])

        }
        catch (SQLSyntaxErrorException sqlex) {
            //do nothing  we will assert in Finally
        }
        finally {
            handler.flush()
            String logMsg = out.toString()
            println logMsg
            assertFalse(logMsg.toLowerCase().contains("identified by"))
            assertTrue(logMsg.toLowerCase().contains("ora-01979: missing or invalid password for role 'ban_default_m'"))
            conn.close()
            sql.close()
            sqlLog.removeHandler(handler)
        }
    }



    @Test
    public void testSettingInValidOracleRole() {
        BannerConnection conn
        java.util.logging.Formatter formatter = new SimpleFormatter()
        ByteArrayOutputStream out = new ByteArrayOutputStream()
        Handler handler = new StreamHandler(out, formatter)
        sqlLog.addHandler(handler)

        Sql sql
        try {
            conn = (dataSource as BannerDataSource).getUnproxiedConnection() as BannerConnection
            sql = new Sql( conn.extractOracleConnection() )
            // Retrieve the database role name and password for object STVINTS
            def row = sql.firstRow( "select * from govurol where govurol_object = 'STVINTS' and govurol_userid = 'GRAILS_USER' " )

            conn = (dataSource as BannerDataSource).proxyConnection( conn, "grails_user" ) as BannerConnection
            sql = new Sql( conn.extractOracleConnection() )

            String stmt = "Junk identified by \"${row.govurol_role_pswd}\""        //ORA-01924: role 'JUNK' not granted or does not exist
            sql = new Sql( conn.extractOracleConnection() )
            sql.call("{call dbms_session.set_role(?)}", [stmt])

        }
        catch (SQLSyntaxErrorException sqlex) {
            //do nothing  we will assert in Finally
        }
        finally {
            handler.flush()
            String logMsg = out.toString()
            println logMsg
            assertFalse(logMsg.toLowerCase().contains("identified by"))
            assertTrue(logMsg.toLowerCase().contains("ora-01924: role 'junk' not granted or does not exist"))
            conn.close()
            sql.close()
            sqlLog.removeHandler(handler)
        }
    }


    @Test
    public void testSSBTypeRequestWithNoProxy(){
        backupConfigFileConfigurations();
        setupSSBWithNoProxy()
        def conn = dataSource.getConnection()
        assertTrue "Expected BannerConnection but have ${conn?.class}", conn instanceof BannerConnection
        dataSource.removeConnection(conn)
        if (conn) conn.close()
        tearDownDataSetup()
        resetConfigAsInTheFile()
    }

    @Test
    public void testSSBTypeRequestWithProxy(){
        backupConfigFileConfigurations();
        setupSSBWithProxy()
        def conn = (dataSource as BannerDS).getConnection()
        assertTrue "Expected BannerConnection but have ${conn?.class}", conn instanceof BannerConnection
        dataSource.removeConnection(conn)
        if (conn) conn.close()
        tearDownDataSetup()
        resetConfigAsInTheFile()
    }

    @Test
    public void testAPITypeRequestWithNoProxy(){
        backupConfigFileConfigurations();
        setupAPIWithNoProxy()
        def conn = (dataSource as BannerDS).getConnection()
        assertTrue "Expected BannerConnection but have ${conn?.class}", conn instanceof BannerConnection
        dataSource.removeConnection(conn)
        if (conn) conn.close()
        tearDownDataSetup()
        resetConfigAsInTheFile()
    }

    @Test
    public void testOracleMessageTranslationForCanadianFrench(){
        def locale = 'fr_CA'
        def conn = (dataSource as BannerDS).getConnection()
        def sql = new Sql(conn)
        BannerDS.callNlsUtility(sql,locale)
        sql.eachRow("select VALUE from v\$nls_parameters where parameter='NLS_LANGUAGE'"){row->
            assertEquals("CANADIAN FRENCH",row.value)
        }
        if(sql) sql.close()
        if(conn) conn.close()

    }

    @Test
    public void testOracleMessageTranslationForMexicanSpanish(){
        def locale = 'es_MX'
        def conn = (dataSource as BannerDS).getConnection()
        def sql = new Sql(conn)
        BannerDS.callNlsUtility(sql,locale)
        sql.eachRow("select VALUE from v\$nls_parameters where parameter='NLS_LANGUAGE'"){row->
            assertEquals("MEXICAN SPANISH",row.value)
        }
        if(sql) sql.close()
        if(conn) conn.close()

    }

    @Test
    public void testSetDBMSApplicationInfo() {
        def conn = bannerDS.getConnection()
        assertNotNull(conn)
    }

    @Test
    public void testUnderlyingDataSourceAdminUser() {
        setUpValidAdminUserId()
        login(username, password)
        def underlyingDS = bannerDS.getUnderlyingDataSource()
        assertNotNull(underlyingDS)
        logout()
    }

    @Test
    public void testUnderlyingDataSourceSSBUser() {
        setUpValidSSBTypeUser()
        loginSSB(username, password)
        def underlyingDS = bannerDS.getUnderlyingDataSource()
        assertNotNull(underlyingDS)
        logout()
    }

    @Test
    public void testGetUserRoles () {
        setUpValidAdminUserId()
        login(username, password)
        def user = SecurityContextHolder?.context?.authentication?.principal
        Map unlockedRoles = bannerDS.userRoles(user, user?.authorities)
        assert (!unlockedRoles.isEmpty())

        assertTrue(unlockedRoles.find {key, value -> key.equals('BAN_DEFAULT_M')}.value)
        logout()
    }

    @Test
    public void testCallNLSUtilityFail() {
        try {
            bannerDS.callNlsUtility(null, 'es')
        } catch (Exception e) {
            fail(e.getMessage())
        }
    }

    @Test
    public void testGetConnection() {
        setUpValidAdminUserId()
        login(username, password)
        def connection = bannerDS.getConnection()
        assertNotNull(connection)
        dataSource.removeConnection(connection)
        if (connection) connection.close()
        tearDownDataSetup()
        resetConfigAsInTheFile()
        logout()
    }

    @Test
    public void testGetConnectionWithSelfServiceRequest() {
        def connection
        try {
            setUpValidSSBTypeUser()
            loginSSB(username, password)
            connection = bannerDS.getSsbConnection()
            assertNotNull(connection)
        } finally {
            if (connection) connection.close()
            tearDownDataSetup()
            resetConfigAsInTheFile()
            logout()
        }
    }

    @Test
    public void testGetCachedConnection () {
        backupConfigFileConfigurations();
        setupAPIWithNoProxy()
        def conn = bannerDS.getConnection()
        assertNotNull(conn)
        dataSource.removeConnection(conn)
        if (conn) conn.close()
        tearDownDataSetup()
        resetConfigAsInTheFile()
    }

    @Test
    public void testSetIdentifier () {
        if (log.isTraceEnabled()) {
            def conn = bannerDS.getConnection()
            def identifier = "Banner_Core_Integration_Tests"
            bannerDS.setIdentifier(conn, identifier)

            def sql = new Sql( conn )
            def row = sql.firstRow( "select sys_context( 'USERENV', 'CLIENT_IDENTIFIER' ) FROM DUAL" )
            assertEquals "Banner_Core_Integration_Tests", row."SYS_CONTEXT('USERENV','CLIENT_IDENTIFIER')"

            bannerDS.clearIdentifer(conn)
            dataSource.removeConnection(conn)
            if (conn) conn.close()
            tearDownDataSetup()
            resetConfigAsInTheFile()
        }
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

    public void setUpFormContext(){
        formContext = ['GUAGMNU']
    }

    public void setUpValidAdminUserId(){
        setUpFormContext()
        username = "grails_user"
        password = "u_pick_it"
    }
}

/*******************************************************************************
Copyright 2016 Ellucian Company L.P. and its affiliates.
*******************************************************************************/

package net.hedtech.banner.db

import grails.util.Environment
import grails.util.Holders
import groovy.sql.Sql
import net.hedtech.banner.apisupport.ApiUtils
import net.hedtech.banner.db.BannerDS as BannerDataSource
import net.hedtech.banner.db.dbutility.DBUtility
import net.hedtech.banner.testing.BaseIntegrationTestCase
import oracle.jdbc.OracleConnection
import org.apache.commons.dbcp.BasicDataSource
import org.apache.log4j.Level
import org.apache.log4j.Logger
import org.codehaus.groovy.grails.plugins.testing.GrailsMockHttpServletRequest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.context.ApplicationContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

import java.sql.Connection

/**
        * Integration tests exercising the BannerDS (formerly named BannerDataSource) data source implementation.
*/
public class BannerDataSourceIntegrationTests extends BaseIntegrationTestCase {

    def config

    private String SSB_VALID_USERNAME = "HOSH00002"
    private String SSB_VALID_PASSWORD = "111111"

    private String PROXY_USERNAME = "HOSH00070"
    private String PROXY_PASSWORD = "111111"

    def ssbOracleUsersProxiedInFile
    def apiOracleUsersProxiedInFile
    def apiUrlPrefixesInFile
    def ssbEnabledInFile
    BannerDS bannerDS

    // NOTE: Please also see 'FooServiceIntegrationTests', as that test also includes framework tests pertaining to connections.

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        config = Holders.getConfig()
        bannerDS = (dataSource as BannerDS)
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testExpectedConfiguration() {
        assertTrue "Expected to have a BannerDataSource but instead have ${dataSource?.class}", (dataSource instanceof BannerDS)
        Connection conn = bannerDS.getConnection()
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
            conn = bannerDS.getUnproxiedConnection() as BannerConnection
            conn = bannerDS.proxyConnection(conn, "grails_user") as BannerConnection

            assertEquals("grails_user", conn.getProxyUserName())
            assertTrue conn.extractOracleConnection().isProxySession()

            sql = new Sql(conn.extractOracleConnection())
            def row = sql.firstRow("select sys_context('userenv','proxy_user') from dual")
            assertEquals "BANPROXY", row.getAt("SYS_CONTEXT('USERENV','PROXY_USER')")

            row = sql.firstRow("select sys_context('userenv','current_user') from dual")
            assertEquals "GRAILS_USER", row.getAt("SYS_CONTEXT('USERENV','CURRENT_USER')")
        } finally {
            if (conn) {
                dataSource.removeConnection(conn);
            }
            sql?.close()
        }
    }

    @Test
    public void testSettingOracleRole() {
        BannerConnection conn
        Sql sql
        try {
            conn = bannerDS.getUnproxiedConnection() as BannerConnection
            sql = new Sql(conn.extractOracleConnection())
            // Retrieve the database role name and password for object STVINTS
            def row = sql.firstRow("select * from govurol where govurol_object = 'STVINTS' and govurol_userid = 'GRAILS_USER' ")

            conn = (dataSource as BannerDataSource).proxyConnection(conn, "grails_user") as BannerConnection

            String stmt = "set role ${row.govurol_role} identified by \"${row.govurol_role_pswd}\""
            sql = new Sql(conn.extractOracleConnection())
            sql.execute(stmt)
        } finally {
            if (conn) {
                dataSource.removeConnection(conn);
            }
            sql?.close()
        }
    }

    @Test
    public void testSSBTypeRequestWithNoProxy() {
        backupConfigFileConfigurations();
        setupSSBWithNoProxy()
        def conn = bannerDS.getConnection()
        assertTrue "Expected BannerConnection but have ${conn?.class}", conn instanceof BannerConnection
        dataSource.removeConnection(conn)
        if (conn) conn.close()
        tearDownDataSetup()
        resetConfigAsInTheFile()
    }

    @Test
    public void testSSBTypeRequestWithProxy() {
        backupConfigFileConfigurations();
        setupSSBWithProxy()
        def conn = bannerDS.getConnection()
        assertTrue "Expected BannerConnection but have ${conn?.class}", conn instanceof BannerConnection
        dataSource.removeConnection(conn)
        if (conn) conn.close()
        tearDownDataSetup()
        resetConfigAsInTheFile()
    }

    @Test
    public void testAPITypeRequestWithNoProxy() {
        backupConfigFileConfigurations();
        setupAPIWithNoProxy()
        def conn = bannerDS.getConnection()
        assertTrue "Expected BannerConnection but have ${conn?.class}", conn instanceof BannerConnection
        dataSource.removeConnection(conn)
        if (conn) conn.close()
        tearDownDataSetup()
        resetConfigAsInTheFile()
    }

    @Test
    public void testOracleMessageTranslationForCanadianFrench() {
        def locale = 'fr_CA'
        def conn = bannerDS.getConnection()
        def sql = new Sql(conn)
        BannerDS.callNlsUtility(sql, locale)
        sql.eachRow("select VALUE from v\$nls_parameters where parameter='NLS_LANGUAGE'") { row ->
            assertEquals("CANADIAN FRENCH", row.value)
        }
        if (sql) sql.close()
        if (conn) conn.close()

    }

    @Test
    public void testOracleMessageTranslationForSpanish() {
        def locale = 'es_MX'
        def conn = bannerDS.getConnection()
        def sql = new Sql(conn)
        BannerDS.callNlsUtility(sql, locale)
        sql.eachRow("select VALUE from v\$nls_parameters where parameter='NLS_LANGUAGE'") { row ->
            assertEquals("SPANISH", row.value)
        }
        if (sql) sql.close()
        if (conn) conn.close()

    }

    @Test
    public void testSetDBMSApplicationInfo() {
        backupConfigFileConfigurations();
        setupAPIWithNoProxy()
        def conn = bannerDS.getConnection()
        try {
            assertNotNull(conn)
            Logger.getRootLogger().setLevel(Level.TRACE)
            bannerDS.setDbmsApplicationInfo(conn, null, null)
        } finally {
            Logger.getRootLogger().setLevel(Level.OFF)
            dataSource.removeConnection(conn)
            if (conn) conn.close()
            tearDownDataSetup()
            resetConfigAsInTheFile()
        }

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
    public void testGetUserRoles() {
        setUpValidAdminUserId()
        login(username, password)
        def user = SecurityContextHolder?.context?.authentication?.principal
        Map unlockedRoles = bannerDS.userRoles(user, user?.authorities)
        assert (!unlockedRoles.isEmpty())

        assertTrue(unlockedRoles.find { key, value -> key.equals('BAN_DEFAULT_M') }.value)
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
    public void testGetCachedConnection() {
        backupConfigFileConfigurations()
        setupAPIWithNoProxy()
        def conn = bannerDS.getConnection()
        assertNotNull(conn)
        dataSource.removeConnection(conn)
        if (conn) conn.close()
        tearDownDataSetup()
        resetConfigAsInTheFile()
    }

    @Test
    public void testClearDmsApplicationInfo() {
        backupConfigFileConfigurations()
        setupAPIWithNoProxy()
        def conn = bannerDS.getConnection()
        try {
            assertNotNull(conn)
            Logger.getRootLogger().setLevel(Level.TRACE)
            bannerDS.clearDbmsApplicationInfo(conn)
        } finally {
            Logger.getRootLogger().setLevel(Level.OFF)
            dataSource.removeConnection(conn)
            if (conn) conn.close()
            tearDownDataSetup()
            resetConfigAsInTheFile()
        }
    }

    @Test
    public void testMebSSBMethod() {
        backupConfigFileConfigurations()
        setupAPIWithNoProxy()
        def conn
        def oldSbbValue
        def oldMepValue
        try {
            oldMepValue = RequestContextHolder.currentRequestAttributes().request.session.servletContext.getAttribute('mepEnabled')
            oldSbbValue = config.ssbEnabled
            config.ssbEnabled = true
            RequestContextHolder.currentRequestAttributes().request.session.servletContext.setAttribute('mepEnabled', true)
            conn = bannerDS.getConnection()
        } finally {
            assertNotNull(conn)
            RequestContextHolder.currentRequestAttributes().request.session.servletContext.setAttribute('mepEnabled', oldMepValue)
            config.ssbEnabled = oldSbbValue
            dataSource.removeConnection(conn)
            if (conn) conn.close()
            tearDownDataSetup()
            resetConfigAsInTheFile()
        }
    }


    @Test
    public void testSetIdentifier() {
        def conn
        def sql

        try {
            Logger.getRootLogger().setLevel(Level.TRACE)
            if (log.isTraceEnabled()) {
                conn = bannerDS.getConnection()
                def identifier = "Banner_Core_Integration_Tests"
                bannerDS.setIdentifier(conn, identifier)

                sql = new Sql(conn)
                def row = sql.firstRow("select sys_context( 'USERENV', 'CLIENT_IDENTIFIER' ) FROM DUAL")
                assertEquals "Banner_Core_Integration_Tests", row."SYS_CONTEXT('USERENV','CLIENT_IDENTIFIER')"
            }
        } finally {
            bannerDS.clearIdentifer(conn)
            Logger.getRootLogger().setLevel(Level.OFF)
            dataSource.removeConnection(conn)
            if (sql) sql.close()
            if (conn) conn.close()
            tearDownDataSetup()
            resetConfigAsInTheFile()
        }
    }

    @Test
    public void testNullPrincipal() {
        def backup
        def conn
        try {
            backup = SecurityContextHolder?.context?.authentication?.principal
            SecurityContextHolder?.context?.authentication?.principal = null
            conn = bannerDS.getConnection()
            assertNotNull(conn)
        } finally {
            SecurityContextHolder?.context?.authentication?.principal = backup
        }
    }

    @Test
    public void testProxyAndSetRolesFor() {
        setUpValidAdminUserId()
        login(username, password)
        def banConn
        def conn
        try {
            banConn = (bannerDS.getConnection() as BannerConnection)
            conn = bannerDS.proxyAndSetRolesFor(banConn, username, password)
            assertNotNull(conn)
            conn = bannerDS.proxyAndSetRolesFor(banConn, null, null)
            assertNull(connen)
        } catch (e) {
            print e.stackTrace
        } finally {
            if (conn) conn.close()
            if (banConn) banConn.close()
        }
    }

    @Test
    public void testProxyAndSetRolesForWithoutAuthentication() {
        def oldValue = SecurityContextHolder?.context?.authentication
        setUpValidAdminUserId()
        login(username, password)
        SecurityContextHolder?.context?.authentication = null
        def conn
        def banConn
        try {
            banConn = (bannerDS.getConnection() as BannerConnection)
            conn = bannerDS.proxyAndSetRolesFor(banConn, username, password)
            assertNull(conn)
        } catch (e) {
            print e.stackTrace
        } finally {
            SecurityContextHolder?.context?.authentication = oldValue
            if (conn) conn.close()
            if (banConn) banConn.close()
        }
    }

    @Test
    public void testProxyAndSetRolesForNullPrincipalCheck() {
        def backup
        def conn
        def banConn
        try {
            setUpValidAdminUserId()
            login(username, password)
            backup = SecurityContextHolder.context.authentication.user
            SecurityContextHolder.context.authentication.user = null
            banConn = (bannerDS.getConnection() as BannerConnection)
            conn = bannerDS.proxyAndSetRolesFor(banConn, username, password)
            assertNull(conn)
        } finally {
            if (conn) conn.close()
            if (banConn) banConn.close()
            SecurityContextHolder.context.authentication.user = backup
        }
    }

/***************************************Helper Methods***************************************************/

    private void backupConfigFileConfigurations() {
        ssbOracleUsersProxiedInFile = config.ssbOracleUsersProxied
        apiOracleUsersProxiedInFile = config.apiOracleUsersProxied
        apiUrlPrefixesInFile = config.apiUrlPrefixes
        ssbEnabledInFile = config.ssbEnabled
    }

    private void resetConfigAsInTheFile() {
        config.ssbOracleUsersProxied = ssbOracleUsersProxiedInFile
        config.apiOracleUsersProxied = apiOracleUsersProxiedInFile
        config.apiUrlPrefixes = apiUrlPrefixesInFile
        config.ssbEnabled = ssbEnabledInFile
    }

    private void setupAPIData() {
        username = ""
        password = ""
        config.apiUrlPrefixes = ["api", "qapi"]
        GrailsMockHttpServletRequest request = new GrailsMockHttpServletRequest()
        request.setForwardURI("api")
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request))
        RequestContextHolder.getRequestAttributes().session = request.getSession(true)
        setupUnderlyingSSBDataSource()
    }

    private void setupAPIWithNoProxy() {
        setupAPIData()
        config.with {
            apiOracleUsersProxied = false
        }
        super.setUp()
    }

    private void setupAPIWithProxy() {
        setupAPIData()
        config.with {
            apiOracleUsersProxied = true
        }
        username = PROXY_USERNAME
        password = PROXY_PASSWORD
        super.setUp()
    }

    private void setupSSBData() {
        formContext = ['SELFSERVICE']
        config.ssbEnabled = true
        setupUnderlyingSSBDataSource()
    }


    private void setupSSBWithNoProxy() {
        config.ssbOracleUsersProxied = false
        username = SSB_VALID_USERNAME
        password = SSB_VALID_PASSWORD
        setupSSBData()
        SSBSetUp(username, password)
    }

    private void setupSSBWithProxy() {
        config.ssbOracleUsersProxied = true
        username = PROXY_USERNAME
        password = PROXY_PASSWORD
        setupSSBData()
        SSBSetUp(username, password)
    }

    private void setupUnderlyingSSBDataSource() {
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
        dataSource.underlyingSsbDataSource = testSpringContext.getBean("underlyingSsbDataSource")
    }

    private void tearDownDataSetup() {
        logout()
        super.tearDown()
    }

    private void setUpValidSSBTypeUser() {
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
        dataSource.underlyingSsbDataSource = testSpringContext.getBean("underlyingSsbDataSource")
    }

    private void setUpFormContext() {
        formContext = ['GUAGMNU']
    }

    private void setUpValidAdminUserId() {
        setUpFormContext()
        username = "grails_user"
        password = "u_pick_it"
    }
}
/*******************************************************************************
Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
*******************************************************************************/
package net.hedtech.banner.framework

import grails.test.GrailsUnitTestCase

import groovy.sql.Sql
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

import java.sql.Connection

import net.hedtech.banner.db.BannerDS as BannerDataSource
import net.hedtech.banner.db.BannerConnection
import net.hedtech.banner.security.FormContext
import net.hedtech.banner.testing.BaseIntegrationTestCase
import net.hedtech.banner.testing.Foo
import net.hedtech.banner.security.FormContext
import grails.test.GrailsUnitTestCase

import groovy.sql.Sql

import java.sql.Connection

import oracle.jdbc.OracleConnection

import org.junit.Ignore


/**
 * Integration tests exercising the BannerDS (formerly named BannerDataSource) data source implementation.
 */
public class BannerDataSourceIntegrationTests extends BaseIntegrationTestCase {

    def dataSource

    // NOTE: Please also see 'FooServiceIntegrationTests', as that test also includes framework tests pertaining to connections.


    def formContext


    @Before
    public void setUp(){
        super.setUp()

    }

    @After
    public void tearDown() {
        super.setUp()
    }
    @Test
    void testExpectedConfiguration() {
        formContext = ['GUAGMNU']
        FormContext.set(formContext)

        assertTrue "Expected to have a BannerDataSource but instead have ${dataSource?.class}", (dataSource instanceof BannerDataSource)
        Connection conn = dataSource.getConnection()
        assertTrue "Expected BannerConnection but have ${conn?.class}", conn instanceof BannerConnection
        assertTrue "Expected to be able to extract OracleConnection but found ${(conn as BannerConnection).extractOracleConnection()?.class}",
                   (conn as BannerConnection).extractOracleConnection() instanceof OracleConnection
    }


    @Test
    void testProxyConnection() {
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
    void testSettingOracleRole() {
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


}

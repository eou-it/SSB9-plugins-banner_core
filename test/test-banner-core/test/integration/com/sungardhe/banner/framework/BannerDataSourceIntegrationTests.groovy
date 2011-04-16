/** *****************************************************************************
 © 2010 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
package com.sungardhe.banner.framework

import com.sungardhe.banner.db.BannerDS as BannerDataSource
import com.sungardhe.banner.db.BannerConnection
import com.sungardhe.banner.testing.Foo

import grails.test.GrailsUnitTestCase

import groovy.sql.Sql

import java.sql.Connection

import oracle.jdbc.OracleConnection


/**
 * Integration tests exercising the BannerDS (formerly named BannerDataSource) data source implementation.
 */
public class BannerDataSourceIntegrationTests extends GrailsUnitTestCase {

    def dataSource
    
    
    // NOTE: Please also see 'FooServiceIntegrationTests', as that test also includes framework tests pertaining to connections. 


    void testExpectedConfiguration() {
        assertTrue "Expected to have a BannerDataSource but instead have ${dataSource?.class}", (dataSource instanceof BannerDataSource)
        Connection conn = dataSource.getConnection()
        assertTrue "Expected BannerConnection but have ${conn?.class}", conn instanceof BannerConnection
        assertTrue "Expected to be able to extract OracleConnection but found ${(conn as BannerConnection).extractOracleConnection()?.class}",
                   (conn as BannerConnection).extractOracleConnection() instanceof OracleConnection
    }
    

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
    
    
    void testSettingOracleRole() {
        BannerConnection conn
        Sql sql
        try {
            conn = (dataSource as BannerDataSource).getUnproxiedConnection() as BannerConnection
            sql = new Sql( conn.extractOracleConnection() )
            
            // Retrieve the database role name and password for object STVINTS
            def row = sql.firstRow( "select * from govurol where govurol_object = 'STVINTS'" )
            
            conn = (dataSource as BannerDataSource).proxyConnection( conn, "grails_user" ) as BannerConnection
            
            String stmt = "set role ${row.govurol_role} identified by \"${row.govurol_role_pswd}\"" 
            sql = new Sql( conn.extractOracleConnection() )
            sql.execute( stmt )
        } finally {
            sql?.close()
        }
    }


}
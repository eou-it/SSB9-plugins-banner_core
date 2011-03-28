/** *****************************************************************************

 © 2010 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
package com.sungardhe.banner.db

import com.sungardhe.banner.security.FormContext
import com.sungardhe.banner.security.BannerGrantedAuthority

import groovy.sql.Sql

import javax.sql.DataSource
import java.sql.Connection
import java.sql.SQLException
import java.sql.CallableStatement

import oracle.jdbc.OracleConnection

import org.apache.commons.dbcp.BasicDataSource
import org.apache.log4j.Logger

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder


/**
 * A dataSource that proxies connections, sets roles needed for the current request, 
 * and invokes p_commit and p_rollback.
 **/
class BannerConnection {

    @Delegate
    Connection underlyingConnection

    def proxyUserName
    def bannerDataSource
    private final Logger log = Logger.getLogger( getClass() )


    BannerConnection( Connection conn, DataSource bannerDataSource ) {
        assert conn
        assert bannerDataSource
        underlyingConnection = conn
        this.bannerDataSource = bannerDataSource
        log.trace "BannerConnection has been constructed: ${this}"
        invokeProcedureCall "{ call DBMS_SESSION.MODIFY_PACKAGE_STATE(2) }" // Constant DBMS_SESSION.REINITIALIZE = 2
    }


    BannerConnection( Connection conn, String userName, bannerDataSource ) {
        this( conn, bannerDataSource )
        proxyUserName = userName
    }


    public OracleConnection extractOracleConnection() {
        bannerDataSource.nativeJdbcExtractor.getNativeConnection( underlyingConnection )
    }


    /**
     * Replaces the invocation of 'commit' on the underlying connection with
     * an invocation of the 'p_commit' stored procedure, as required by
     * all Banner clients.  The p_commit procedure is needed to ensure
     * proper generation of events that support OpenEAI messaging semantics.
     * @see java.sql.Connection#commit()
     */
    public void commit() throws SQLException {
        log.trace "BannerConnection ${super.toString()} 'commit()' invoked"
        invokeProcedureCall "{ call gb_common.p_commit() }"
    }


    /**
     * Replaces the invocation of rollback on the underlying connection with
     * an invocation of the 'p_rollback' stored procedure, as required by
     * all Banner clients.  The p_rollback procedure is needed to ensure
     * proper generation of events that support OpenEAI messaging semantics.
     * @see java.sql.Connection#rollback()
     */
    public void rollback() throws SQLException {
        log.trace "BannerConnection ${super.toString()}.rollback() invoked"
        invokeProcedureCall "{ call gb_common.p_rollback() }"
    }


    public void close() throws SQLException {
        log.trace "BannerConnection ${super.toString()}.close() invoked"
//        invokeProcedureCall "{ set role 'null' }" // TODO: Do we need to clear the roles if we're releasing the connection?

        def oconn = extractOracleConnection()
        log.trace "${super.toString()}.closeProxySession() will close proxy session for $oconn"
        closeProxySession oconn, proxyUserName
        
        log.trace "${super.toString()} will close it's underlying connection: $underlyingConnection, that wraps $oconn"
        underlyingConnection?.close()
    }


    public static closeProxySession( OracleConnection oracleConnection, String proxiedUserName ) {
        if (oracleConnection.isProxySession()) {
            oracleConnection.close( OracleConnection.PROXY_SESSION )
        }
    }


    /**
     * Invokes the supplied stored procedure call.
     * @param procedureCall the stored procedure to call
     * @throws SQLException if reported when executing this stored procedure
     */
    private void invokeProcedureCall( String procedureCall ) throws SQLException {
        log.trace "BannerConnection ${super.toString()}.invokeProcedureCall() will execute '$procedureCall'"
        CallableStatement cs
        try {
            cs = underlyingConnection.prepareCall( procedureCall )
            cs.execute()
        }
        finally {
            cs?.close()
        }
    }

    boolean isWrapperFor( Class clazz ) {
        log.trace "isWrapperFor clazz = $clazz"
    }

    Object unwrap( Class clazz ) {
        log.trace "unwrap clazz = $clazz"
    }

    public String toString() {
        "${super.toString()}[user='${proxyUserName}', oracle connection='${extractOracleConnection()}']"
    }


}
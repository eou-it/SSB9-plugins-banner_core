/*******************************************************************************
Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
*******************************************************************************/ 
package net.hedtech.banner.db

import net.hedtech.banner.security.FormContext
import net.hedtech.banner.security.BannerGrantedAuthority

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
    def oracleConnection // the native connection
    
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
        bannerDataSource.setIdentifier( conn, userName )
    }


    public OracleConnection extractOracleConnection() {
        if (!oracleConnection) {
            oracleConnection = bannerDataSource.nativeJdbcExtractor.getNativeConnection( underlyingConnection )
        }
        oracleConnection
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
        bannerDataSource.clearDbmsApplicationInfo( this )           
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
        bannerDataSource.clearDbmsApplicationInfo( this )            
    }


    public void close() throws SQLException {
        try {
            log.trace "BannerConnection ${super.toString()}.close() invoked"
            bannerDataSource.closeProxySession( this, proxyUserName )              
            bannerDataSource.clearIdentifer( this )
        } finally {            
            log.trace "${super.toString()} will close it's underlying connection: $underlyingConnection, that wraps ${extractOracleConnection()}"
            underlyingConnection?.close()  
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

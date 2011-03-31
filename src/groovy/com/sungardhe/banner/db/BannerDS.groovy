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

import java.sql.Connection
import java.sql.SQLException
import java.sql.CallableStatement
import javax.sql.DataSource

import oracle.jdbc.OracleConnection

import org.apache.log4j.Logger

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder

// This class was renamed from the more desirable 'BannerDataSource' as doing so
// circumvented 'cannot resolve class' issues when running applications using this plugin.
// To reduce confusion, you may want to import this class using:
//     'import com.sungardhe.banner.db.BannerDS as BannerDataSource'
//
/**
 * A dataSource that proxies connections, sets roles needed for the current request,
 * and invokes p_commit and p_rollback.
 **/
public class BannerDS {

    // Delegates all methods not implemented here, to the underlying dataSource injected via Spring.
    @Delegate
    DataSource underlyingDataSource

    def nativeJdbcExtractor  // injected by Spring

    private final Logger log = Logger.getLogger( getClass() )

    /**
     * Returns a proxied connection for the current logged in user, from the underlying connection pool.
     * In addition to proxying the connection, appropriate password protected roles are unlocked
     * (based upon the configuration within govurol).
     * */
    public Connection getConnection() throws SQLException {
        log.trace "in BannerDS.getConnection() -- going to delegate to underlying dataSource"

        Connection conn = underlyingDataSource.getConnection()
        OracleConnection oconn = nativeJdbcExtractor.getNativeConnection( conn )

        log.trace "in BannerDS.getConnection() -- have attained connection ${oconn} from underlying dataSource"

        // We'll proxy and set roles on the underlying Oracle connection before wrapping it in
        // the BannerConnection
        def user
        if (SecurityContextHolder?.context?.authentication) {
            user = SecurityContextHolder.context.authentication.principal
            if (user?.username) {
                List applicableAuthorities = extractApplicableAuthorities( user?.authorities )
                proxy( oconn, user?.username )
                setRoles( oconn, user?.username, applicableAuthorities )
            }
        }
        return new BannerConnection( conn, user?.username, this )  // Note that while an IDE may not like this, the delegate supports this type coersion
    }
    

    // Note: This method is used for Integration Tests.
    public Connection proxyAndSetRolesFor( BannerConnection bconn, userName, password ) {
        def user
        if (SecurityContextHolder?.context?.authentication) {
            user = SecurityContextHolder.context.authentication.principal
            if (user?.username && user?.password) {
                List applicableAuthorities = extractApplicableAuthorities( user?.authorities )
                proxyConnection( bconn, userName )
                setRoles(bconn.extractOracleConnection(), user?.username, applicableAuthorities )
            }
        }
    }
    

    // Note: This method should be used only for initial authentication, and for testing purposes.
    /**
     * This method serves the banner authentication provider, by returning an unproxied connection that may be used
     * to retrive authorities for a user. Once the authorities are retrieved, the BannerAuthenticationProvider is
     * exected to call 'proxyConnection(conn,username,password)' in order to authenticate that user.
     * Once the authentication completes, all connections attained though getConnection()
     * will be proxied (this method should not be used to explicitly pass the username and password).
     * Note that this method does NOT set password protected roles, as it is intended solely for authentication
     * and not authorization. Subsequent calls to getConnection() method will unlock roles as appropriate.
     * */
    public Connection getUnproxiedConnection() {
        Connection conn = underlyingDataSource.getConnection()
        OracleConnection oconn = nativeJdbcExtractor.getNativeConnection( conn )
        return new BannerConnection( conn, null, this )  // Note that while an IDE may not like this, the delegate supports this type coersion
    }
    

    // Note: This method should be used only for initial authentication, and for testing purposes.
    // WARNING: This method MUTATES the supplied connection by proxying it for the identified user.
    /**
     * This method serves the banner authentication provider, by proxying the supplied connection in order
     * to authenticate a user. Once the authentication completes, all connections attained though getConnection()
     * will be proxied (this method should not be used to explicitly pass the username and password).
     * Note that this method does NOT set password protected roles, as it is intended solely for authentication
     * and not authorization. Subsequent calls to getConnection() method will unlock roles as appropriate.
     * */
    public Connection proxyConnection( BannerConnection bconn, userName ) {
        proxy( bconn.extractOracleConnection(), userName )
        bconn.proxyUserName = userName
        return bconn
    }
    

    /**
     * Returns the jdbcUrl of the underlying DataSource.
     */
    public String getUrl() {
        return underlyingDataSource.getUrl()
    }
    

    // ------------- end of public methods ----------------


    private proxy( OracleConnection oconn, userName ) {
        log.trace "BannerDS.proxyConnection invoked with $oconn, $userName"

        Properties properties = new Properties()
        // Oracle bug 4689310 precludes setting the userName and password the 'right' way (per Oracle documentation)
        // properties.put( OracleConnection.PROXY_USER_NAME, userName )
        // properties.put( OracleConnection.PROXY_USER_PASSWORD, password )
        // The following is a workaround approach:
        // properties.put( OracleConnection.PROXY_USER_NAME, ("${userName}/${password}" as String) )
        // We proxy connections without authenticating via password.  This is required in a
        // claims-based authentication approach.
        properties.put( OracleConnection.PROXY_USER_NAME, ("${userName}" as String) )

        oconn.openProxySession( OracleConnection.PROXYTYPE_USER_NAME, properties )
        log.trace "in BannerDS.proxyConnection - proxied connection for $userName and connection $oconn"
    }


    private List extractApplicableAuthorities( grantedAuthorities ) {
        if (!grantedAuthorities) return

        List formContext = FormContext.get()
        log.debug "BannerDS has retrieved the FormContext value: $formContext"
        // log.debug "The user's granted authorities are $grantedAuthorities*.authority" // re-enable in development to see all the user's privileges 

        List applicableAuthorities = []
        formContext.each { form ->
            def authoritiesForForm = grantedAuthorities.findAll { it.authority ==~ /\w+_${form}_\w+/ }
            authoritiesForForm.each { applicableAuthorities << it }
        }
        log.debug "Given FormContext of ${formContext?.join(',')}, the user's applicable authorities are $applicableAuthorities" 
        applicableAuthorities
    }


    private setRoles( OracleConnection oconn, String proxiedUserName, applicableAuthorities ) {
        log.debug "Applicable role(s) are ${applicableAuthorities*.authority}" 

        try {
            log.trace "BannerDS.setRoles - will unlock role(s) for the connection proxied for $proxiedUserName"
            applicableAuthorities?.each { auth -> unlockRole( oconn, (BannerGrantedAuthority) auth ) }
            log.trace "BannerDS.setRoles unlocked role(s) for the connection proxied for $proxiedUserName"
        }
        catch (e) {
            //if we cannot unlock a role, abort the proxy session and rollback
            log.error "Failed to unlock role for proxy session for Oracle connection $oconn  Exception: $e "
            BannerConnection.closeProxySession( oconn, proxiedUserName )
            throw e
        }
    }


    private unlockRole( Connection conn, BannerGrantedAuthority bannerAuth ) throws SQLException {
        switch (bannerAuth.bannerPassword) {
            case null:        println "No role to unlock -- the password was null"
                              return // nothing to do... no roles need to be set
                              
            case 'INSECURED': println "No role to unlock -- the password was 'INSECURED'"
                              return // nothing to do... no roles need to be set
                              
            case 'ABORT':     println "Role 'ABORT' encountered - throwing an exception!"
                              throw new RuntimeException( "ABORT Banner Role encountered!" )
        }
        // still here? We will now try to set the role...
        Sql db = new Sql( conn )
        try {
            log.trace "BannerDS.unlockRole will set role '${bannerAuth.roleName}' for connection $conn" 
            db.execute( "set role \"${bannerAuth.roleName}\" identified by \"${bannerAuth.bannerPassword}\"" )
        }
        finally {
            // Note: Don't close the Sql as this closes the connection, and we're preparing the connection for subsequent use
        }
    }
    

    public void setLogWriter( PrintWriter printWriter ) {
        log.trace "setLogWriter printWriter = $printWriter"
    }
    

    public PrintWriter getLogWriter() {
        log.trace 'getLogWriter'
    }
    

    boolean isWrapperFor( Class clazz ) {
        log.trace "isWrapperFor clazz = $clazz"
    }
    

    Object unwrap( Class clazz ) {
        log.trace "unwrap clazz = $clazz"
    }
    

    void setLoginTimeout( int i ) {
        log.trace "setLoginTimeout i = $i"
    }
    

    int getLoginTimeout() {
        log.trace 'getLoginTimeout'
    }

}


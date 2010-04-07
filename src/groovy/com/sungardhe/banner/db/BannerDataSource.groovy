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
import org.springframework.security.GrantedAuthority
import org.springframework.security.context.SecurityContextHolder


/**
 * A dataSource that proxies connections, sets roles needed for the current request, 
 * and invokes p_commit and p_rollback.
 **/
public class BannerDataSource {

    // Delegates all methods not implemented here, to the underlying dataSource injected via Spring.
    @Delegate
    DataSource underlyingDataSource

    def nativeJdbcExtractor

    private final Logger log = Logger.getLogger( getClass() )


    /**
     * Returns a proxied connection for the current logged in user, from the underlying connection pool.
     * In addition to proxying the connection, appropriate password protected roles are unlocked
     * (based upon the configuration within govurol).
     * */
    public Connection getConnection() throws SQLException {
        log.trace "in BannerDataSource.getConnection() -- going to delegate to underlying dataSource"

        Connection conn = underlyingDataSource.getConnection()
        OracleConnection oconn = nativeJdbcExtractor.getNativeConnection( conn )

        log.trace "in BannerDataSource.getConnection() -- have attained connection ${oconn} from underlying dataSource"

        // We'll proxy and set roles on the underlying Oracle connection before wrapping it in
        // the BannerConnection
        def user
        if (SecurityContextHolder?.context?.authentication) {
            user = SecurityContextHolder.context.authentication.principal
            if (user?.username && user?.password) {
                List applicableAuthorities = extractApplicableAuthorities( user?.authorities )
                proxy( oconn, user?.username )
                setRoles( oconn, user?.username, applicableAuthorities )
            }
        }
        return new BannerConnection( conn, user?.username, this )  // Note that while an IDE may not like this, the delegate supports this type coersion
    }
    
    
    // Note: This method is used for Integration Tests -- it is not intended to be used within production code.
    // TODO: Refactor - not DRY with other methods
    public Connection proxyAndSetRolesFor( BannerConnection bconn, userName, password ) {
        def user
        if (SecurityContextHolder?.context?.authentication) {
            user = SecurityContextHolder.context.authentication.principal
            if (user?.username && user?.password) {
                List applicableAuthorities = extractApplicableAuthorities( user?.authorities )
                proxyConnection( bconn, userName )
                setRoles( bconn.extractOracleConnection(), user?.username, applicableAuthorities )
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
     * Returns the jdbcUrl of the real DataSource
     */
    public String getUrl() {
    	return (underlyingDataSource as BasicDataSource).getUrl()
    }

    // ------------- end of public methods ----------------


    private proxy( OracleConnection oconn, userName ) {
        log.trace "BannerDataSource.proxyConnection invoked with $oconn, $userName"

        Properties properties = new Properties()
        // Oracle bug 4689310 precludes setting the userName and password the 'right' way (per Oracle documentation)
        // properties.put( OracleConnection.PROXY_USER_NAME, userName )
        // properties.put( OracleConnection.PROXY_USER_PASSWORD, password )
        // The following is a workaround approach:
        // properties.put(OracleConnection.PROXY_USER_NAME, ("${userName}/${password}" as String))
        // Changed approach to proxy connections without authenticating via password.  This will be required in a
        // claims based authentication approach and IdM integration.
        properties.put( OracleConnection.PROXY_USER_NAME, ("${userName}" as String) )

        oconn.openProxySession( OracleConnection.PROXYTYPE_USER_NAME, properties )
        log.trace "in BannerDataSource.proxyConnection - proxied connection for $userName and connection $oconn"
    }


    private List extractApplicableAuthorities(grantedAuthorities) {
        if (!grantedAuthorities) return

        List formContext = FormContext.get()
        log.debug "BannerDataSource has retrieved the FormContext value: $formContext"
        log.debug "The user's granted authorities are $grantedAuthorities*.authority" // TODO remove logging of authorities, or log only in Test environment

        List applicableAuthorities = []
        formContext.each { form ->
            def authoritiesForForm = grantedAuthorities.findAll { it.authority ==~ /\w+_${form}_\w+/ }
            authoritiesForForm.each { applicableAuthorities << it }
        }
        log.debug "The user's applicable authorities are $applicableAuthorities" // TODO remove logging of authorities, or log only in Test environment
        applicableAuthorities
    }


    private setRoles( OracleConnection oconn, String proxiedUserName, applicableAuthorities ) {
        log.debug "Applicable roles are ${applicableAuthorities*.authority}" // TODO remove logging of authorities, or log only in Test environment

        try {
            log.trace "BannerDataSource.setRoles - will unlock roles for the connection proxied for $proxiedUserName"
            applicableAuthorities?.each { auth -> unlockRole(oconn, (BannerGrantedAuthority) auth) }
            log.trace "BannerDataSource.setRoles unlocked roles for the connection proxied for $proxiedUserName"
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
            case null: println "No role to unlock -- the password was null"
                return // nothing to do... no roles need to be set
            case 'INSECURED': println "No role to unlock -- the password was 'INSECURED'"
                return // nothing to do... no roles need to be set
            case 'ABORT': println "Role 'ABORT' encountered - throwing an exception!"
                throw new RuntimeException("ABORT Banner Role encountered!")
        }
        // still here? We will try to unlock the role...
        Sql db = new Sql( conn )
        try {
            String stmt = "set role \"${bannerAuth.roleName}\" identified by \"${bannerAuth.bannerPassword}\""
            log.debug "BannerDataSource.unlockRole will now execute $stmt"
            db.execute(stmt);
            log.debug "BannerDataSource.unlockRole executed $stmt"
        }
        finally {
            // Note: Don't close the Sql as this closes the connection, and we're preparing the connection for subsequent use
        }
    }
}


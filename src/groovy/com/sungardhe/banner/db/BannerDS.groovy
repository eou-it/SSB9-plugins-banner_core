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


/**
 * A dataSource that wraps an 'underlying' datasource.  When this datasource is asked for a 
 * connection, it will first:
 * 1) proxy the connection for the authenticated user, 
 * 2) set Banner roles that are applicable to the current request, based on the authenticated user's privileges
 * and the 'FormContext' 
 * 3) wrap the connection within a 'BannerConnection'. (The BannerConnection wrapper will 
 * invoke p_commit and p_rollback.) 
 * Note this class is named 'BannerDS' versus 'BannerDataSource' to circumvent 
 * 'cannot resolve class' issues when including this plugin.  It is recommended when importing this  
 * class, to import it like: 'import com.sungardhe.banner.db.BannerDS as BannerDataSource'. 
 **/
public class BannerDS implements DataSource {

    // Delegates all methods not implemented here, to the underlying dataSource injected via Spring.
    DataSource underlyingDataSource
    DataSource underlyingSSBDataSource

    def nativeJdbcExtractor  // injected by Spring

    private final Logger log = Logger.getLogger( getClass() )


    /**
     * Returns a proxied connection for the current logged in user, from the underlying connection pool.
     * In addition to proxying the connection, appropriate password protected roles are unlocked
     * (based upon the configuration within govurol).
     * */
    public Connection getConnection() throws SQLException {
        log.trace "BannerDS.getConnection() invoked and will delegate to the underlying dataSource"

        Connection conn = getUnderlyingDataSource().getConnection()
        OracleConnection oconn = nativeJdbcExtractor.getNativeConnection( conn )

        log.debug "BannerDS.getConnection() has attained connection ${oconn} from the underlying dataSource"

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
        new BannerConnection( conn, user?.username, this )  // Note that while an IDE may not like this, the delegate supports this type coersion
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
        Connection conn = getUnderlyingDataSource().getConnection()
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


    public void closeProxySession( BannerConnection conn, String proxiedUserName ) {
        log.trace "${super.toString()}.closeProxySession() will close proxy session for $conn"
                
        if (conn.extractOracleConnection().isProxySession()) {
            conn.extractOracleConnection().close( OracleConnection.PROXY_SESSION )
        }
    }


    // note: This method would be implemented within BannerConnection, however that is proxied when it is retrieved from the hibernate session 
    /**
     * Sets the identifer into DBMS_SESSION. 
     * @param conn the connection upon which to set the dbms session identifer
     * @param identifer the identifier to set
     **/
    public void setIdentifier( conn, identifier ) {
        log.trace "BannerConnection.setIdentifier will execute 'dbms_session.set_identifier' using identifer '$identifier' for '$conn'" 
        Sql db = new Sql( conn ) 
        db.call( "{call dbms_session.set_identifier(?)}", [ identifier ] ) 
    }
    
    
    // note: This method would be implemented within BannerConnection, however that is proxied when it is retrieved from the hibernate session 
    /**
     * Clears the identifer from the DBMS_SESSION.  This should be called when 'closing' the connection. 
     * @param conn the connection upon which to clear the dbms session identifer
     **/
    public void clearIdentifer( conn ) {
        log.trace "BannerConnection.clearIdentifier will execute 'dbms_session.set_identifier' using a null value, for '$conn'" 
        Sql db = new Sql( conn ) 
        db.call( "{call dbms_session.set_identifier( NULL )}" ) 
    }
    
    
    // note: This method would be implemented within BannerConnection, however that is proxied when it is retrieved from the hibernate session 
    /**
     * Sets the 'module' and 'action' into the 'DBMS_APPLICATION_INFO'. This method should be called by services, when  
     * logging at the debug level (except for testing).  Note that 'ServiceBase' does this for it's CRUD methods. 
     * @param conn the connection upon which to set DBMS application info
     * @param module The 'module' to be set, which will be populated from the FormContext threadlocal if not provided
     * @param action the 'action' to set, which will be NULL if not provided 
     **/
    public void setDbmsApplicationInfo( conn, module = null, action = null ) {
        String mod = module ?: (FormContext.get() ? FormContext.get()[0] : null) // FormContext returns a list, but we'll just use the first entry        
        log.trace "BannerConnection.setDbmsApplicationInfo will call Oracle 'dbms_application_info.set_module' using module = '$mod' and action = '$action' for '$conn'" 
    
        Sql db = new Sql( conn )        
        db.call( "{call dbms_application_info.set_module(?,?)}", [ mod, action ] ) 
        // Note: Don't close the Sql as this closes the connection, and we're preparing the connection for subsequent use
    }
    
    
    // note: This method would be implemented within BannerConnection, however that is proxied when it is retrieved from the hibernate session 
    /**
     * Clears the DBMS application information.  This should be invoked when 'committing' the transaction. 
     * @param conn the connection upon which to clear DBMS application info
     **/
    public void clearDbmsApplicationInfo( conn ) {
        log.trace "BannerConnection.clearDbmsApplicationInfo will call Oracle 'dbms_application_info.set_module' with null values, for '$conn'" 
        Sql db = new Sql( conn )        
        db.call "{call dbms_application_info.set_module( NULL,NULL )}"
        // Note: Don't close the Sql as this closes the connection, and we're preparing the connection for subsequent use
    }
    


    /**
     * Returns the jdbcUrl of the underlying DataSource.
     */
	public String getUrl() {
	    def url
	      try {
	        //Getting the URL this way works only with Apache DBCP datasource
	        //This does not work when using Weblogic server as Weblogic server returns a RmiDatasource
	        url = getUnderlyingDataSource().getUrl()
	      } catch(MissingMethodException e) {
	        url = getUnderlyingDataSource().connection.metaData.URL
	      }
	      return url
	}
	
	
	// -------------------- Pass through methods to delegate ------------------------
	
	public Connection getConnection( String username, String password ) {
	    getUnderlyingDataSource().getConnection( username, password )
	}
    

    public void setLogWriter( PrintWriter printWriter ) {
        log.trace "BannerDS.setLogWriter(printWriter) will set '$printWriter' onto the underyling dataSource"
        getUnderlyingDataSource().setLogWriter( printWriter )
    }
    

    public PrintWriter getLogWriter() {
        log.trace 'BannerDS.getLogWriter() will delegate to underlying dataSource'
        getUnderlyingDataSource().getLogWriter()
    }
    

    boolean isWrapperFor( Class clazz ) {
        log.trace "BannerDS.isWrapperFor(clazz) was invoked with '$clazz' and will delegate to the underlying dataSource"
        getUnderlyingDataSource().isWrapperFor( clazz )
    }
    

    Object unwrap( Class clazz ) {
        log.trace "BannerDS.unwrap(clazz) was invoked with '$clazz' and will delegate to the underlying dataSource"
        getUnderlyingDataSource().unwrap( clazz )
    }
    

    void setLoginTimeout( int i ) {
        log.trace "setLoginTimeout i = $i"
        getUnderlyingDataSource().setLoginTimeout( i )
    }
    

    int getLoginTimeout() {
        log.trace 'getLoginTimeout'
        getUnderlyingDataSource().getLoginTimeout()
    }
    

// --------------------------- end of public methods ----------------------------


    private DataSource getUnderlyingDataSource() {
        underlyingDataSource // TODO: Pick between underlyingDataSource and underlyingSSBDataSource
    }


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
        log.debug "in BannerDS.proxyConnection - proxied connection for $userName and connection $oconn"
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
        log.debug "BannerDS will set applicable role(s): ${applicableAuthorities*.authority}" 

        try {
            log.trace "BannerDS.setRoles - will unlock role(s) for the connection proxied for $proxiedUserName"
            applicableAuthorities?.each { auth -> unlockRole( oconn, (BannerGrantedAuthority) auth ) }
            log.trace "BannerDS.setRoles unlocked role(s) for the connection proxied for $proxiedUserName"
        }
        catch (e) {
            //if we cannot unlock a role, abort the proxy session and rollback
            log.error "Failed to unlock role for proxy session for Oracle connection $oconn  Exception: $e "
            closeProxySession( oconn, proxiedUserName )
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
        String stmt = "set role \"${bannerAuth.roleName}\" identified by \"${bannerAuth.bannerPassword}\"" as String
        log.trace "BannerDS.unlockRole will set role '${bannerAuth.roleName}' for connection $conn" 
        
        Sql db = new Sql( conn )        
        db.execute( stmt ) // Note: Don't close the Sql as this closes the connection, and we're preparing the connection for subsequent use
    }

}


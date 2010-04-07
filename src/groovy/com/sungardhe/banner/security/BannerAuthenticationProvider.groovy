/** *****************************************************************************

 © 2010 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
package com.sungardhe.banner.security


import org.springframework.security.providers.*
import org.springframework.security.Authentication
import org.springframework.security.GrantedAuthority
import org.springframework.security.GrantedAuthorityImpl
import org.apache.log4j.Logger
import java.sql.SQLException
import groovy.sql.Sql
import com.sungardhe.banner.db.BannerConnection
import org.codehaus.groovy.grails.commons.ConfigurationHolder as CH
import oracle.jdbc.pool.OracleDataSource

/**
 * An authentication provider which authenticates a user by logging into the Banner database.
 */
public class BannerAuthenticationProvider implements AuthenticationProvider {

    private final Logger log = Logger.getLogger( getClass() )

    def dataSource // injected by Spring
    def authenticationDataSource	// injected by Spring


    public Authentication authenticate( Authentication authentication ) {
        
        // Determine if database authentication is successful
        def conn
        try {
            log.trace "BannerAuthenticationProvider jdbc url = ${dataSource.getUrl()}"
            authenticationDataSource.setURL( dataSource.getUrl() )
            conn = authenticationDataSource.getConnection( authentication.name, authentication.credentials )
            log.trace "BannerAuthenticationProvider successfully authenticated user ${authentication.name} against data source ${dataSource.getUrl()}"
        } catch (SQLException e) {
            log.error "BannerAuthenticationProvider not able to authenticate user ${authentication.name} against data source ${dataSource.getUrl()} due to exception $e.message"
            return null
        } finally {
            conn?.close()
        }
        
        try {
            GrantedAuthority[] authorities = determineAuthorities( authentication.name.toUpperCase() )

            if (authorities) {
                def user = new BannerUser( authentication.name, authentication.credentials as String,
                                           true /*enabled*/, true /*accountNonExpired*/,
                                           true /*credentialsNonExpired*/, true /*accountNonLocked*/, authorities )
                def token = new BannerAuthenticationToken( user )

                log.trace "BannerAuthenticationProvider.authenticate authenticated user $user and is returning a token $token"
                token
            }
            else {
                log.warn "BannerAuthenticationProvider found no authorities for user $authentication.name"
                return null
            }
        }
        catch (Exception e) {
            log.warn "BannerAuthenticationProvider was not able to authenticate user $authentication.name due to exception: ${e.message}"
            return null
        }
    }


    public boolean supports( Class clazz ) {
        return clazz == UsernamePasswordAuthenticationToken
    }


    private GrantedAuthority[] determineAuthorities( String name ) {
        def conn = null
        List authorities = []
        name = name.toUpperCase()
        try {
            // We query the database for all role assignments for the user, using an unproxied connection.
            // The Banner roles are converted to an 'acegi friendly' format: e.g., ROLE_{FORM-OBJECT}_{BANNER_ROLE}
            conn = dataSource.unproxiedConnection
            Sql db = new Sql( conn )
            db.eachRow( "select * from govurol where govurol_userid = ?", [name] ) { row ->
                def authority = BannerGrantedAuthority.create( row.GOVUROL_OBJECT, row.GOVUROL_ROLE, row.GOVUROL_ROLE_PSWD )
                log.trace "BannerAuthenticationProvider.determineAuthorities is adding authority $authority"
                authorities << authority
            }
        } catch (SQLException e) {
            log.error "BannerAuthenticationProvider not able to authenticate user $name due to exception $e.message"
            return null
        } finally {
            conn?.close()
        }
        log.trace "BannerAuthenticationProvider.determineAuthorities is returning $authorities"
        authorities as GrantedAuthority[]
    }

}
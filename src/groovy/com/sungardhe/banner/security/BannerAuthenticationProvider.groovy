/** *****************************************************************************
 © 2010 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
package com.sungardhe.banner.security


import com.sungardhe.banner.db.BannerDS

import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.GrantedAuthorityImpl

import java.sql.SQLException

import groovy.sql.Sql

import org.apache.log4j.Logger

import org.codehaus.groovy.grails.commons.ConfigurationHolder as CH

import oracle.jdbc.pool.OracleDataSource
import org.jasig.cas.client.util.AbstractCasFilter
import org.springframework.web.context.request.RequestContextHolder

/**
 * An authentication provider which authenticates a user by logging into the Banner database.
 */
public class BannerAuthenticationProvider implements AuthenticationProvider {

    private static final Logger log = Logger.getLogger( getClass() )

    def dataSource // injected by Spring
    def authenticationDataSource	// injected by Spring


    public Authentication authenticate( Authentication authentication ) {

        // Determine if database authentication is successful
         // Determine if database authentication is successful
        def dbUser
        def authenticationProvider = CH?.config.banner.sso.authenticationProvider
        log.trace "authenticationProvider = $authenticationProvider"

        if ('cas'.equalsIgnoreCase( authenticationProvider )) {
            dbUser = casAuthentication()
        } else {
            dbUser = defaultAuthentication( authentication )
        }

        if (!dbUser) {
            log.warn "BannerAuthenticationProvider was not able to authenticate user."
            return null
        }

        try {
            Collection<GrantedAuthority> authorities = determineAuthorities( dbUser.toUpperCase(), dataSource )

            if (authorities) {
                def user = new BannerUser( dbUser, authentication.credentials as String,
                        true /*enabled*/, true /*accountNonExpired*/,
                        true /*credentialsNonExpired*/, true /*accountNonLocked*/, authorities as Collection, getFullName( dbUser.toUpperCase(), dataSource ) as String  )
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

    private def defaultAuthentication( Authentication authentication ) {
        def conn

        try {
            authenticationDataSource.setURL( CH?.config?.myDataSource.url )
            conn = authenticationDataSource.getConnection( authentication.name, authentication.credentials )
            log.trace "BannerAuthenticationProvider successfully authenticated user ${authentication.name} against data source ${dataSource.url}"
        } catch (SQLException e) {
            println e
            log.error "BannerAuthenticationProvider not able to authenticate user ${authentication.name} against data source ${dataSource.url} due to exception $e.message"
            return null
        } finally {
            conn?.close()
        }
        authentication.name
    }

    private def casAuthentication() {
        log.trace "BannerAuthenticationProvider doing a cas authentication"
        def attributeMap = RequestContextHolder.currentRequestAttributes().request.session.getAttribute( AbstractCasFilter.CONST_CAS_ASSERTION ).principal.attributes
        def assertAttributeValue = attributeMap[CH?.config?.banner.sso.authenticationAssertionAttribute]
        getMappedDatabaseUserForUdcId( assertAttributeValue, dataSource )
    }

    public static def getMappedDatabaseUserForUdcId( String udcId, def dataSource ) {
        def conn
        def dbUser
        try {
            log.trace "BannerAuthenticationProvider mapping for udcId = $udcId"
            conn = dataSource.unproxiedConnection
            Sql db = new Sql( conn )
            def sqlStatement = '''SELECT gobeacc_username FROM gobumap, gobeacc
                                WHERE gobumap_pidm = gobeacc_pidm AND gobumap_udc_id = ?'''
            db.eachRow( sqlStatement, [udcId] ) {row ->
                dbUser = row.gobeacc_username
            }
        } catch (SQLException e) {
            log.error "BannerAuthenticationProvider not able to map udcId $udcId to db user"
            return null
        } finally {
            conn?.close()
        }
        dbUser
    }


    public boolean supports( Class clazz ) {
        return clazz == UsernamePasswordAuthenticationToken
    }


    public static Collection<GrantedAuthority> determineAuthorities( String name, def dataSource ) {
        def conn = null
        Collection<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>()
        name = name.toUpperCase()
        try {
            // We query the database for all role assignments for the user, using an unproxied connection.
            // The Banner roles are converted to an 'acegi friendly' format: e.g., ROLE_{FORM-OBJECT}_{BANNER_ROLE}
            conn = dataSource.unproxiedConnection
            Sql db = new Sql( conn )
            db.eachRow( "select * from govurol where govurol_userid = ?", [name] ) { row ->
                def authority = BannerGrantedAuthority.create( row.GOVUROL_OBJECT, row.GOVUROL_ROLE, row.GOVUROL_ROLE_PSWD )
                // log.trace "BannerAuthenticationProvider.determineAuthorities is adding authority $authority"
                authorities << authority
            }
        } catch (SQLException e) {
            log.error "BannerAuthenticationProvider not able to authenticate user $name due to exception $e.message"
            return null
        } finally {
            conn?.close()
        }
        log.trace "BannerAuthenticationProvider.determineAuthorities is returning $authorities"
        authorities
    }


    public static getFullName ( String name, def dataSource ) {
      def conn = null
      def fullName
      Collection<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>()
      name = name.toUpperCase()
      try {
          conn = dataSource.unproxiedConnection
          Sql db = new Sql( conn )
          db.eachRow( "select  f_format_name(spriden_pidm,'FL') fullname from spriden, gobeacc where gobeacc_username = ? AND spriden_pidm = gobeacc_pidm AND  spriden_change_ind is null", [name] ) {
            row -> fullName = row.fullname
          }
          log.trace "BannerAuthenticationProvider.getFullName after checking f_formatname $fullName"
          if (null == fullName) {
            db.eachRow( "select  f_format_name(spriden_pidm,'FL') fullname  from gurlogn,spriden where gurlogn_user = ? and gurlogn_pidm = spriden_pidm", [name] ) { row ->
               fullName = row.fullname
            }
          }
          log.trace "BannerAuthenticationProvider.getFullName after checking gurlogn_pidm $fullName"
          if (null == fullName) {
            db.eachRow( "select gurlogn_first_name|| ' '||gurlogn_last_name fullname from gurlogn where gurlogn_user = ? and gurlogn_first_name is not null and gurlogn_last_name is not null", [name] ) { row ->
               fullName = row.fullname
            }
          }
          if (null == fullName) fullName = name
      } catch (SQLException e) {
          log.error "BannerAuthenticationProvider not able to getFullName $name due to exception $e.message"
          return null
      } finally {
          conn?.close()
      }
      log.trace "BannerAuthenticationProvider.getFullName is returning $fullName"
      fullName
    }

}

/** *****************************************************************************
 � 2011 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
package com.sungardhe.banner.security

import com.sungardhe.banner.db.BannerDS
import com.sungardhe.banner.service.LoginAuditService

import java.sql.SQLException

import javax.sql.DataSource

import groovy.sql.Sql

import oracle.jdbc.pool.OracleDataSource

import org.apache.log4j.Logger

import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.codehaus.groovy.grails.commons.ConfigurationHolder as CH
import org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes
import org.codehaus.groovy.grails.web.context.ServletContextHolder

import org.springframework.context.ApplicationContext
import org.springframework.security.authentication.AccountExpiredException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.CredentialsExpiredException
import org.springframework.security.authentication.DisabledException
import org.springframework.security.authentication.LockedException
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.GrantedAuthorityImpl
import org.springframework.web.context.request.RequestContextHolder


/**
 * An authentication provider which authenticates a user by logging into the Banner database.
 */
public class BannerAuthenticationProvider implements AuthenticationProvider {

    // note: using 'getClass()' here doesn't work -- hierarchical class loader issue?  Anyway, we'll just use a String
    private static final Logger log = Logger.getLogger( "com.sungardhe.banner.security.BannerAuthenticationProvider" )

    def dataSource                  // injected by Spring
    def authenticationDataSource	// injected by Spring


    public boolean supports( Class clazz ) {
        log.trace "SelfServiceBannerAuthenticationProvider.supports( $clazz ) will return ${clazz == UsernamePasswordAuthenticationToken && isAdministrativeBannerEnabled() == true}"
        clazz == UsernamePasswordAuthenticationToken && isAdministrativeBannerEnabled()
    }
    
    
    /**
     * Authenticates the user.
     * @param authentication an Authentication object containing a user's credentials
     * @return Authentication an authentication object providing authentication results and holding the user's authorities, or null
     **/
    public Authentication authenticate( Authentication authentication ) {
                
        log.trace "BannerAuthenticationProvider.authenticate invoked"

        def authenticationResults
        try {
            authenticationResults = defaultAuthentication( authentication )
        
            // Next, we'll verify the authenticationResults (and throw appropriate exceptions for expired pin, disabled account, etc.)
            // Note that when we execute this inside a try-catch block, we need to re-throw exceptions we want caught by the filter
            verifyAuthenticationResults authenticationResults
           
            def applicationContext = (ApplicationContext) ServletContextHolder.getServletContext().getAttribute( GrailsApplicationAttributes.APPLICATION_CONTEXT )
            
            if (!authenticationResults['oracleUserName']) {
                log.warn "BannerAuthenticationProvider was not able to authenticate user."
                applicationContext.publishEvent( new BannerAuthenticationEvent( authenticationResults.name, false, 'BannerAuthenticationProvider - Invalid password tried', 
                                                                                'BannerAuthenticationProvider', new Date(), 1 ) )
                return null
            }
            loadDefault( applicationContext, authenticationResults['oracleUserName'] )
            applicationContext.publishEvent( new BannerAuthenticationEvent( authenticationResults['oracleUserName'], true, '', '', new Date(), '' ) )
            authenticationResults['authorities'] = (Collection<GrantedAuthority>) determineAuthorities( authenticationResults['oracleUserName'].toUpperCase(), dataSource )
            authenticationResults['fullName'] = getFullName( authenticationResults.name.toUpperCase(), dataSource ) as String            
            newAuthenticationToken( this, authenticationResults )
        }
        catch (DisabledException de) {
            log.warn "BannerAuthenticationProvider was not able to authenticate user $authentication.name, due to exception: ${de.message}"
            throw de
        }
        catch (CredentialsExpiredException ce) {
            log.warn "BannerAuthenticationProvider was not able to authenticate user $authentication.name, due to exception: ${ce.message}"
            throw ce
        }
        catch (LockedException le) {
            log.warn "BannerAuthenticationProvider was not able to authenticate user $authentication.name, due to exception: ${le.message}"
            throw le
        }
        catch (BadCredentialsException be) {
            log.warn "BannerAuthenticationProvider was not able to authenticate user $authentication.name, due to exception: ${be.message}"
            throw be // NOTE: If we decide to add another provider after this one, we 'may' want to return null here...
        }
        catch (e) {
            // We don't expect an exception here, as failed authentication should be reported via the above exceptions
            log.error "BannerAuthenticationProvider was not able to authenticate user $authentication.name, due to exception: ${e.message}"
            return null // this is a rare situation where we want to bury the exception - we *need* to return null
        }
    }
    
    
    /**
     * Throws appropriate Spring Security exceptions for disabled accounts, locked accounts, expired pin, 
     * @throws DisabledException if account is disabled
     * @throws CredentialsExpiredException if credential is expired
     * @throws LockedException if the user account has been locked
     * @throws RuntimeException if the pin was invalid or the id was incorrect (i.e., the default error)
     **/
    public static verifyAuthenticationResults( authenticationResults ) {
        // These will be localized later, by the LoginController
        if (authenticationResults.disabled) throw new DisabledException( '' )
        if (authenticationResults.expired)  throw new CredentialsExpiredException( '' )
        if (authenticationResults.locked)   throw new LockedException( '' )
        if (!authenticationResults.valid)   throw new BadCredentialsException( '' )
    }

    
    /**
     * Returns a new authentication object based upon the supplied arguments.  
     * This method, when used within other providers, should NOT catch the exceptions but should let them be caught by the filter.
     * @param provider the provider who needs to create a token (used for logging purposes)
     * @param authentication the initial authentication object containing credentials
     * @param authentictionResults the authentication results, including the user's Oracle database username 
     * @param authorities the user's authorities that must be included in the new authentication object
     * @throws AuthenticationException various AuthenticationException types may be thrown, and should NOT be caught by providers using this method
     **/
    public static def newAuthenticationToken( provider, authenticationResults ) { 
        
        try {
            def user = new BannerUser( authenticationResults.name,                       // username
                                       authenticationResults.credentials as String,      // password
                                       authenticationResults.oracleUserName,             // oracle username (note this may be null)
                                       !authenticationResults.disabled,                  // enabled (account)
                                       true,                                             // accountNonExpired - NOT USED
                                       !authenticationResults.expired,                   // credentialsNonExpired 
                                       true,                                             // accountNonLocked - NOT USED (YET)
                                       authenticationResults.authorities as Collection, 
                                       authenticationResults.fullName )

            def token = new BannerAuthenticationToken( user )
            log.trace "${provider?.class?.simpleName}.newAuthenticationToken is returning token $token"
            token
        } catch (e) {
            // We don't expect an exception when simply constructing the user and token, so we'll report this as an error
            log.error "BannerAuthenticationProvider.newAuthenticationToken was not able to construct a token for user $authenticationResults.name, due to exception: ${e.message}"
            return null // this is a rare situation where we want to bury the exception - we *need* to return null to allow other providers a chance...
        }        
    }


    /**
     * Returns the authorities granted for the identified user.
     **/
    public static Collection<GrantedAuthority> determineAuthorities( String oracleUserName, DataSource dataSource ) {

        def conn
        def db
        try {
            // We query the database for all role assignments for the user, using an unproxied connection.
            // The Banner roles are converted to an 'acegi friendly' format: e.g., ROLE_{FORM-OBJECT}_{BANNER_ROLE}
            conn = dataSource.unproxiedConnection
            db = new Sql( conn )
            return determineAuthorities( oracleUserName, db )
        } finally {
            conn?.close()
        }        
    }
    
    
    /**
     * Returns the authorities granted for the identified user.
     **/
    public static Collection<GrantedAuthority> determineAuthorities( String oracleUserName, Sql db ) {
        Collection<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>()
        try {
            // We query the database for all role assignments for the user, using an unproxied connection.
            // The Banner roles are converted to an 'acegi friendly' format: e.g., ROLE_{FORM-OBJECT}_{BANNER_ROLE}
            db.eachRow( "select * from govurol where govurol_userid = ?", [oracleUserName] ) { row ->
                def authority = BannerGrantedAuthority.create( row.GOVUROL_OBJECT, row.GOVUROL_ROLE, row.GOVUROL_ROLE_PSWD )
                // log.trace "BannerAuthenticationProvider.determineAuthorities is adding authority $authority"
                authorities << authority
            }
        } catch (SQLException e) {
            log.error "BannerAuthenticationProvider not able to determine Authorities for user $oracleUserName due to exception $e.message"
            return new ArrayList<GrantedAuthority>()
        } 
        log.trace "BannerAuthenticationProvider.determineAuthorities is returning ${authorities?.size()} authorities. "
        authorities
    }


    /**
     * Returns the user's full name.
     **/
    public static getFullName( String name, def dataSource ) {
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
    
    
    private def isAdministrativeBannerEnabled() {
        CH.config.administrativeBannerEnabled instanceof Boolean ? CH.config.administrativeBannerEnabled : true // default is 'true'
    }
    

    private def defaultAuthentication( Authentication authentication ) {
        def conn
        def authenticationResults
        try {
            log.trace "BannerAuthenticationProvider.defaultAuthentication invoked..."
            authenticationDataSource.setURL( dataSource.getUrl() )
            conn = authenticationDataSource.getConnection( authentication.name, authentication.credentials )
            log.trace "BannerAuthenticationProvider.defaultAuthentication was able to connect, and will create authenticationResults..."
            authenticationResults = [ name:           authentication.name, 
                                      credentials:    authentication.credentials, 
                                      oracleUserName: authentication.name,
                                      valid:          true ].withDefault { k -> false }
            log.trace "BannerAuthenticationProvider.defaultAuthentication successfully authenticated user ${authentication.name} and will return $authenticationResults"
            authenticationResults
        } 
        catch (SQLException e) {
            log.warn "BannerAuthenticationProvider not able to perform default authentication for $authentication.name due to exception $e.message"
            if (e.getErrorCode() == 01017) throw new BadCredentialsException( e.message )
            else                            throw e
        } 
        finally {
            conn?.close()
        }
    }

    private void loadDefault( ApplicationContext appContext, def userName ) {
        appContext.getBean("defaultLoaderService").loadDefault( userName )
    }

}

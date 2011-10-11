/*********************************************************************************
 Copyright 2009-2011 SunGard Higher Education. All Rights Reserved.
 This copyrighted software contains confidential and proprietary information of 
 SunGard Higher Education and its subsidiaries. Any use of this software is limited 
 solely to SunGard Higher Education licensees, and is further subject to the terms 
 and conditions of one or more written license agreements between SunGard Higher 
 Education and the licensee in question. SunGard is either a registered trademark or
 trademark of SunGard Data Systems in the U.S.A. and/or other regions and/or countries.
 Banner and Luminis are either registered trademarks or trademarks of SunGard Higher 
 Education in the U.S.A. and/or other regions and/or countries.
 **********************************************************************************/
package com.sungardhe.banner.security


import com.sungardhe.banner.db.BannerDS
import com.sungardhe.banner.service.LoginAuditService

import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.AccountExpiredException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.CredentialsExpiredException
import org.springframework.security.authentication.DisabledException
import org.springframework.security.authentication.LockedException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.GrantedAuthorityImpl
import org.springframework.web.context.request.RequestContextHolder

import java.sql.SQLException

import groovy.sql.Sql

import oracle.jdbc.pool.OracleDataSource
import oracle.jdbc.driver.OracleTypes

import org.apache.log4j.Logger

import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.codehaus.groovy.grails.commons.ConfigurationHolder as CH
import org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes
import org.codehaus.groovy.grails.web.context.ServletContextHolder

import org.jasig.cas.client.util.AbstractCasFilter

import org.springframework.context.ApplicationContext


/**
 * An authentication provider which authenticates a self service user.  Self service users
 * need not have an oracle login..
 */
public class SelfServiceBannerAuthenticationProvider implements AuthenticationProvider {

    private static final Logger log = Logger.getLogger( "com.sungardhe.banner.security.SelfServiceBannerAuthenticationProvider" )

    def dataSource	// injected by Spring
    
    
    // a cached map of web roles to their configured timeout values, that is populated on first need
    private static roleBasedTimeOutsCache = [:]
    private static Integer defaultWebSessionTimeout // will be read from configuration

    public boolean supports( Class clazz ) {
        log.trace "SelfServiceBannerAuthenticationProvider.supports( $clazz ) will return ${clazz == UsernamePasswordAuthenticationToken && isSsbEnabled() && !isCasEnabled()}"
        clazz == UsernamePasswordAuthenticationToken && isSsbEnabled() && !isCasEnabled()
    }
    
    
    /**
     * Authenticates the user represented by the supplied authentication.
     * @param authentication an authentication object containing authentication information
     * @return Authentication an authentication token for the now-authenticated user
     **/
    public Authentication authenticate( Authentication authentication ) {
        
        log.trace "SelfServiceBannerAuthenticationProvider asked to authenticate $authentication"
        def conn
        try {
            if ('cas'.equalsIgnoreCase( CH?.config.banner.sso.authenticationProvider )) {
                log.trace "SelfServiceBannerAuthenticationProvider will not authenticate user since CAS is enabled"
                return null
            }
            conn = dataSource.getSsbConnection()                
            Sql db = new Sql( conn ) 
            
            def authenticationResults = selfServiceAuthentication( authentication, db ) // may throw exceptions, like SQLException

            // Next, we'll verify the authenticationResults (and throw appropriate exceptions for expired pin, disabled account, etc.)
            // Note that we execute this outside of a try-catch block, to let the exceptions be caught by the filter
            BannerAuthenticationProvider.verifyAuthenticationResults authenticationResults
            
            authenticationResults['authorities'] = (Collection<GrantedAuthority>) determineAuthorities( authentication, authenticationResults, db )
            authenticationResults['webTimeout']  = getWebTimeOut( authenticationResults, db ) 
            setWebSessionTimeout( authenticationResults['webTimeout'] )
            authenticationResults['fullName']    = getFullName( authenticationResults.name.toUpperCase(), dataSource ) as String
            newAuthenticationToken( authenticationResults )
        }
        catch (DisabledException de) {
            log.warn "SelfServiceBannerAuthenticationProvider was not able to authenticate user $authentication.name, due to exception: ${de.message}"
            throw de
        }
        catch (CredentialsExpiredException ce) {
            log.warn "SelfServiceBannerAuthenticationProvider was not able to authenticate user $authentication.name, due to exception: ${ce.message}"
            throw ce
        }
        catch (LockedException le) {
            log.warn "SelfServiceBannerAuthenticationProvider was not able to authenticate user $authentication.name, due to exception: ${le.message}"
            throw le
        }
        catch (BadCredentialsException be) {
            log.warn "SelfServiceBannerAuthenticationProvider was not able to authenticate user $authentication.name, but another provider may be able to..."
            return null // Other providers follow this one, and returning null will give them an opportunity to authenticate the user
        }
        catch (e) {
            // We'll bury other exceptions (e.g., we'll get a SQLException because the user couldn't be found) 
            log.warn "SelfServiceBannerAuthenticationProvider was not able to authenticate user $authentication.name, but another provider may be able to..."
            return null // again, we'll return null to allow other providers a chance to authenticate the user
        } finally {
            conn?.close()
        }
    }


    public static getFullName ( String name, dataSource ) {        
        BannerAuthenticationProvider.getFullName( name, dataSource )
    }


// ------------------------------- Helper Methods ------------------------------


    public static def isSsbEnabled() {
        CH.config.ssbEnabled instanceof Boolean ? CH.config.ssbEnabled : false
    }
    
    
    public static def isCasEnabled() {
        def ssoConfig = CH.config.banner.sso.authenticationProvider instanceof String || CH.config.banner.sso.authenticationProvider instanceof GString ? CH.config.ssbEnabled : ''
        'cas' == ssoConfig
    }
    

    def selfServiceAuthentication( Authentication authentication, db )  {
        def pidm
        def errorStatus
        def expirationDate
        def displayUsage
        def oracleUserName
        def valid
        
        db.call( "{call gokauth.p_authenticate(?,?,?,?,?,?,?)}",
            [ authentication.name,
              authentication.credentials,
              Sql.INTEGER,  // pidm
              Sql.INTEGER,  // status
              Sql.DATE,     // expirationDate
              Sql.VARCHAR,  // displayUsage
              Sql.VARCHAR   // Oracle username
            ]
            ) { out_pidm, status, expiration_date, display_usage,user_name ->
            pidm = out_pidm
            errorStatus = status
            expirationDate = expiration_date
            displayUsage = display_usage
            oracleUserName = user_name
        }

        def authenticationResults = [ name: authentication.name, credentials: authentication.credentials,
                                      pidm: pidm, oracleUserName: oracleUserName ].withDefault { k -> false }
        switch (errorStatus) {
            case -20101:
                log.error "SelfServiceAuthenticationProvider failed on invalid login id/pin"
                authenticationResults.valid = false
                break
            case -20112:
                log.error "SelfServiceAuthenticationProvider failed on deceased user"
                authenticationResults.deceased = true
                break
            case -20105:
                log.error "SelfServiceAuthenticationProvider failed on disabled pin"
                authenticationResults.disabled = true
                break
            case -20901:
                log.error "SelfServiceAuthenticationProvider failed on expired pin"
                authenticationResults.expired = true
                break
            case -20903:
                log.error "SelfServiceAuthenticationProvider failed on ldap authentication"
                authenticationResults.valid = false
                break
            case 0:
                authenticationResults.valid = true
                break
        }
        authenticationResults
    }
    
    
    def newAuthenticationToken( authenticationResults ) {
       BannerAuthenticationProvider.newAuthenticationToken( this, authenticationResults )
     //   newSelfServiceAuthenticationToken(  this, authenticationResults )
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
    public static def newSelfServiceAuthenticationToken( provider, authenticationResults ) {

        try {
            def user = new BannerUser( authenticationResults.name,                       // username
                                       authenticationResults.credentials as String,      // password
                                       authenticationResults.oracleUserName,             // oracle username (note this may be null)
                                       !authenticationResults.disabled,                  // enabled (account)
                                       true,                                             // accountNonExpired - NOT USED
                                       !authenticationResults.expired,                   // credentialsNonExpired
                                       true,                                             // accountNonLocked - NOT USED (YET)
                                       authenticationResults.authorities as Collection,
                                       authenticationResults.fullName,
                                       authenticationResults.pidm,
                                       authenticationResults.webTimeout
                                       )

            def token = new BannerAuthenticationToken( user )
            log.trace "${provider?.class?.simpleName}.newAuthenticationToken is returning token $token"
            token
        } catch (e) {
            // We don't expect an exception when simply constructing the user and token, so we'll report this as an error
            log.error "BannerAuthenticationProvider.newAuthenticationToken was not able to construct a token for user $authenticationResults.name, due to exception: ${e.message}"
            return null // this is a rare situation where we want to bury the exception - we *need* to return null to allow other providers a chance...
        }
    }
    
    
    def getPidm( authentication, db ) {
        
        def pidm
        db.call( "{? = call twbkslib.f_fetchpidm(?)}", [ Sql.INTEGER, authentication.name ] ) { fetchedPidm ->
           pidm = fetchedPidm
        }
        if (!pidm) throw new RuntimeException( "No PIDM found for ${authentication.name}" )
        log.trace "SelfServiceAuthenticationProvider.getPidm found PIDM $pidm for user ${authentication.name}"
        pidm
    }
    
    
     public static Collection<GrantedAuthority>determineAuthorities( Authentication authentication, Map authentictionResults, Sql db ) {

        Collection<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>()
        
        def rows = db.rows( 
            """select twgrrole_pidm,twgrrole_role from twgrrole 
                       where twgrrole_pidm = :pidm
                union
                select govrole_pidm,twtvrole_code from govrole,twtvrole,twgrrole
                       where govrole_faculty_ind = 'Y' and govrole_pidm = :pidm and twtvrole_code = 'FACULTY'
                union
                select govrole_pidm,twtvrole_code from govrole,twtvrole,twgrrole
                       where govrole_student_ind = 'Y' and govrole_pidm = :pidm and twtvrole_code = 'STUDENT'
                union
                select govrole_pidm,twtvrole_code from govrole,twtvrole,twgrrole
                       where govrole_employee_ind = 'Y' and govrole_pidm = :pidm and twtvrole_code = 'EMPLOYEE'
                union
                select govrole_pidm,twtvrole_code from govrole,twtvrole,twgrrole
                       where govrole_alumni_ind = 'Y' and govrole_pidm = :pidm and twtvrole_code = 'ALUMNI'
                union
                select govrole_pidm,twtvrole_code from govrole,twtvrole,twgrrole
                       where govrole_friend_ind = 'Y' and govrole_pidm = :pidm and twtvrole_code = 'FRIEND'
                union
                select govrole_pidm,twtvrole_code from govrole,twtvrole,twgrrole
                       where govrole_finaid_ind = 'Y' and govrole_pidm = :pidm and twtvrole_code = 'FINAID'
                union
                select govrole_pidm,twtvrole_code from govrole,twtvrole,twgrrole 
                       where govrole_finance_ind = 'Y' and govrole_pidm = :pidm and twtvrole_code = 'FINANCE'
            """, [ pidm: authentictionResults.pidm ] ) 
                    
        rows?.each { row ->    
            authorities << BannerGrantedAuthority.create( "SELFSERVICE-$row.TWGRROLE_ROLE", "BAN_DEFAULT_M", null )
        }
        
        def selfServiceRolePassword
        if (authentictionResults.oracleUserName) {            
            Collection<GrantedAuthority> adminAuthorities = BannerAuthenticationProvider.determineAuthorities( authentictionResults.oracleUserName, db )
            if (adminAuthorities.size() > 0) {
                selfServiceRolePassword = adminAuthorities[0].bannerPassword // we'll just grab the password from the first administrative role...
            }
            authorities.addAll( adminAuthorities )
        }
        
        // Users should be given the 'ROLE_SELFSERVICE_BAN_DEFAULT_M' role to have access to self service pages
        // that are associated to the 'SELFSERVICE' FormContext. 
        if (authorities.size() > 0) authorities << BannerGrantedAuthority.create( "SELFSERVICE", "BAN_DEFAULT_M", selfServiceRolePassword ) 
        
        log.trace "SelfServiceAuthenticationProvider.determineAuthorities will return $authorities"
        authorities 
    }  
    
       
    public setWebSessionTimeout( Integer timeoutSeconds ) {
        RequestContextHolder.currentRequestAttributes().session.setMaxInactiveInterval( timeoutSeconds )
    }
    
    
    public int getDefaultWebSessionTimeout() {
        
        if (!defaultWebSessionTimeout) {
            def configuredTimeout = CH.config.defaultWebSessionTimeout 
            defaultWebSessionTimeout = configuredTimeout instanceof Map ? 1500 : configuredTimeout        
        }
        defaultWebSessionTimeout
    } 
    
    
    def getWebTimeOut( authenticationResults, db ) {
        
        if (roleBasedTimeOutsCache.size() == 0) retrieveRoleBasedTimeOuts( db )
        
        // Grrrr... the 'findResults' method isn't available until Groovy 1.8.1  
        // def timeoutsForUser = authenticationResults['authorities']?.findResults { authority -> 
        //     roleBasedTimeOutsCache[authority.roleName]
        // }
        // So, we'll have to do this using each...
        
        def timeoutsForUser = [ getDefaultWebSessionTimeout() ]
        authenticationResults['authorities']?.each { authority -> 
            def timeout = roleBasedTimeOutsCache[authority.roleName]
            if (timeout) timeoutsForUser << timeout * 60 // Convert minutes to seconds....
        }                
        timeoutsForUser.max()
    }
    
    
    def retrieveRoleBasedTimeOuts( db ) {
        
        def rows = db.rows( "select twtvrole_code, twtvrole_time_out from twtvrole" ) 
        rows?.each { row ->    
            roleBasedTimeOutsCache << [ "${row.TWTVROLE_CODE}": row.TWTVROLE_TIME_OUT ]
        }
        log.debug "retrieveRoleBasedTimeOuts() has cached web role timeouts: ${roleBasedTimeOutsCache}"    
        roleBasedTimeOutsCache // we'll return this to facilitate testing of this method 
    }
      

}


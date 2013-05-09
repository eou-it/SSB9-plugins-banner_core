/*******************************************************************************
Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
*******************************************************************************/ 
package net.hedtech.banner.security

import org.springframework.security.authentication.AuthenticationProvider

import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.CredentialsExpiredException
import org.springframework.security.authentication.DisabledException
import org.springframework.security.authentication.LockedException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority

import org.springframework.web.context.request.RequestContextHolder

import java.sql.SQLException

import groovy.sql.Sql

import org.apache.log4j.Logger

import org.codehaus.groovy.grails.commons.ConfigurationHolder as CH

/**
 * An authentication provider which authenticates a self service user.  Self service users
 * need not have an oracle login..
 */
public class SelfServiceBannerAuthenticationProvider implements AuthenticationProvider {

    private static final Logger log = Logger.getLogger( "net.hedtech.banner.security.SelfServiceBannerAuthenticationProvider" )

    def dataSource	// injected by Spring


    // a cached map of web roles to their configured timeout values, that is populated on first need
    private static roleBasedTimeOutsCache = [:]
    private static Integer defaultWebSessionTimeout // will be read from configuration

    public boolean supports( Class clazz ) {
        log.trace "SelfServiceBannerAuthenticationProvider.supports( $clazz ) will return ${clazz == UsernamePasswordAuthenticationToken && isSsbEnabled()}"
        clazz == UsernamePasswordAuthenticationToken && isSsbEnabled()
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
            BannerAuthenticationProvider.verifyAuthenticationResults this, authentication, authenticationResults

            authenticationResults['authorities']        = (Collection<GrantedAuthority>) determineAuthorities( authenticationResults, db )
            authenticationResults['webTimeout']         = getWebTimeOut( authenticationResults, db )
            authenticationResults['transactionTimeout'] = getTransactionTimeout()
            authenticationResults['fullName']           = getFullName( authenticationResults, dataSource ) as String
            setWebSessionTimeout(  authenticationResults['webTimeout'] )
            setTransactionTimeout( authenticationResults['transactionTimeout'] )

            newAuthenticationToken( authenticationResults )
        }
        catch (DisabledException de)           { throw de }
        catch (CredentialsExpiredException ce) { throw ce }
        catch (LockedException le)             { throw le }
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




// ------------------------------- Helper Methods ------------------------------


    public static def isSsbEnabled() {
        CH.config.ssbEnabled instanceof Boolean ? CH.config.ssbEnabled : false
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
        db.commit() //Added to make sure db updates are committed
        def authenticationResults = [ name: authentication.name, credentials: authentication.credentials,
                                      pidm: pidm, oracleUserName: oracleUserName ].withDefault { k -> false }
        switch (errorStatus) {
            case -20101:
                log.debug "SelfServiceAuthenticationProvider failed on invalid login id/pin"
                authenticationResults.valid = false
                break
            case -20112:
                log.debug "SelfServiceAuthenticationProvider failed on deceased user"
                authenticationResults.deceased = true
                break
            case -20105:
                log.debug "SelfServiceAuthenticationProvider failed on disabled pin"
                authenticationResults.disabled = true
                break
            case -20901:
                log.debug "SelfServiceAuthenticationProvider failed on expired pin"
                authenticationResults.expired = true
                break
            case -20903:
                log.debug "SelfServiceAuthenticationProvider failed on ldap authentication"
                authenticationResults.valid = false
                break
            case 0:
                authenticationResults.valid = true
                break
        }

        if (!authenticationResults.valid) {
            if (guestAuthenticationSuccessful( authentication, db , authenticationResults )) {
                authenticationResults.valid = true
                authenticationResults.guest = true
            }
        }
        authenticationResults
    }


    private static getFullName( authenticationResults, dataSource ) {
      def conn = null
      def fullName

      def name = authenticationResults.name.toUpperCase()
      def pidm = authenticationResults.pidm
      try {
          conn = dataSource.unproxiedConnection
          Sql db = new Sql( conn )
          db.eachRow( "select  f_format_name(spriden_pidm,'FMIL') fullname from spriden where spriden_pidm = ?", [pidm] ) {
            row -> fullName = row.fullname
          }
          log.trace "SelfServiceAuthenticationProvider.getFullName after checking f_formatname $fullName"

          if (null == fullName) fullName = name
      } catch (SQLException e) {
          log.error "SelfServiceAuthenticationProvider not able to getFullName $name due to exception $e.message"
          return null
      } finally {
          conn?.close()
      }
      log.trace "SelfServiceAuthenticationProvider.getFullName is returning $fullName"
      fullName
    }



    private boolean guestAuthenticationSuccessful( Authentication authentication, db, authenticationResults ) {
        if (!isGuestAuthenticationEnabled()) return false
        log.debug "guestAuthenticationSuccessful()  "
        def salt
        def hashPin
        def gidm
        def userPin
        def firstName
        def lastName
        db.eachRow( """select gpbprxy_salt, gpbprxy_pin,gpbprxy_first_name,gpbprxy_last_name,gpbprxy_proxy_idm
                       from gpbprxy where  NVL(TRUNC(GPBPRXY_PIN_EXP_DATE), TRUNC(SYSDATE)) >= TRUNC(SYSDATE) AND GPBPRXY_PIN_DISABLED_IND = 'N' AND GPBPRXY_EMAIL_ADDRESS = ?""", [authentication.name] ) {
            salt = it.gpbprxy_salt
            hashPin = it.gpbprxy_pin
            gidm = it.gpbprxy_proxy_idm
            firstName = it.gpbprxy_first_name
            lastName = it.gpbprxy_last_name
        }
        if (null == hashPin) return false
        db.call( "{call gspcrpt.p_saltedhash(?,?,?)}", [
            authentication.credentials,
            salt ,
            Sql.VARCHAR
            ]
            ) {
            userpasswd -> userPin = userpasswd}
        if (userPin == hashPin)  {
            authenticationResults.gidm = gidm
            authenticationResults.fullName =  firstName + " " + lastName
            return true
        }
        false
    }


    private boolean isGuestAuthenticationEnabled() {
        CH.config.guestAuthenticationEnabled instanceof Boolean ? CH.config.guestAuthenticationEnabled : false
    }


    def newAuthenticationToken( authenticationResults ) {
        BannerAuthenticationProvider.newAuthenticationToken( this, authenticationResults )
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


     public static Collection<GrantedAuthority>determineAuthorities( Map authentictionResults, Sql db ) {

        Collection<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>()

        if (authentictionResults.guest) {
            authorities << BannerGrantedAuthority.create( "SELFSERVICE-GUEST", "BAN_DEFAULT_M", null )
        }

        def rows
        if (authentictionResults.pidm) {
            rows = db.rows(
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
        }

       // def selfServiceRolePassword
        if (authentictionResults.oracleUserName) {
            Collection<GrantedAuthority> adminAuthorities = BannerAuthenticationProvider.determineAuthorities( authentictionResults, db )
            authorities.addAll( adminAuthorities )
        }

        // Users should be given the 'ROLE_SELFSERVICE_BAN_DEFAULT_M' role to have access to self service pages
        // that are associated to the 'SELFSERVICE' FormContext.
        //if (authorities.size() > 0) authorities << BannerGrantedAuthority.create( "SELFSERVICE", "BAN_DEFAULT_M", selfServiceRolePassword )

        log.trace "SelfServiceAuthenticationProvider.determineAuthorities will return $authorities"
        authorities
    }


    public setWebSessionTimeout( Integer timeoutSeconds ) {
        RequestContextHolder.currentRequestAttributes().session.setMaxInactiveInterval( timeoutSeconds )
    }



    public setTransactionTimeout( timeoutSeconds ) {
        RequestContextHolder.currentRequestAttributes().session.transactionTimeout = timeoutSeconds
    }

    def getTransactionTimeout() {
        def timeoutSeconds = CH.config.banner?.transactionTimeout instanceof Integer ? CH.config.banner?.transactionTimeout : 30
        timeoutSeconds
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


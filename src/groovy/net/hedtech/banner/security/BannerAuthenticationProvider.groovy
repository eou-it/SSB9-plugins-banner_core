/*******************************************************************************
 Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.security

import grails.util.GrailsNameUtils
import grails.util.Holders  as CH
import grails.util.Holders  as AH
import groovy.sql.Sql
import net.hedtech.banner.controllers.ControllerUtils
import org.apache.log4j.Logger
import org.springframework.context.ApplicationContext
import org.springframework.security.authentication.*
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority

import javax.sql.DataSource
import java.sql.SQLException

/**
 * An authentication provider which authenticates a user by logging into the Banner database.
 */
public class BannerAuthenticationProvider implements AuthenticationProvider {

    // note: using 'getClass()' here doesn't work -- we'll just use a String
    private static final Logger log = Logger.getLogger( "net.hedtech.banner.security.BannerAuthenticationProvider" )

    private static def applicationContext // set lazily via 'getApplicationContext()'

    def dataSource               // injected by Spring
    def authenticationDataSource // injected by Spring

    public boolean supports( Class clazz ) {
        log.trace "BannerAuthenticationProvider.supports( $clazz ) will return ${clazz == UsernamePasswordAuthenticationToken && isAdministrativeBannerEnabled() == true}"
        clazz == UsernamePasswordAuthenticationToken && isAdministrativeBannerEnabled()
    }


    /**
     * Authenticates the user.
     * @param authentication an Authentication object containing a user's credentials
     * @return Authentication an authentication object providing authentication results and holding the user's authorities, or null
     **/
    public Authentication authenticate( Authentication authentication ) {

        log.trace "BannerAuthenticationProvider.authenticate invoked"

        try {
            def authenticationResults = defaultAuthentication( authentication )

            // Next, we'll verify the authenticationResults (and throw appropriate exceptions for expired pin, disabled account, etc.)
            // Note that when we execute this inside a try-catch block, we need to re-throw exceptions we want caught by the filter
            verifyAuthenticationResults this, authentication, authenticationResults

            loadDefault( getApplicationContext(), authenticationResults['oracleUserName'] )
            getApplicationContext().publishEvent( new BannerAuthenticationEvent( authenticationResults['oracleUserName'], true, '', '', new Date(), '' ) )

            authenticationResults['authorities'] = (Collection<GrantedAuthority>) determineAuthorities( authenticationResults, dataSource )

            def pidm=AuthenticationProviderUtility.getUserPidm(authenticationResults.name.toUpperCase(),dataSource);

            if(pidm!=null){
                authenticationResults['pidm']=pidm;
                authenticationResults['fullName']=AuthenticationProviderUtility.getUserFullName(pidm,authenticationResults.name,dataSource);

            }else{
                authenticationResults['fullName'] = AuthenticationProviderUtility.getFullName( authenticationResults.name.toUpperCase(), dataSource ) as String
            }

            AuthenticationProviderUtility.newAuthenticationToken( this, authenticationResults )

        }
        catch (DisabledException de)           { throw de }
        catch (CredentialsExpiredException ce) { throw ce }
        catch (LockedException le)             { throw le }
        catch (BadCredentialsException be)     { throw be } // NOTE: If we decide to add another provider after this one, we 'may' want to return null here...
        catch (e) {
            // We don't expect an exception here, as failed authentication should be reported via the above exceptions
            log.error "BannerAuthenticationProvider was not able to authenticate user $authentication.name, due to exception: ${e.message}"
            return null // this is a rare situation where we want to bury the exception - we *need* to return null
        }
    }


    public static def getApplicationContext() {
        if (!applicationContext) {
            applicationContext = (ApplicationContext) AH.grailsApplication.getMainContext()
        }
        applicationContext
    }


    /**
     * Throws appropriate Spring Security exceptions for disabled accounts, locked accounts, expired pin,
     * @throws DisabledException if account is disabled
     * @throws CredentialsExpiredException if credential is expired
     * @throws LockedException if the user account has been locked
     * @throws RuntimeException if the pin was invalid or the id was incorrect (i.e., the default error)
     **/
    public static verifyAuthenticationResults( AuthenticationProvider provider, Authentication authentication, Map authenticationResults ) {

        def report = BannerAuthenticationProvider.&handleFailure.curry( provider, authentication, authenticationResults )

        if (authenticationResults.disabled) report( new DisabledException('') )
        if (authenticationResults.expired)  report( new CredentialsExpiredException('') )
        if (authenticationResults.locked)   report( new LockedException('') )
        if (!authenticationResults.valid)   report( new BadCredentialsException('') )
    }


    private static handleFailure( provider, authentication, authenticationResults, exception ) {

        log.warn "${provider.class.simpleName} was not able to authenticate user $authentication.name due to exception ${exception.class.simpleName}: ${exception.message} "
        def msg = GrailsNameUtils.getNaturalName( GrailsNameUtils.getLogicalName( exception.class.simpleName, "Exception" ) )
        def module = GrailsNameUtils.getNaturalName( GrailsNameUtils.getLogicalName( provider.class.simpleName, "AuthenticationProvider" ) )
        getApplicationContext().publishEvent( new BannerAuthenticationEvent( authentication.name, false, msg, module, new Date(), 1 ) )
        throw exception
    }


    /**
     * Returns the authorities granted for the identified user.
     **/
    public static Collection<GrantedAuthority> determineAuthorities( Map authenticationResults, DataSource dataSource ) {
        return BannerGrantedAuthorityService.determineAuthorities (authenticationResults, dataSource)
    }


    /**
     * Returns the authorities granted for the identified user.
     **/
    public static Collection<GrantedAuthority> determineAuthorities( Map authenticationResults, Sql db ) {
        return BannerGrantedAuthorityService.determineAuthorities (authenticationResults, db)
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
            if (null == fullName) {
                db.eachRow( "select f_format_name(spriden_pidm,'FMIL') fullname from spriden where spriden_id = ?", [name] ) {
                    row -> fullName = row.fullname
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
        def authenticationResults = [:]
        try {
            log.trace "BannerAuthenticationProvider.defaultAuthentication invoked..."
            authenticationDataSource.setURL( dataSource.getUrl() )
            conn = authenticationDataSource.getConnection( authentication.name, authentication.credentials )
            log.trace "BannerAuthenticationProvider.defaultAuthentication was able to connect, and will create authenticationResults..."
            authenticationResults = [ name:           authentication.name,
                    credentials:    authentication.credentials,
                    oracleUserName: authentication.name,
                    valid:          true ].withDefault { k -> false }
            log.trace "BannerAuthenticationProvider.defaultAuthentication successfully authenticated user ${authentication.name} and will return authenticationResults[name:${authentication.name},credentials:{PROTECTED},oracleUserName:${authentication.name},valid:true]"
            authenticationResults
        }
        catch (SQLException e) {
            switch (e.getErrorCode()) {
                case 1017 : // 'Invalid userName/password'
                    authenticationResults.valid = false
                    break
                case 28000 : // 'Locked account'
                    authenticationResults.locked = true
                    break
                case 28001 : // 'Expired password'
                    authenticationResults.expired = true
                    break
                default :
                    authenticationResults.valid = false
                    break
            }
            authenticationResults
        }
        finally {
            conn?.close()
        }
    }

    private void loadDefault( ApplicationContext appContext, def userName ) {
        appContext.getBean("defaultLoaderService").loadDefault( userName )
    }

}


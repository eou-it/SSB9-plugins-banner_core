/*******************************************************************************
 Copyright 2009-2020 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.security

import grails.util.Holders
import groovy.sql.Sql
import groovy.util.logging.Slf4j
import org.springframework.context.ApplicationContext
import org.springframework.security.authentication.*
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import javax.sql.DataSource
import java.sql.SQLException

/**
 * An authentication provider which authenticates a user by logging into the Banner database.
 */
@Slf4j
public class BannerAuthenticationProvider implements AuthenticationProvider {

    private static def applicationContext // set lazily via 'getApplicationContext()'
    private static Map<String,DetermineAuthoritiesCacheItem> determineAuthorities_cache = new HashMap<String,DetermineAuthoritiesCacheItem>();
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
            AuthenticationProviderUtility.verifyAuthenticationResults this, authentication, authenticationResults

            loadDefault( getApplicationContext(), authenticationResults['oracleUserName'] )
            getApplicationContext().publishEvent( new BannerAuthenticationEvent( authenticationResults['oracleUserName'], true, '', '', new Date(), '' ) )

            Boolean authorityCachingEnabled = Holders?.config?.authorityCachingEnabled instanceof Boolean ? Holders?.config?.authorityCachingEnabled : false
            log.debug "Authority Caching Enable = {}", authorityCachingEnabled
            if(authorityCachingEnabled){
                authenticationResults['authorities'] = (Collection<GrantedAuthority>) cached_determineAuthorities( authenticationResults, dataSource )
            } else {
                authenticationResults['authorities'] = (Collection<GrantedAuthority>) determineAuthorities( authenticationResults, dataSource )
            }
            def pidm=AuthenticationProviderUtility.getUserPidm(authenticationResults.name.toUpperCase(),dataSource)

            if(pidm!=null){
                authenticationResults['fullName']=AuthenticationProviderUtility.getUserFullName(pidm,authenticationResults.name,dataSource)

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
            applicationContext = (ApplicationContext) Holders.grailsApplication.getMainContext()
        }
        applicationContext
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

    static private class DetermineAuthoritiesCacheItem
    {
        public Collection<GrantedAuthority> authorities
        public String key
        public long expiration
    }

    public static Collection<GrantedAuthority> cached_determineAuthorities (Map authenticationResults, DataSource dataSource ) {
        String key=authenticationResults["oracleUserName"]
        DetermineAuthoritiesCacheItem cacheItem = null
        if (determineAuthorities_cache.containsKey(key))
        {
            cacheItem = determineAuthorities_cache[key]
            if (System.currentTimeMillis() > cacheItem.expiration)
                cacheItem = null
        }
        if (cacheItem==null)
        {
            log.trace "cached_determineAuthorities miss"
            cacheItem = new DetermineAuthoritiesCacheItem()
            cacheItem.key = key
            cacheItem.expiration  = System.currentTimeMillis() + 5*60*1000   //5 minutes
            cacheItem.authorities = BannerGrantedAuthorityService.determineAuthorities (authenticationResults, dataSource)
            determineAuthorities_cache[key] = cacheItem
        }
        else {
            log.trace "cached_determineAuthorities hit"
        }
        return cacheItem.authorities
    }


    /**
     * Returns the user's full name.
     **/
    public static getFullName( String name, def dataSource ) {
        def conn = null
        def fullName
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
        Holders.config.administrativeBannerEnabled instanceof Boolean ? Holders.config.administrativeBannerEnabled : true // default is 'true'
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


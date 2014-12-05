/*******************************************************************************
Copyright 2009-2014 Ellucian Company L.P. and its affiliates.
*******************************************************************************/ 
package net.hedtech.banner.security

import java.sql.SQLException
import groovy.sql.Sql
import org.apache.log4j.Logger
import org.jasig.cas.client.util.AbstractCasFilter
import org.codehaus.groovy.grails.commons.ConfigurationHolder as CH
import org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes
import org.codehaus.groovy.grails.web.context.ServletContextHolder

import org.springframework.context.ApplicationContext
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.CredentialsExpiredException
import org.springframework.security.authentication.DisabledException
import org.springframework.security.authentication.LockedException
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.core.Authentication
import org.springframework.web.context.request.RequestContextHolder as RCH


/**
 * An authentication provider for Banner that authenticates a user using CAS.
 */
public class CasAuthenticationProvider implements AuthenticationProvider {

    // note: using 'getClass()' here doesn't work
    private static final Logger log = Logger.getLogger( "net.hedtech.banner.security.CasAuthenticationProvider" )

    def dataSource  // injected by Spring


    public boolean supports( Class clazz ) {
        log.trace "CasBannerAuthenticationProvider.supports( $clazz ) will return ${isCasEnabled()}"
        isCasEnabled() && isNotExcludedFromSSO()
    }
    
    
    public boolean isNotExcludedFromSSO() {
        def theUrl = RCH.currentRequestAttributes().request.forwardURI
        def excludedUrlPattern = CH?.config.banner.sso.excludedUrlPattern.toString() // e.g., 'guest'
        !("$theUrl".contains( excludedUrlPattern ))
    }
    
    
    public boolean isCasEnabled() {
        def casEnabled = CH?.config.banner.sso.authenticationProvider
        'cas'.equalsIgnoreCase( casEnabled )
    }
    
    
    public static def isSsbEnabled() {
        SelfServiceBannerAuthenticationProvider.isSsbEnabled()
    }
    
    
    public Authentication authenticate( Authentication authentication ) {
        log.trace "CasAuthenticationProvider.authenticate invoked for ${authentication.name}"

        def conn
        try {
            conn = dataSource.unproxiedConnection
            Sql db = new Sql( conn ) 
            
            def authenticationResults = casAuthentication( authentication, db )
            
            // Next, we'll verify the authenticationResults (and throw appropriate exceptions for expired pin, disabled account, etc.)
            log.debug "CasAuthenticationProvider.authenticate verify authentication results"
            BannerAuthenticationProvider.verifyAuthenticationResults this, authentication, authenticationResults

            def applicationContext = (ApplicationContext) ServletContextHolder.getServletContext().getAttribute( GrailsApplicationAttributes.APPLICATION_CONTEXT )
      
            if (isSsbEnabled()) {
                authenticationResults['authorities'] = SelfServiceBannerAuthenticationProvider.determineAuthorities( authenticationResults, db )
                log.debug "CasAuthenticationProvider.authenticate found Self Service authorities $authenticationResults['authorities']"
            }
            else {
                authenticationResults['authorities'] = BannerAuthenticationProvider.determineAuthorities( authenticationResults, db )                    
                log.debug "CasAuthenticationProvider.authenticate found Admin authorities $authenticationResults['authorities']"
            }
            
            applicationContext.publishEvent( new BannerAuthenticationEvent( authenticationResults.name, true, '', '', new Date(), '' ) )
            
            authenticationResults['fullName'] = getFullName( authenticationResults.name.toUpperCase(), dataSource ) as String
            log.debug "CasAuthenticationProvider.authenticate mapped user to full name $authenticationResults['fullName']"

            newAuthenticationToken( authenticationResults )
        }
        catch (DisabledException de)           {
            log.fatal "CasAuthenticationProvider was not able to authenticate user $authentication.name, due to exception: ${de.message}"
            throw de
        } catch (CredentialsExpiredException ce) {
            log.fatal "CasAuthenticationProvider was not able to authenticate user $authentication.name, due to exception: ${ce.message}"
            throw ce
        } catch (LockedException le)             {
            log.fatal "CasAuthenticationProvider was not able to authenticate user $authentication.name, due to exception: ${le.message}"
            throw le
        } catch (BadCredentialsException be)     {
            log.fatal "CasAuthenticationProvider was not able to authenticate user $authentication.name, due to exception: ${be.message}"
            throw be
        }
        catch (e) {
            // We don't expect an exception here, as failed authentication should be reported via the above exceptions
            log.error "CasAuthenticationProvider was not able to authenticate user $authentication.name, due to exception: ${e.message}"
            return null // this is a rare situation where we want to bury the exception 
        } finally {
            conn?.close()
        }
    }
    
    
    def newAuthenticationToken( authentictionResults ) {  
        BannerAuthenticationProvider.newAuthenticationToken( this, authentictionResults )       
    }


    public static getFullName ( String name, dataSource ) {        
        BannerAuthenticationProvider.getFullName( name, dataSource )
    }


    private def casAuthentication( authentication, db ) {
        log.trace "CasAuthenticationProvider.casAuthentication doing CAS authentication"
        def attributeMap = RCH.currentRequestAttributes().request.session.getAttribute( AbstractCasFilter.CONST_CAS_ASSERTION ).principal.attributes
        def assertAttributeValue = attributeMap[CH?.config?.banner.sso.authenticationAssertionAttribute]
        def oracleUserName
        def pidm
        def spridenId
        def authenticationResults
        String accountStatus
        def proxySsb = CH.config.ssbOracleUsersProxied instanceof Boolean ? CH.config.ssbOracleUsersProxied : false
        def processSsbOracleAccount = true
        if(assertAttributeValue) {
            try {
                log.trace "CasAuthenticationProvider.casAuthentication mapping for $CH?.config?.banner.sso.authenticationAssertionAttribute = $assertAttributeValue"
            // Determine if ssb users need oracle authenticatoion
            if (isSsbEnabled() && !proxySsb  ) {
                log.trace "CasAuthenticationProvider.casAuthentication configuration values ssbOracleUsersProxied = $proxySsb"
                processSsbOracleAccount = false
            }
            // Determine if they map to a Banner Admin user
            def sqlStatement = '''SELECT gobeacc_username, gobeacc_pidm FROM gobumap, gobeacc
                                  WHERE gobumap_pidm = gobeacc_pidm AND gobumap_udc_id = ?'''
            db.eachRow( sqlStatement, [assertAttributeValue] ) { row ->
                oracleUserName = row.gobeacc_username
                pidm = row.gobeacc_pidm
            }
            if ( oracleUserName && processSsbOracleAccount ) {

                // check if the oracle user account is locked

                def sqlStatement1 = '''select account_status,lock_date from dba_users where username=?'''
                db.eachRow( sqlStatement1, [oracleUserName.toUpperCase()] ) { row ->
                    accountStatus = row.account_status
                    }
                    if ( accountStatus.contains("LOCKED")) {
                        authenticationResults = [locked : true]
                        log.debug "CasAuthenticationProvider.casAuthentication Oracle database user $oracleUserName is Locked"
                    } else {
                        authenticationResults = [ name: oracleUserName, pidm: pidm, oracleUserName: oracleUserName, valid: true ].withDefault { k -> false }
                        log.debug "CasAuthenticationProvider.casAuthentication Oracle database user $oracleUserName is Valid"
                    }
                } else {
                    log.debug "CasAuthenticationProvider.casAuthentication No Oracle database user found"
                    // Not an Admin user, must map to a self service user
                    def sqlStatement2 = '''SELECT spriden_id, gobumap_pidm FROM gobumap,spriden WHERE spriden_pidm = gobumap_pidm AND spriden_change_ind is null AND gobumap_udc_id = ?'''
                    db.eachRow( sqlStatement2, [assertAttributeValue] ) { row ->
                        spridenId = row.spriden_id
                        pidm = row.gobumap_pidm
                    }
                    if(spridenId && pidm) {
                        log.debug "CasAuthenticationProvider.casAuthentication Spriden user $spridenId found for assertAttributeValue $assertAttributeValue and is Valid"
                    } else {
                        log.debug "CasAuthenticationProvider.casAuthentication Spriden user $spridenId found for assertAttributeValue $assertAttributeValue and is Invalid"
                    }
                    authenticationResults = [ name: spridenId, pidm: pidm, valid: (spridenId && pidm), oracleUserName: null ].withDefault { k -> false }
                }
            } catch (SQLException e) {
                log.fatal "CasAuthenticationProvider not able to map $CH?.config?.banner.sso.authenticationAssertionAttribute = $assertAttributeValue to db user"
                throw e
            }
            log.trace "CasAuthenticationProvider.casAuthentication results are $authenticationResults"
            authenticationResults
        } else {
            log.fatal("System is configured for external authentication and identity assertion is $assertAttributeValue")  // NULL
            throw new BadCredentialsException("System is configured for CAS authentication and identity assertion is $assertAttributeValue")
        }
    }

}

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

import java.sql.SQLException

import javax.sql.DataSource

import groovy.sql.Sql

import org.apache.log4j.Logger

import org.jasig.cas.client.util.AbstractCasFilter

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
import org.springframework.web.context.request.RequestContextHolder as RCH


/**
 * An authentication provider for Banner that authenticates a user using CAS.
 */
public class CasAuthenticationProvider implements AuthenticationProvider {

    // note: using 'getClass()' here doesn't work
    private static final Logger log = Logger.getLogger( "com.sungardhe.banner.security.CasAuthenticationProvider" )

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
            BannerAuthenticationProvider.verifyAuthenticationResults this, authentication, authenticationResults        
        
            def applicationContext = (ApplicationContext) ServletContextHolder.getServletContext().getAttribute( GrailsApplicationAttributes.APPLICATION_CONTEXT )
      
            if (isSsbEnabled()) {
                authenticationResults['authorities'] = SelfServiceBannerAuthenticationProvider.determineAuthorities( authenticationResults, db )
            } 
            else {
                authenticationResults['authorities'] = BannerAuthenticationProvider.determineAuthorities( authenticationResults, db )                    
            }
            
            applicationContext.publishEvent( new BannerAuthenticationEvent( authenticationResults.name, true, '', '', new Date(), '' ) )
            
            authenticationResults['fullName'] = getFullName( authenticationResults.name.toUpperCase(), dataSource ) as String
            newAuthenticationToken( authenticationResults )
        }
        catch (DisabledException de)           { throw de }
        catch (CredentialsExpiredException ce) { throw ce }
        catch (LockedException le)             { throw le }
        catch (BadCredentialsException be)     { throw be }
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
        try {
            log.trace "CasAuthenticationProvider.casAuthentication mapping for $CH?.config?.banner.sso.authenticationAssertionAttribute = $assertAttributeValue"
            // Determine if they map to a Banner Admin user
            def sqlStatement = '''SELECT gobeacc_username, gobeacc_pidm FROM gobumap, gobeacc
                                  WHERE gobumap_pidm = gobeacc_pidm AND gobumap_udc_id = ?'''
            db.eachRow( sqlStatement, [assertAttributeValue] ) { row ->
                oracleUserName = row.gobeacc_username
                pidm = row.gobeacc_pidm
            }
            if ( oracleUserName ) {
                authenticationResults = [ name: oracleUserName, pidm: pidm, oracleUserName: oracleUserName, valid: true ].withDefault { k -> false } 
            } else {
                // Not an Admin user, must map to a self service user
                def sqlStatement2 = '''SELECT spriden_id, gobumap_pidm FROM gobumap,spriden WHERE spriden_pidm = gobumap_pidm AND spriden_change_ind is null AND gobumap_udc_id = ?'''
                db.eachRow( sqlStatement2, [assertAttributeValue] ) { row ->
                    spridenId = row.spriden_id
                    pidm = row.gobumap_pidm
                }
                authenticationResults = [ name: spridenId, pidm: pidm, valid: (spridenId && pidm), oracleUserName: null ].withDefault { k -> false } 
            }
        } catch (SQLException e) {
            log.error "CasAuthenticationProvider not able to map $CH?.config?.banner.sso.authenticationAssertionAttribute = $assertAttributeValue to db user"
            throw e
        }
        log.trace "CasAuthenticationProvider.casAuthentication results are $authenticationResults"
        authenticationResults
    }
       
}

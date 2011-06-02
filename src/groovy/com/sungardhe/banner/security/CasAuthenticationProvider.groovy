/** *****************************************************************************
 © 2011 SunGard Higher Education.  All Rights Reserved.

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

import org.apache.log4j.Logger

import org.jasig.cas.client.util.AbstractCasFilter

import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.codehaus.groovy.grails.commons.ConfigurationHolder as CH
import org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes
import org.codehaus.groovy.grails.web.context.ServletContextHolder

import org.springframework.context.ApplicationContext
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.GrantedAuthorityImpl
import org.springframework.web.context.request.RequestContextHolder


/**
 * An authentication provider for Banner that authenticates a user using CAS.
 */
public class CasAuthenticationProvider implements AuthenticationProvider {

    // note: using 'getClass()' here doesn't work -- hierarchical class loader issue?  Anyway, we'll just use a String
    private static final Logger log = Logger.getLogger( "com.sungardhe.banner.security.CasAuthenticationProvider" )

    def dataSource  // injected by Spring


    public boolean supports( Class clazz ) {
        log.trace "CasBannerAuthenticationProvider.supports( $clazz ) will return ${isCasEnabled()}"
        isCasEnabled()
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

        def authenticationResults = casAuthentication( authentication )

        def applicationContext = (ApplicationContext) ServletContextHolder.getServletContext().getAttribute( GrailsApplicationAttributes.APPLICATION_CONTEXT )
 
        if (!authenticationResults.oracleUserName) {
            log.warn "CasAuthenticationProvider was not able to authenticate user."
            applicationContext.publishEvent( new BannerAuthenticationEvent( authenticationResults.name, false, 'CasAuthenticationProvider - CAS user not mapped to Oracle user', 
                                                                            'CasAuthenticationProvider', new Date(), 1 ) )
            return null
        }
        applicationContext.publishEvent( new BannerAuthenticationEvent( authenticationResults.name, true, '', '', new Date(), '' ) )
        
        try {
            authenticationResults['authorities'] = (Collection<GrantedAuthority>) BannerAuthenticationProvider.determineAuthorities( authenticationResults.oracleUserName.toUpperCase(), dataSource )
            authenticationResults['fullName'] = getFullName( authenticationResults.name.toUpperCase(), dataSource ) as String
            newAuthenticationToken( authenticationResults )
        }
        catch (Exception e) {
            log.warn "CasAuthenticationProvider was not able to authenticate user $authenticationResults.name due to exception: ${e.message}"
            return null
        }
    }
    
    
    def newAuthenticationToken( authentictionResults ) {  
        BannerAuthenticationProvider.newAuthenticationToken( this, authentictionResults )       
    }


    public static getFullName ( String name, dataSource ) {        
        BannerAuthenticationProvider.getFullName( name, dataSource )
    }


    public static def getMappedDatabaseUserForUdcId( String udcId, def dataSource ) {
        def conn
        def dbUser
        try {
            log.trace "CasAuthenticationProvider.getMappedDatabaseUserForUdcId mapping for udcId = $udcId"
            conn = dataSource.unproxiedConnection
            Sql db = new Sql( conn )
            def sqlStatement = '''SELECT gobeacc_username FROM gobumap, gobeacc
                                  WHERE gobumap_pidm = gobeacc_pidm AND gobumap_udc_id = ?'''
            db.eachRow( sqlStatement, [udcId] ) { row ->
                dbUser = row.gobeacc_username
            }
        } catch (SQLException e) {
            log.error "CasAuthenticationProvider not able to map udcId $udcId to db user"
            return null
        } finally {
            conn?.close()
        }
        dbUser
    }
    
    
    private def casAuthentication( authentication ) {
        log.trace "CasAuthenticationProvider.casAuthentication doing CAS authentication"
        def attributeMap = RequestContextHolder.currentRequestAttributes().request.session.getAttribute( AbstractCasFilter.CONST_CAS_ASSERTION ).principal.attributes
        def assertAttributeValue = attributeMap[CH?.config?.banner.sso.authenticationAssertionAttribute]
        def oracleUserName = getMappedDatabaseUserForUdcId( assertAttributeValue, dataSource )
        def authenticationResults = [ name: oracleUserName ? oracleUserName : assertAttributeValue, oracleUserName: oracleUserName ].withDefault { k -> false }
        log.trace "CasAuthenticationProvider.casAuthentication results are $authenticationResults"
        authenticationResults
    }
       
}

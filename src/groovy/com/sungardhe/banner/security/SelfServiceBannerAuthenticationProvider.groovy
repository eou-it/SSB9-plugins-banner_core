/** *****************************************************************************
 © 2010-2011 SunGard Higher Education.  All Rights Reserved.

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
import oracle.jdbc.driver.OracleTypes
import org.jasig.cas.client.util.AbstractCasFilter
import org.springframework.web.context.request.RequestContextHolder
import com.sungardhe.banner.service.LoginAuditService
import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes
import org.codehaus.groovy.grails.web.context.ServletContextHolder
import org.springframework.context.ApplicationContext

/**
 * An authentication provider which authenticates a self service user.  Self service users
 * may not have an oracle login, and are thus authenticated using Banner security versus 
 * logging into the oracle database.
 */
public class SelfServiceBannerAuthenticationProvider implements AuthenticationProvider {

    // note: using 'getClass()' here doesn't work -- hierarchical class loader issue?  Anyway, we'll just use a String
    private static final Logger log = Logger.getLogger( "com.sungardhe.banner.security.SelfServiceBannerAuthenticationProvider" )

    def dataSource	// injected by Spring


    public boolean supports( Class clazz ) {
        log.trace "SelfServiceBannerAuthenticationProvider.supports( $clazz ) will return ${clazz == UsernamePasswordAuthenticationToken && CH?.config.ssbEnabled == true}"
        clazz == UsernamePasswordAuthenticationToken && CH?.config.ssbEnabled == true
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
            
            def authentictionResults = selfServiceAuthentication( authentication, db )
            Collection<GrantedAuthority> authorities = determineAuthorities( authentication, authentictionResults, db )
            newAuthenticationToken( authentication, authentictionResults, authorities )
        }
        catch (Exception e) {
            log.error "SelfServiceBannerAuthenticationProvider.authenticate was not able to authenticate user $authentication.name due to exception: ${e.message}"
            return null // note this is a rare situation where we want to bury the exception - we need to return null
        } finally {
            conn?.close()
        }
    }


    public static getFullName ( String name, dataSource ) {        
        BannerAuthenticationProvider.getFullName( name, dataSource )
    }


// ------------------------------- Helper Methods ------------------------------
// (note: many methods are exposed with package-level accessibility to facilitate testing.)    
    

    def selfServiceAuthentication( Authentication authentication, db ) {
        
        try {
            
            def pidm = getPidm( authentication, db ) 
            def gobtpac = getGobtpac( pidm, db )
                       
            def authenticationResults = [ pidm: pidm, oracleUserName: getOracleUsername( pidm, db ), 
                                          gobtpac: gobtpac ].withDefault { k -> false }
            
            if (shouldUseLDAP( db )) {
                log.error "SelfServiceAuthenticationProvider does not currently support LDAP"
                throw new RuntimeException( "@@r1:not.yet.implemented@@" )
            }
                          
            // should not use LDAP                
            def validationResult = validatePin( pidm, authentication.credentials, db ) 
            authenticationResults << [ 'validationResult': validationResult ]
            log.trace "SelfServiceAuthenticationProvider.selfServiceAuthentication will return $authenticationResults"
                                    
            authenticationResults
        } catch (SQLException e) {
            log.error "SelfServiceBannerAuthenticationProvider.selfServiceAuthentication not able to authenticate user ${authentication.name} against data source ${dataSource.url} due to exception $e.message", e
            throw e
        }
    }
    
    
    def newAuthenticationToken( authentication, authentictionResults, authorities ) {  
        if (authorities) {
            def user = new BannerUser( authentication.name, 
                                       authentication.credentials as String,
                                       authentictionResults.oracleUserName, 
                                       !authentictionResults.validationResult.disabled /*non-disabled*/, 
                                       !authentictionResults.validationResult.expired  /*non-expired*/,
                                       true                           /*credentialsNonExpired*/, 
                                       true                           /*accountNonLocked*/, 
                                       authorities as Collection, 
                                       getFullName( authentication.name.toUpperCase(), dataSource ) as String  )
            def token = new BannerAuthenticationToken( user )
            log.trace "SelfServiceBannerAuthenticationProvider.newAuthenticationToken is returning token $token"
            token
        }
        else {
            log.warn "SelfServiceBannerAuthenticationProvider found no authorities for user $authentication.name"
            return null
        }       
    }
    
    
    def getPidm( authentication, db ) {
        
        def pidm
        db.call( "{? = call twbkslib.f_fetchpidm(?)}", [ Sql.INTEGER, authentication.name ] ) { fetchedPidm ->
           pidm = fetchedPidm
        }
        if (!pidm) throw new RuntimeException( "No PIDM found for ${authentication.name}")
        log.trace "SelfServiceAuthenticationProvider.getPidm found PIDM $pidm for user ${authentication.name}"
        pidm
    }
    
    
    // if the SSB user has an Oracle username, we'll want to proxy the user's database connections
    def getOracleUsername( pidm, db ) {
        
        def oracleUserName         
        db.call( "{call gokeacc.p_getgobeaccinfo(?,?,?)}", 
            [ Sql.inout( Sql.VARCHAR( "" ) ),   // Oracle username (we want the OUT value)
              Sql.inout( Sql.INTEGER( pidm ) ), // pidm
              Sql.VARCHAR                       // spriden_id
            ] 
            ) { user_name, outPidm, spiden_id ->
            oracleUserName = user_name 
        }
        log.trace "SelfServiceAuthenticationProvider.getOracleUsername found oracle username $oracleUserName for user with PIDM $pidm"
        oracleUserName
    }
    
    
    def getGobtpac( pidm, db ) {
        
         def gobtpac // will hold a single Banner GOBTPAC record
         db.call( """declare result SYS_REFCURSOR;
                      begin
                          result := gb_third_party_access.f_query_one( $pidm );
                          ${Sql.out OracleTypes.CURSOR} := result;
                      end;
                   """
                ) { results ->
                    // Developer note:
                    // Per Rajesh, this proc may be returning more than one record and we'd have to loop
                    // through to get the latest. Since we don't support LDAP in this implementation, and 
                    // this will be replaced with a new PL/SQL API to do this, we won't bother changing this 
                    // implementation.  Again, this method is under test but not used, as it applies to LDAP only. 
             results?.each() { row ->
                 row.next()
                 gobtpac = [ pin:           row.GOBTPAC_PIN, 
                             ldap_user:     row.GOBTPAC_LDAP_USER, 
                             external_user: row.GOBTPAC_EXTERNAL_USER, 
                             disabled_ind:  row.GOBTPAC_PIN_DISABLED_IND, 
                             pin_exp_date:  row.GOBTPAC_PIN_EXP_DATE ]
             }
        }
        log.trace "SelfServiceAuthenticationProvider.getGobtpac found GOBTPAC record for PIDM $pidm:  $gobtpac"
        gobtpac
    }
    
    
    boolean shouldUseLDAP( db ) {
        def protocol = db.firstRow( "select twgbldap_protocol from twgbldap" )
        log.trace "SelfServiceAuthenticationProvider.shouldUseLDAP will return ${protocol ==~ /^LDAP/}"
        protocol ==~ /^LDAP/
    }
    
    
    def getLdapParm( db ) {
        
        def ldapParm
        db.call( "{? = call twbkwbis.f_fetchWTParam('LDAPMAPUSER')}", [ Sql.VARCHAR ] ) { ldap_parm  ->
           ldapParm = ldap_parm 
        }
        log.trace "SelfServiceAuthenticationProvider.getLdapParm found 'ldapparm' $ldapParm"
        ldapParm
    }
    
    
    def getLdapId( db, gobtpac ) {
        
        def ldapId
        def ldapParm = getLdapParm( db )
        
        switch (ldapParm) {
            case 'LDAPUSER'     : ldapId = gobtpac.ldap_user
                                  break
            case 'EXTERNALUSER' : ldapId = gobtpac.external_user
                                  break
            default             : ldapId = authentication.name
        }
        log.trace "SelfServiceAuthenticationProvider.getLdapId found LDAP ID $ldapId"
        ldapId
    }
    
        
    def isValidLdap( db, loginId, gobtpac ) {
        log.warn "****WARNING - isValidLdap currently hardcoded to return false ***** "
        false // TODO: Support LDAP - twbklogn.f_validate_ldap is not accessible         
    }
    
    
    /**
     * Returns either 'valid', 'expired', 'disabled', or 'invalid'.
     */
    def validatePin( pidm, pin, db ) {
        def pinValidation = [:]
        db.call( """declare 
                        lv_boolResult BOOLEAN;
                        lv_result NUMBER;
                        lv_pidm VARCHAR2(8) := $pidm;
                        lv_pin gobtpac.gobtpac_pin%TYPE := $pin;
                        lv_expire_ind VARCHAR2(1);
                        lv_disable_ind VARCHAR2(1);
                    begin
                        lv_boolResult := gb_third_party_access.f_validate_pin( lv_pidm, lv_pin, lv_expire_ind, lv_disable_ind );
                        IF (lv_boolResult) THEN
                          lv_result := 1;
                        ELSE
                          lv_result := 0;
                        END IF;                        
                        ${Sql.out OracleTypes.NUMBER} := lv_result;
                        ${Sql.VARCHAR} := lv_expire_ind;
                        ${Sql.VARCHAR} := lv_disable_ind;
                    end;
                 """
               ) { result, expiredInd, disabledInd ->
                     pinValidation << [ valid: (result == 1 ? true : false ), 
                                        expired: (expiredInd == 'Y' ? true : false), 
                                        disabled: (disabledInd == 'Y' ? true : false) ]
                 }
        log.trace "SelfServiceAuthenticationProvider.validatePin will return $pinValidation"
        pinValidation  
    }
    
    
    def determineAuthorities( authentication, authentictionResults, db ) {

        Collection<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>()
        
        def rows = db.rows( 
            """select twgrrole_pidm,twgrrole_role from twgrrole 
                       where twgrrole_pidm = :pidm
                union
                select govrole_pidm,twtvrole_desc from govrole,twtvrole,twgrrole 
                       where govrole_faculty_ind = 'Y' and govrole_pidm = :pidm and twtvrole_code = 'FACULTY'
                union
                select govrole_pidm,twtvrole_desc from govrole,twtvrole,twgrrole 
                       where govrole_student_ind = 'Y' and govrole_pidm = :pidm and twtvrole_code = 'STUDENT'
                union
                select govrole_pidm,twtvrole_desc from govrole,twtvrole,twgrrole 
                       where govrole_employee_ind = 'Y' and govrole_pidm = :pidm and twtvrole_code = 'EMPLOYEE'
                union
                select govrole_pidm,twtvrole_desc from govrole,twtvrole,twgrrole 
                       where govrole_alumni_ind = 'Y' and govrole_pidm = :pidm and twtvrole_code = 'ALUMNI'
                union
                select govrole_pidm,twtvrole_desc from govrole,twtvrole,twgrrole 
                       where govrole_friend_ind = 'Y' and govrole_pidm = :pidm and twtvrole_code = 'FRIEND'
                union
                select govrole_pidm,twtvrole_desc from govrole,twtvrole,twgrrole 
                       where govrole_finaid_ind = 'Y' and govrole_pidm = :pidm and twtvrole_code = 'FINAID'
                union
                select govrole_pidm,twtvrole_desc from govrole,twtvrole,twgrrole 
                       where govrole_finance_ind = 'Y' and govrole_pidm = :pidm and twtvrole_code = 'FINANCE'
            """, [ pidm: authentictionResults.pidm ] ) 
                    
        rows?.each { row ->    
            authorities << BannerGrantedAuthority.create( "SELFSERVICE", "$row.TWGRROLE_ROLE", null )
        }
        
        if (authentictionResults.oracleUserName) {
            Collection<GrantedAuthority> adminAuthorities = BannerAuthenticationProvider.determineAuthorities( authentictionResults.oracleUserName, db )
            authorities.addAll( adminAuthorities )
        }
        log.trace "SelfServiceAuthenticationProvider.determineAuthorities will return $authorities"
        authorities 
    }    

}


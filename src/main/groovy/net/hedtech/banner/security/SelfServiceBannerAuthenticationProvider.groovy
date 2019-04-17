/*******************************************************************************
 Copyright 2009-2018 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.security

import grails.util.Holders
import grails.util.Holders  as CH
import groovy.sql.Sql
import groovy.util.logging.Slf4j
import net.hedtech.banner.general.audit.LoginAuditService
import org.springframework.security.authentication.*
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.web.context.request.RequestContextHolder

import java.sql.SQLException

/**
 * An authentication provider which authenticates a self service user.  Self service users
 * need not have an oracle login..
 */
@Slf4j
public class SelfServiceBannerAuthenticationProvider implements AuthenticationProvider {


    def dataSource	// injected by Spring
    def loginAuditService = new LoginAuditService()
    String loginComment


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
            if ( !isGuestAuthenticationEnabled() ) {
                def provider = CH?.config.banner.sso.authenticationProvider
                if (provider && 'cas'.equalsIgnoreCase( provider )) {
                    log.trace "SelfServiceBannerAuthenticationProvider will not authenticate user since CAS is enabled"
                    return null
                }
            }
            conn = dataSource.getSsbConnection()
            Sql db = new Sql( conn )

            def authenticationResults = selfServiceAuthentication( authentication, db ) // may throw exceptions, like SQLException

            // Next, we'll verify the authenticationResults (and throw appropriate exceptions for expired pin, disabled account, etc.)
            AuthenticationProviderUtility.verifyAuthenticationResults this, authentication, authenticationResults

            authenticationResults['authorities']        = (Collection<GrantedAuthority>) determineAuthorities( authenticationResults, db )
            if(AuthenticationProviderUtility.isSsbEnabled()){
                authenticationResults['webTimeout']         = AuthenticationProviderUtility.getWebTimeOut(authenticationResults,dataSource)
            }
            else{
                authenticationResults['webTimeout'] = AuthenticationProviderUtility.findDefaultWebSessionTimeout()
            }

            AuthenticationProviderUtility.setWebSessionTimeout(  authenticationResults['webTimeout'] )
            authenticationResults['transactionTimeout'] = getTransactionTimeout()
            String preferredName = AuthenticationProviderUtility.getUserFullName(authenticationResults.pidm,authenticationResults.name,dataSource) as String
            if(authenticationResults.guest){
                authenticationResults['fullName'] = getFullName(authenticationResults, dataSource) as String
            } else {
                authenticationResults['fullName'] = preferredName
            }

            setTransactionTimeout( authenticationResults['transactionTimeout'] )

             if(authenticationResults!= null && Holders.config.EnableLoginAudit == "Y"){
                 loginComment= "LOGIN SUCCESSFUL"
                 loginAuditService.createLoginAudit(authenticationResults,loginComment)
            }

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
                AuthenticationProviderUtility.setUserDetails(authenticationResults.pidm,authenticationResults.name)
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

        def errorStatus
        def pass

        String GUEST_AUTHENTICATION = """
                         DECLARE
                            lv_proxyIDM      gpbprxy.gpbprxy_proxy_idm%TYPE;
                            lv_pinhash       gpbprxy.gpbprxy_pin%TYPE;
                            lv_context_hash  gpbprxy.gpbprxy_pin%TYPE;
                            lv_GPBPRXY_rec   gp_gpbprxy.gpbprxy_rec;
                            lv_GPBPRXY_ref   gp_gpbprxy.gpbprxy_ref;
                            state            NUMBER(1);
                            gidm             gpbprxy.gpbprxy_proxy_idm%TYPE;
                            firstName        gpbprxy.gpbprxy_first_name%TYPE;
                            lastName         gpbprxy.gpbprxy_last_name%TYPE;
                            
                            PROCEDURE P_Update_Invalid_Login (
                                     p_proxyIDM      gpbprxy.gpbprxy_proxy_idm%TYPE,
                            p_disabled_ind  gpbprxy.gpbprxy_pin_disabled_ind%TYPE,
                                     p_inv_login_cnt gpbprxy.gpbprxy_inv_login_cnt%TYPE)
                            IS
                            lv_cnt       gpbprxy.gpbprxy_inv_login_cnt%TYPE;
                            lv_disabled  gpbprxy.gpbprxy_pin_disabled_ind%TYPE;
                            BEGIN
                            lv_cnt      := nvl(p_inv_login_cnt,0) + 1;
                            lv_disabled := p_disabled_ind;
                            
                            IF lv_cnt >= NVL (bwgkprxy.F_GetOption ('MAX_INVALID_LOGINS'), 3) THEN
                            lv_disabled := 'Y';
                            lv_cnt      := 0;
                            END IF;
                            gp_gpbprxy.P_Update (
                                     p_proxy_idm        => p_proxyIDM,
                                     p_pin_disabled_ind => lv_disabled,
                                     p_inv_login_cnt    => lv_cnt,
                                     p_user_id          => goksels.f_get_ssb_id_context
                            );
                            gb_common.p_commit;
                            
                            END P_Update_Invalid_Login;
                                                  
                         BEGIN
                            -- Get proxy by e-mail address
                            lv_proxyIDM := bwgkpxya.F_GetProxyIDM (?);
                            
                         IF lv_proxyIDM = 0 THEN
                               state := -1;
                         ELSE
                              lv_GPBPRXY_ref := gp_gpbprxy.F_Query_One (lv_proxyIDM);
                            
                            FETCH lv_GPBPRXY_ref INTO lv_GPBPRXY_rec;
                            CLOSE lv_GPBPRXY_ref;
                            
                            gspcrpt.P_SaltedHash (?, lv_GPBPRXY_rec.R_SALT, lv_pinhash);
                            
                            -- Check for disabled PIN
                            IF lv_GPBPRXY_rec.R_PIN_DISABLED_IND <> 'N' THEN
                              state := -2;
                            -- Check for expired PIN
                            ELSIF NVL (TRUNC(lv_GPBPRXY_rec.R_PIN_EXP_DATE), TRUNC(SYSDATE)) < TRUNC(SYSDATE) THEN
                              state := -3;
                            ELSIF lv_pinhash <> lv_GPBPRXY_rec.R_PIN THEN
                              state := -4;
                              P_Update_Invalid_Login (lv_proxyIDM, lv_GPBPRXY_rec.R_PIN_DISABLED_IND, lv_GPBPRXY_rec.R_INV_LOGIN_CNT);
                            
                            ELSE
                             gidm := lv_GPBPRXY_rec.r_proxy_idm;
                             firstName := lv_GPBPRXY_rec.r_first_name;
                             lastName := lv_GPBPRXY_rec.r_last_name;
                             state := 0;
                            END IF;
                        END IF;
                            
                            ? := state;
                            ? := gidm;
                            ? := firstName;
                            ? := lastName;
                            
                        END;
      """
        try {

            db.call(GUEST_AUTHENTICATION,
                    [authentication.name, authentication.credentials, db.NUMERIC, db.NUMERIC, db.VARCHAR,  db.VARCHAR])
                    { errorOut, gidmOut, firstNameOut, lastNameOut ->
                        errorStatus = errorOut
                        gidm = gidmOut
                        firstName = firstNameOut
                        lastName = lastNameOut
                    }

            switch (errorStatus) {
                case -1:
                    log.debug "SelfServiceAuthenticationProvider guestAuthenticationS failed on invalid login id/pin"
                    authenticationResults.valid = false
                    pass = false
                    break
                case -2:
                    log.debug "SelfServiceAuthenticationProvider guestAuthenticationS failed on disabled pin"
                    authenticationResults.disabled = true
                    pass = false
                    RequestContextHolder.currentRequestAttributes()?.request?.session.setAttribute("guestUser", true)
                    break
                case -3:
                    log.debug "SelfServiceAuthenticationProvider guestAuthentication failed on expired pin"
                    authenticationResults.expired = true
                    AuthenticationProviderUtility.setUserDetails(authenticationResults.pidm, authenticationResults.name)
                    pass = false
                    RequestContextHolder.currentRequestAttributes()?.request?.session.setAttribute("guestUser", true)
                    break
                case 0:
                    authenticationResults.valid = true
                    authenticationResults.gidm = gidm
                    authenticationResults.fullName = firstName + " " + lastName
                    pass = true
                    RequestContextHolder.currentRequestAttributes()?.request?.session.setAttribute("guestUser", true)
                    break
            }

        }catch(Exception e){
            log.error('Problem with SelfServiceAuthenticationProvider guestAuthentication')
            log.error(e)
            return false
        }

        return pass
    }


    private boolean isGuestAuthenticationEnabled() {
        CH.config.guestAuthenticationEnabled instanceof Boolean ? CH.config.guestAuthenticationEnabled : false
    }


    private BannerAuthenticationToken newAuthenticationToken( authenticationResults ) {
        AuthenticationProviderUtility.newAuthenticationToken( this, authenticationResults )
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
            if(rows.size() > 0){
                authorities << BannerGrantedAuthority.create( "SELFSERVICE-SSROLE", "BAN_DEFAULT_M", null )
            }
        }
        if (authentictionResults.pidm) {
            authorities << BannerGrantedAuthority.create( "SELFSERVICE-ALLROLES", "BAN_DEFAULT_M", null )
        }
        // def selfServiceRolePassword
        if (authentictionResults.oracleUserName) {
            authorities << BannerGrantedAuthority.create( "SELFSERVICE", "BAN_DEFAULT_M", null )
            Collection<GrantedAuthority> adminAuthorities = AuthenticationProviderUtility.determineAuthorities( authentictionResults, db )
            authorities.addAll( adminAuthorities )
        }
        log.trace "SelfServiceAuthenticationProvider.determineAuthorities will return $authorities"
        authorities
    }

    public setTransactionTimeout( timeoutSeconds ) {
        RequestContextHolder.currentRequestAttributes().session.transactionTimeout = timeoutSeconds
    }

    def getTransactionTimeout() {
        def timeoutSeconds = CH.config.banner?.transactionTimeout instanceof Integer ? CH.config.banner?.transactionTimeout : 30
        timeoutSeconds
    }

}


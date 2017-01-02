/*******************************************************************************
 Copyright 2009-2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.security

import grails.util.GrailsNameUtils
import groovy.sql.Sql
import net.hedtech.banner.exceptions.AuthorizationException
import org.apache.log4j.Logger
import grails.util.Holders
import org.springframework.context.ApplicationContext
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.CredentialsExpiredException
import org.springframework.security.authentication.DisabledException
import org.springframework.security.authentication.LockedException
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes as GA
import org.springframework.web.context.request.RequestContextHolder

import java.sql.SQLException


/**
 * An Utility Class to handle the common logic of mapping user to Banner user.
 */
class AuthenticationProviderUtility {

    // note: using 'getClass()' here doesn't work -- we'll just use a String
    private static final Logger log = Logger.getLogger( "net.hedtech.banner.security.AuthenticationProviderUtility" )
    private static def applicationContext // set lazily via 'getApplicationContext()'
    private static roleBasedTimeOutsCache = [:]

    // a cached map of web roles to their configured timeout values, that is populated on first need

    private static Integer defaultWebSessionTimeout // will be read from configuration

    public static def getMappedUserForUdcId(assertAttributeValue, dataSource ) {
        log.trace "AuthenticationProviderUtility.getMappedUserForUdcId doing external authentication"

        if(assertAttributeValue == null) {
            log.fatal("System is configured for non default authentication and identity assertion is $assertAttributeValue")  // NULL
            throw new UsernameNotFoundException("System is configured for non default authentication and identity assertion is $assertAttributeValue")
        }

        def oracleUserName
        def pidm
        def spridenId
        def authenticationResults
        String accountStatus
        def conn

        Boolean proxySsb = Holders.config.ssbOracleUsersProxied instanceof Boolean ? Holders.config.ssbOracleUsersProxied : false
        Boolean processSsbOracleAccount = true
        try {

            // Determine if ssb users need oracle authentication
            if (isSsbEnabled() && !proxySsb  ) {
                log.trace "AuthenticationProviderUtility.getMappedUserForUdcId configuration values ssbOracleUsersProxied = $proxySsb"
                processSsbOracleAccount = false
            }

            conn = dataSource.unproxiedConnection
            Sql db = new Sql( conn )

            log.trace "AuthenticationProviderUtility.getMappedUserForUdcId mapping for $Holders?.config?.banner.sso.authenticationAssertionAttribute = $assertAttributeValue"
            // Determine if they map to a Banner Admin user
            def sqlStatement = '''SELECT gobeacc_username, gobeacc_pidm FROM gobumap, gobeacc
                                  WHERE gobumap_pidm = gobeacc_pidm AND gobumap_udc_id = ?'''
            db.eachRow( sqlStatement, [assertAttributeValue] ) { row ->
                oracleUserName = row.gobeacc_username
                pidm = row.gobeacc_pidm
            }
            if ( oracleUserName  && processSsbOracleAccount ) {
                log.trace "AuthenticationProviderUtility.getMappedUserForUdcId oracleUsername $oracleUserName found"
                // check if the oracle user account is locked

                def sqlStatement1 = '''select account_status,lock_date from dba_users where username=?'''
                db.eachRow( sqlStatement1, [oracleUserName.toUpperCase()] ) { row ->
                    accountStatus = row.account_status
                }
                if ( accountStatus.contains("LOCKED")) {
                    log.trace "AuthenticationProviderUtility.getMappedUserForUdcId account status of user $oracleUserName is Locked"
                    authenticationResults = [locked : true]
                } else {
                    log.trace "AuthenticationProviderUtility.getMappedUserForUdcId account status of user $oracleUserName is valid"
                    authenticationResults = [ name: oracleUserName, pidm: pidm, oracleUserName: oracleUserName, valid: true ].withDefault { k -> false }
                }
            } else {
                log.trace "AuthenticationProviderUtility.getMappedUserForUdcId oracleUsername $oracleUserName not found"
                // Not an Admin user, must map to a self service user
                def sqlStatement2 = '''SELECT spriden_id, gobumap_pidm FROM gobumap,spriden WHERE spriden_pidm = gobumap_pidm AND spriden_change_ind is null AND gobumap_udc_id = ?'''
                db.eachRow( sqlStatement2, [assertAttributeValue] ) { row ->
                    spridenId = row.spriden_id
                    pidm = row.gobumap_pidm
                }
                log.trace "AuthenticationProviderUtility.getMappedUserForUdcId query spriden_id for UDC IDENTIFIER $assertAttributeValue"
                if(spridenId && pidm) {
                    log.trace "AuthenticationProviderUtility.getMappedUserForUdcId spridenID $spridenId and gobumap pidm $pidm found"
                    authenticationResults = [ name: spridenId, pidm: pidm, valid: (spridenId && pidm), oracleUserName: oracleUserName ].withDefault { k -> false }
                } else {
                    log.fatal "System is configured for external authentication, identity assertion $assertAttributeValue does not map to a Banner user"
                    //throw new BadCredentialsException("System is configured for external authentication, identity assertion $assertAttributeValue does not map to a Banner user")

                }
            }

        } catch (SQLException e) {
            log.error "AuthenticationProviderUtility.getMappedDatabaseUserForUdcId not able to map $Holders?.config?.banner.sso.authenticationAssertionAttribute = $assertAttributeValue to db user"
            throw e
        } finally {
            conn?.close()
        }
        log.trace "AuthenticationProviderUtility.getMappedDatabaseUserForUdcId results are $authenticationResults"
        authenticationResults
    }

    private static Boolean isSsbEnabled() {
        Holders.config.ssbEnabled instanceof Boolean ? Holders.config.ssbEnabled : false
    }


    public static getUserFullName(pidm,name,dataSource){
        def ctx = Holders.servletContext.getAttribute(GA.APPLICATION_CONTEXT)
        def preferredNameService = ctx.preferredNameService
        def fullName
        def conn
        def preferredName
        if (isSsbEnabled()) {
            conn = dataSource.getSsbConnection();
            log.debug "AuthenticationProviderUtility.getUserFullName using banssuser ssb connection"
        }else{
            conn = dataSource.getConnection();
            log.debug "AuthenticationProviderUtility.getUserFullName using banproxy connection"
        }

        try {
            preferredName = preferredNameService.getPreferredName(pidm,conn) as String
            if(preferredName!=null && !preferredName.isEmpty() ) {
                fullName = preferredName
                log.debug "AuthenticationProviderUtility.getUserFullName found full name $preferredName"
            }
            else {
                fullName = getFullName(name.toUpperCase(), dataSource) as String
                log.debug "AuthenticationProviderUtility.getUserFullName found full name $fullName"
            }
        }
        finally{
            conn?.close()
        }
        return fullName;
    }

    public static BannerAuthenticationToken createAuthenticationToken(dbUser, dataSource, provider ) {

        def fullName=getUserFullName(dbUser.pidm,dbUser.name,dataSource);
        log.debug "AuthenticationProviderUtility.createAuthenticationToken found full name $fullName"

        Collection<GrantedAuthority> authorities
        def conn
        if (isSsbEnabled()) {
            try {
                conn = dataSource.getSsbConnection()
                Sql db = new Sql( conn )
                authorities = SelfServiceBannerAuthenticationProvider.determineAuthorities( dbUser, db )
                log.debug "AuthenticationProviderUtility.createAuthenticationToken found Self Service authorities $authorities"
            } catch(Exception e) {
                log.fatal("Error occurred in loading authorities : " + e.localizedMessage())
                throw new BadCredentialsException(e.localizedMessage());
            } finally {
                conn?.close()
            }

        }
        else {
            authorities = BannerAuthenticationProvider.determineAuthorities( dbUser, dataSource )
            log.debug "AuthenticationProviderUtility.createAuthenticationToken found Banner Admin authorities $authorities"
        }
        if(authorities == null || authorities.size() == 0) {
            log.fatal("No authorities found")
            throw new AuthorizationException("No authorities found")
        }
        dbUser['authorities'] = authorities
        dbUser['fullName'] = fullName
        if(isSsbEnabled()){
            dbUser['webTimeout'] = getWebTimeOut( dbUser,dataSource)
        }
        else{
            dbUser['webTimeout'] = getDefaultWebSessionTimeout()
        }

        setWebSessionTimeout( dbUser['webTimeout'] )
        BannerAuthenticationToken bannerAuthenticationToken = newAuthenticationToken( provider, dbUser )

        log.debug "AuthenticationProviderUtility.createAuthenticationToken BannerAuthenticationToken created $bannerAuthenticationToken"
        bannerAuthenticationToken
    }

    /**
     * Returns a new authentication object based upon the supplied arguments.
     * This method, when used within other providers, should NOT catch the exceptions but should let them be caught by the filter.
     * @param provider the provider who needs to create a token (used for logging purposes)
     * @param authentication the initial authentication object containing credentials
     * @param authenticationResults the authentication results, including the user's Oracle database username
     * @param authorities the user's authorities that must be included in the new authentication object
     * @throws AuthenticationException various AuthenticationException types may be thrown, and should NOT be caught by providers using this method
     **/
    public static BannerAuthenticationToken newAuthenticationToken( provider, authenticationResults ) {

        try {
            def user = new BannerUser( authenticationResults.name,                       // username
                    authenticationResults.credentials as String,      // password
                    authenticationResults.oracleUserName,             // oracle username (note this may be null)
                    !authenticationResults.disabled,                  // enabled (account)
                    true,                                             // accountNonExpired - NOT USED
                    !authenticationResults.expired,                   // credentialsNonExpired
                    true,                                             // accountNonLocked - NOT USED (YET)
                    authenticationResults.authorities as Collection,
                    authenticationResults.fullName
            )
            if (authenticationResults?.webTimeout) user.webTimeout = authenticationResults.webTimeout
            if (authenticationResults?.pidm) user.pidm = authenticationResults.pidm
            if (authenticationResults?.gidm) user.gidm = authenticationResults.gidm
            def token = new BannerAuthenticationToken( user, null )
            log.trace "${provider?.class?.simpleName}.newAuthenticationToken is returning token $token"
            token
        } catch (e) {
            // We don't expect an exception when simply constructing the user and token, so we'll report this as an error
            log.error "AuthenticationProviderUtility.newAuthenticationToken was not able to construct a token for user $authenticationResults.name, due to exception: ${e.message}"
            return null // this is a rare situation where we want to bury the exception - we *need* to return null to allow other providers a chance...
        }
    }

    /**
     * Throws appropriate Spring Security exceptions for disabled accounts, locked accounts, expired pin,
     * @throws org.springframework.security.authentication.DisabledException if account is disabled
     * @throws org.springframework.security.authentication.CredentialsExpiredException if credential is expired
     * @throws org.springframework.security.authentication.LockedException if the user account has been locked
     * @throws RuntimeException if the pin was invalid or the id was incorrect (i.e., the default error)
     **/
    public static verifyAuthenticationResults( Map authenticationResults ) {

        if (authenticationResults.disabled){
            log.warn "Provider was not able to authenticate user - Account Disabled"
            throw new DisabledException('')
        }
        if (authenticationResults.expired) {
            log.warn "Provider was not able to authenticate user - Account Expired"
            throw new CredentialsExpiredException('')
        }
        if (authenticationResults.locked) {
            log.warn "Provider was not able to authenticate user - Account Locked"
            throw new LockedException('')
        }
        if (!authenticationResults.valid) {
            log.warn "Provider was not able to authenticate user - Account Invalid"
            throw new BadCredentialsException('')
        }
    }

    /**
     * Throws appropriate Spring Security exceptions for disabled accounts, locked accounts, expired pin,
     * @throws org.springframework.security.authentication.DisabledException if account is disabled
     * @throws org.springframework.security.authentication.CredentialsExpiredException if credential is expired
     * @throws org.springframework.security.authentication.LockedException if the user account has been locked
     * @throws RuntimeException if the pin was invalid or the id was incorrect (i.e., the default error)
     **/
    public static verifyAuthenticationResults( AuthenticationProvider provider, Authentication authentication, Map authenticationResults ) {

        def report = AuthenticationProviderUtility.&handleFailure.curry( provider, authentication, authenticationResults )

        if (authenticationResults?.disabled) report( new DisabledException('') )
        if (authenticationResults?.expired)  report( new CredentialsExpiredException('') )
        if (authenticationResults?.locked)   report( new LockedException('') )
        if (!authenticationResults?.valid)   report( new BadCredentialsException('') )
    }

    private static handleFailure( provider, authentication, authenticationResults, exception ) {

        log.warn "${provider.class.simpleName} was not able to authenticate user $authentication.name due to exception ${exception.class.simpleName}: ${exception.message} "

        def tmp
        def sessionObj = RequestContextHolder.currentRequestAttributes().request.session
        def msg = GrailsNameUtils.getNaturalName(GrailsNameUtils.getLogicalName(exception.class.simpleName, "Exception"))
        def module = GrailsNameUtils.getNaturalName(GrailsNameUtils.getLogicalName(provider.class.simpleName, "AuthenticationProvider"))
        if(module=="Banner"||module=="Self Service Banner")
            sessionObj.setAttribute("auth_name", authentication.name)
        if(sessionObj.getAttribute("msg")) {
            tmp = sessionObj.getAttribute("msg") + ", " + msg
            sessionObj.setAttribute("msg", tmp)
        }
        else
            sessionObj.setAttribute("msg", msg)

        if(sessionObj.getAttribute("module")) {
            tmp = sessionObj.getAttribute("module") + ", " + module
            sessionObj.setAttribute("module", tmp)
        }
        else
            sessionObj.setAttribute("module", module)

        throw exception
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
    public static Collection<GrantedAuthority> determineAuthorities( Map authenticationResults, Sql db ) {
        return BannerGrantedAuthorityService.determineAuthorities (authenticationResults, db)
    }

    /*
   * Return's the user pidm
   * */
    public static getUserPidm( String name, def dataSource ) {
        def conn = null
        def pidm
        name = name.toUpperCase()
        try {
            conn = dataSource.unproxiedConnection
            Sql db = new Sql(conn)
            db.eachRow( "select  spriden_pidm  from spriden where spriden_id = ? AND  spriden_change_ind is null", [name] ) {
                row -> pidm = row.spriden_pidm
            }
        } catch (SQLException e) {
            log.error "AuthenticationProviderUtility not able to getUserPidm of user $name due to exception $e.message"
            return null
        } finally {
            conn?.close()
        }
        pidm
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
            log.trace "AuthenticationProviderUtility.getFullName after checking f_formatname $fullName"
            if (null == fullName) {
                db.eachRow( "select  f_format_name(spriden_pidm,'FL') fullname  from gurlogn,spriden where gurlogn_user = ? and gurlogn_pidm = spriden_pidm", [name] ) { row ->
                    fullName = row.fullname
                }
            }
            log.trace "AuthenticationProviderUtility.getFullName after checking gurlogn_pidm $fullName"
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
            log.error "AuthenticationProviderUtility not able to getFullName $name due to exception $e.message"
            return null
        } finally {
            conn?.close()
        }
        log.trace "AuthenticationProviderUtility.getFullName is returning $fullName"
        fullName
    }
    public static setWebSessionTimeout( Integer timeoutSeconds ) {
        RequestContextHolder.currentRequestAttributes().session.setAttribute("maxInactiveInterval",timeoutSeconds);
    }


    public static int getDefaultWebSessionTimeout() {

        if (!defaultWebSessionTimeout) {
            def configuredTimeout = Holders.config.defaultWebSessionTimeout
            defaultWebSessionTimeout = configuredTimeout instanceof Map ? RequestContextHolder.currentRequestAttributes().session.getMaxInactiveInterval() : configuredTimeout
        }
        defaultWebSessionTimeout
    }


    public static getWebTimeOut( authenticationResults,dataSource) {
        if (roleBasedTimeOutsCache.size() == 0) {retrieveRoleBasedTimeOuts(dataSource)}
        def timeoutsForUser = [ getDefaultWebSessionTimeout() ]
        authenticationResults['authorities']?.each { authority ->
            def objectNameSplit
            def timeout
            if (authority.objectName.contains('-')) {
                objectNameSplit = authority.objectName.split('-')
                timeout = roleBasedTimeOutsCache.get("${objectNameSplit[1]}")
                if (timeout) timeoutsForUser << timeout.intValue()* 60 // Convert minutes to seconds....
            }
        }
        timeoutsForUser.max()

    }


    public static retrieveRoleBasedTimeOuts(dataSource) {
        def conn
        try {
            conn = dataSource.getSsbConnection()
            Sql db = new Sql(conn)
            def rows = db.rows("select twtvrole_code, twtvrole_time_out from twtvrole")
            rows?.each { row ->
                roleBasedTimeOutsCache.put("${row.TWTVROLE_CODE}", row.TWTVROLE_TIME_OUT);
            }
            log.debug "retrieveRoleBasedTimeOuts() has cached web role timeouts: ${roleBasedTimeOutsCache}"
            roleBasedTimeOutsCache
        }finally {
            conn?.close()
        }
    }
    public static setUserDetails(pidm,name){
        RequestContextHolder.currentRequestAttributes().session.setAttribute("usersName",name)
        RequestContextHolder.currentRequestAttributes().session.setAttribute("usersPidm",pidm)
    }

}

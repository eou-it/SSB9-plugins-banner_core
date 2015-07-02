/*******************************************************************************
Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
*******************************************************************************/ 
package net.hedtech.banner.testing

import java.sql.Connection
import groovy.sql.Sql
import org.codehaus.groovy.grails.commons.ConfigurationHolder as CH

/**
 * Functional tests of banner admin authentication.
 */
class AdminAuthenticationFunctionalTests extends BaseFunctionalTestCase {

    protected void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
    }


    void testAdminUserAccessFailed() {
        def currentAuthProvider = grailsApplication.config.banner.sso.authenticationProvider
        grailsApplication.config.banner.sso.authenticationProvider = "default"

        login "grails_user", "u_pick_i"

        def stringContent = page?.webResponse?.contentAsString

        grailsApplication.config.banner.sso.authenticationProvider = currentAuthProvider
        if (stringContent =~ "invalid username/password") assert true
        else assert false

    }


    void testLockAccount() {
        Connection sessionConnection
        Sql sql
        String lockStmt = "ALTER USER grails_user ACCOUNT LOCK"
        String unlockStmt = "ALTER USER grails_user ACCOUNT UNLOCK"

        def url = CH.config.bannerDataSource.url
        try {
            sql = Sql.newInstance(url,
                    "bansecr",
                    "u_pick_it",
                    'oracle.jdbc.driver.OracleDriver')


            sql.execute(lockStmt)

            login "grails_user", "u_pick_it"

            def stringContent = page?.webResponse?.contentAsString

            if (stringContent =~ "account is locked") assert true
            else assert false

        } finally {
            sql.execute(unlockStmt)
            sql?.close()
        }
    }
}

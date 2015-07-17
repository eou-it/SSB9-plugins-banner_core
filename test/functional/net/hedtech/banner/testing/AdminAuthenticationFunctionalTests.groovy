/*******************************************************************************
Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
*******************************************************************************/ 
package net.hedtech.banner.testing

import grails.util.Holders
import grails.util.Holders  as CH
import groovy.sql.Sql
import net.hedtech.banner.security.FormContext
import org.junit.After
import org.junit.Before
import org.junit.Test

import java.sql.Connection

/**
 * Functional tests of banner admin authentication.
 */

class AdminAuthenticationFunctionalTests extends BaseFunctionalTestCase {

    @Before
    public void setUp(){
        Holders.config.banner.sso.authenticationProvider = "default"
        formContext = ['GUAGMNU']
        super.setUp()
    }

    @After
    public void tearDown() {
        FormContext.clear()
    }

    @Test
    void testAdminUserAccessFailed() {
        login "grails_user", "u_pick_i"

        def stringContent = page?.webResponse?.contentAsString

        if (stringContent =~ "invalid username/password") assert true
        else assert false

    }

    @Test
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

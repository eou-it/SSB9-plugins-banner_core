/*******************************************************************************
Copyright 2009-2015 Ellucian Company L.P. and its affiliates.
*******************************************************************************/
package net.hedtech.banner.testing

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
    boolean envSsbEnabledValue



    @Before
    public void setUp(){
        formContext = ['GUAGMNU']
        super.setUp()
        grailsApplication.config.banner.sso.authenticationProvider = "default"
        envSsbEnabledValue = grailsApplication.config.ssbEnabled
        grailsApplication.config.ssbEnabled = false
    }

    @After
    public void tearDown() {
        super.tearDown()
        grailsApplication.config.ssbEnabled = envSsbEnabledValue
    }

    @Test
    void testAdminUserAccessSuccess() {
        login "grails_user", "u_pick_it"

        assertStatus 200
        assertContentContains "Welcome Back grails_user"
    }

    @Test
    void testAdminUserAccessFailed() {
        login "grails_user", "u_pick_i"
        assertStatus 200
        assertContentContains "invalid username/password; logon denied"
    }

    @Test
    void testLockAccount() {
        Connection sessionConnection
        Sql sql
        String lockStmt = "ALTER USER grails_user ACCOUNT LOCK"
        String unlockStmt = "ALTER USER grails_user ACCOUNT UNLOCK"

        def url = grailsApplication.config.bannerDataSource.url
        try {
            sql = Sql.newInstance(url,
                    "bansecr",
                    "u_pick_it",
                    'oracle.jdbc.driver.OracleDriver')


            sql.execute(lockStmt)

            login "grails_user", "u_pick_it"

            assertStatus 200
            assertContentContains "account is locked; logon denied"

        } finally {
            sql.execute(unlockStmt)
            sql?.close()
        }
    }
}

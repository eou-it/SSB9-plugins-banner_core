/** *****************************************************************************
 © 2011 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
package com.sungardhe.banner.testing

import java.sql.Connection
import groovy.sql.Sql
import org.codehaus.groovy.grails.commons.ConfigurationHolder as CH

/**
 * Functional tests of banner admin authentication.
 */
class AdminAuthenticationFunctionalTests extends BaseFunctionalTestCase {

    protected void setUp() {
        formContext = ['STVCOLL']
        super.setUp()
    }


    void testAdminUserAccessFailed() {

        login "grails_user", "u_pick_i"

        def stringContent = page?.webResponse?.contentAsString

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
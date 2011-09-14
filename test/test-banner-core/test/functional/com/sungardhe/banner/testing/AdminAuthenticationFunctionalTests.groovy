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
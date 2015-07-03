package net.hedtech.banner.db.dbutility

import net.hedtech.banner.SpringContextUtils
import net.hedtech.banner.db.BannerConnection
import net.hedtech.banner.db.BannerDS
import net.hedtech.banner.testing.BaseIntegrationTestCase
import oracle.jdbc.OracleConnection
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.security.core.context.SecurityContextHolder
import net.hedtech.banner.security.FormContext
import net.hedtech.banner.testing.BaseIntegrationTestCase
import spock.lang.*
import org.codehaus.groovy.grails.commons.ConfigurationHolder as CH

import java.sql.Connection

class DBUtilityIntegrationTestsSpec extends BaseIntegrationTestCase {

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
    }

    @After
    public void tearDown() {
        super.tearDown()
    }

    @Test
    public void testOracleUser(){
        def user = SecurityContextHolder?.context?.authentication?.principal
        println user
        assertTrue(DBUtility.isOracleUser(user))
    }


}

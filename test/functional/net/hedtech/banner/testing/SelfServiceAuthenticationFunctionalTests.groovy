/*******************************************************************************
Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
*******************************************************************************/ 
package net.hedtech.banner.testing

import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

/**
 * Functional tests of self service authentication.
 * @TODO Grails Functional Tests require the support of grails-remote-control plugin
 * to gain runtime config property modification abilities. This is essential for
 * Authentication test cases since it has toggles the ssbEnabled and authenticationProvider
 * flags to test various cases in one test suite itself.
 *
 * The default grails Holders class support is not available to manipulate config properties,
 * because functional test framework run in a different JVM than the one used by the Application
 * Under Test.
 *
 */
class SelfServiceAuthenticationFunctionalTests extends BaseFunctionalTestCase {
    boolean envSsbEnabledValue

    def dataSource  // injected by Spring

    @Before
    protected void setUp() {
        formContext = [ 'SELFSERVICE' ]
        super.setUp()
        grailsApplication.config.banner.sso.authenticationProvider = "default"
        envSsbEnabledValue = grailsApplication.config.ssbEnabled
        grailsApplication.config.ssbEnabled = true
    }

    @After
    public void tearDown() {
        super.tearDown()
        grailsApplication.config.ssbEnabled = envSsbEnabledValue
    }

    @Ignore
    void testSuccessfulLogin() {
        login "HOSWEB002", "111111"

        assertStatus 200
        assertContentContains "Welcome Back HOSWEB002"
    }

    /**
     * @TODO Warning: Failed test will increment the unsuccessful attempts in Database; won't get reset by
     * subsequent successful logins. Because of this, this test case should have reset that to nullify
     * the side-effect of the test case to the applicaiton.
     */
    @Test
    void testFailedLogin() {
        login "HOSWEB002", "111115"

        assertStatus 200
        assertContentContains "invalid username/password; logon denied"
    }

}

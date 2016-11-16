/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.controllers

import grails.util.Holders
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.web.context.request.RequestContextHolder

/**
 * Test cases for ControllerUtils
 */
class ControllerUtilsIntegrationTests extends BaseIntegrationTestCase {
    def outcome

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
    void isGuestAuthenticationEnabledTest() {
        outcome = ControllerUtils.isGuestAuthenticationEnabled()
        assertNotNull(outcome)
    }

    @Test
    void isLocalLogoutDisabledTest() {
        def oldlocalLogoutValue = Holders?.config.banner?.sso?.authentication.saml.localLogout
        Holders?.config.banner?.sso?.authentication.saml.localLogout = 'true'
        outcome = ControllerUtils.isLocalLogoutEnabled()
        assertNotNull(outcome)
        assertTrue(outcome)
        Holders?.config.banner?.sso?.authentication.saml.localLogout = oldlocalLogoutValue
    }

    @Test
    void isLocalLogoutEnabledTest() {
        outcome = ControllerUtils.isLocalLogoutEnabled()
        assertNotNull(outcome)
        assertFalse(outcome)
    }

    @Test
    void isCasEnabledTest() {
        Holders?.config.banner.sso.authenticationProvider = 'external'
        outcome = ControllerUtils.isCasEnabled();
        assertFalse(outcome);
    }

    @Test
    void isCasDisabledTest() {
        Holders?.config.banner.sso.authenticationProvider = null
        outcome = ControllerUtils.isCasEnabled();
        assertFalse(outcome);
    }

    @Test
    void isSamlEnabledTest() {
        Holders?.config.banner.sso.authenticationProvider = 'saml'
        outcome = ControllerUtils.isSamlEnabled();
        assertNotNull(outcome);
    }

    @Test
    void isSamlDisabledTest() {
        Holders?.config.banner.sso.authenticationProvider = null
        outcome = ControllerUtils.isSamlEnabled();
        assertFalse(outcome)
    }


    @Test
    void getAfterLogoutRedirectURITest() {
        outcome = ControllerUtils.getAfterLogoutRedirectURI();
        assertNotNull(outcome)
    }

    @Test
    void getAfterLogoutRedirectURIFailedTest() {
        outcome = ControllerUtils.getAfterLogoutRedirectURI();
        assertNotNull(outcome);
    }

    @Test
    void getHomePageURLTest() {
        outcome = ControllerUtils.getHomePageURL();
        assertNotNull(outcome);
    }

    @Test
    void buildLogoutRedirectURIMepNullTest() {
        def oldLocalvalue = RequestContextHolder?.currentRequestAttributes()?.request?.session?.getAttribute("mep")
        RequestContextHolder?.currentRequestAttributes()?.request?.session?.putAt("mep", 'test');
        outcome = ControllerUtils.buildLogoutRedirectURI();
        RequestContextHolder?.currentRequestAttributes()?.request?.session?.putAt("mep", null);
        assertNotNull(outcome);
    }

    @Test
    void buildLogoutRedirectURINoMepTest() {
        outcome = ControllerUtils.buildLogoutRedirectURI();
        assertNotNull(outcome);
    }

    @Test
    void buildLogoutRedirectURIWithSamlEnabledTest() {
        Holders?.config.banner.sso.authenticationProvider = 'saml'
        outcome = ControllerUtils.buildLogoutRedirectURI();
        assertNotNull(outcome);
    }

    @Test
    void buildLogoutRedirectURIDefaultTest() {
        Holders?.config.banner.sso.authenticationProvider = null
        outcome = ControllerUtils.buildLogoutRedirectURI();
        assertNotNull(outcome);
    }

}

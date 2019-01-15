/*******************************************************************************
 Copyright 2016-2018 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.controllers

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import grails.util.Holders
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.web.context.request.RequestContextHolder

/**
 * Test cases for ControllerUtils
 */
@Integration
@Rollback
class ControllerUtilsIntegrationTests extends BaseIntegrationTestCase {
    def outcome
    private static final def SAML = 'saml'
    private static final def EXTERNAL = 'external'
    private static final def MEP='mep'

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
        def oldlocalLogoutValue = Holders.config.banner.sso.authentication.saml.localLogout
        Holders.config.banner.sso.authentication.saml.localLogout = 'true'
        outcome = ControllerUtils.isLocalLogoutEnabled()
        assertTrue(outcome)
        Holders.config.banner.sso.authentication.saml.localLogout = oldlocalLogoutValue
    }

    @Test
    void isLocalLogoutEnabledTest() {
        outcome = ControllerUtils.isLocalLogoutEnabled()
        assertFalse(outcome)
    }

    @Test
    void isCasEnabledTest() {
        Holders.config.banner.sso.authenticationProvider = EXTERNAL
        outcome = ControllerUtils.isCasEnabled();
        assertFalse(outcome);
    }

    @Test
    void isCasDisabledTest() {
        Holders.config.banner.sso.authenticationProvider = null
        outcome = ControllerUtils.isCasEnabled();
        assertFalse(outcome);
    }

    @Test
    void isSamlEnabledTest() {
        Holders.config.banner.sso.authenticationProvider = SAML
        outcome = ControllerUtils.isSamlEnabled();
        assertNotNull(outcome);
    }

    @Test
    void isSamlDisabledTest() {
        Holders.config.banner.sso.authenticationProvider = null
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
        def oldLocalvalue = RequestContextHolder?.currentRequestAttributes()?.request?.session?.getAttribute(MEP)
        RequestContextHolder?.currentRequestAttributes()?.request?.session?.putAt(MEP, 'test');
        outcome = ControllerUtils.buildLogoutRedirectURI();
        RequestContextHolder?.currentRequestAttributes()?.request?.session?.putAt(MEP, oldLocalvalue);
        assertNotNull(outcome);
    }

    @Test
    void buildLogoutRedirectURINoMepTest() {
        outcome = ControllerUtils.buildLogoutRedirectURI();
        assertNotNull(outcome);
    }

    @Test
    void buildLogoutRedirectURIWithSamlEnabledTest() {
        Holders.config.banner.sso.authenticationProvider = SAML
        outcome = ControllerUtils.buildLogoutRedirectURI();
        assertNotNull(outcome);
    }

    @Test
    void buildLogoutRedirectURIDefaultTest() {
        Holders.config.banner.sso.authenticationProvider = null
        outcome = ControllerUtils.buildLogoutRedirectURI();
        assertNotNull(outcome);
    }

}

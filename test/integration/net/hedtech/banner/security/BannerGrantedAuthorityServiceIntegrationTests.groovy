/*******************************************************************************
Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
*******************************************************************************/
package net.hedtech.banner.security

import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.Before
import org.junit.After
import org.springframework.security.core.Authentication
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.junit.Ignore
import org.junit.Test

/**
 * Integration test for the self service Banner authentication provider.
 **/
class BannerGrantedAuthorityServiceIntegrationTests extends BaseIntegrationTestCase {

    public static final String EDITABLE_USER = "GRAILS_USER"
    public static final String READONLY_USER = "GRAILS_USER_READONLY"
    def dataSource  // injected by Spring

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
    void testDummyTest() {
    }

    @Ignore
    void testDetermineAuthorities() {
        logout()

        Authentication authentication = bannerAuthenticationProvider.authenticate(new UsernamePasswordAuthenticationToken(EDITABLE_USER, "u_pick_it"))
        Map authenticationResults = [ name:           authentication.name,
                                      credentials:    authentication.credentials,
                                      oracleUserName: authentication.name,
                                      valid:          true ].withDefault { k -> false }

        def s = BannerGrantedAuthorityService.determineAuthorities(authenticationResults, dataSource)
        assertNotNull(s)
    }
    @Ignore
    void testFilterAuthoritiesFromFormContext() {
        logout()

        Authentication authentication = bannerAuthenticationProvider.authenticate(new UsernamePasswordAuthenticationToken(EDITABLE_USER, "u_pick_it"))
        Map authenticationResults = [ name:           authentication.name,
                                      credentials:    authentication.credentials,
                                      oracleUserName: authentication.name,
                                      valid:          true ].withDefault { k -> false }

        def s = BannerGrantedAuthorityService.determineAuthorities(authenticationResults, dataSource)
        assertNotNull(s)

        def t = BannerGrantedAuthorityService.filterAuthorities(s)
        assertNotNull(t)
    }
    @Ignore
    void testFilterAuthoritiesForFormNames() {
        logout()

        Authentication authentication = bannerAuthenticationProvider.authenticate(new UsernamePasswordAuthenticationToken(EDITABLE_USER, "u_pick_it"))
        Map authenticationResults = [ name:           authentication.name,
                                      credentials:    authentication.credentials,
                                      oracleUserName: authentication.name,
                                      valid:          true ].withDefault { k -> false }

        def s = BannerGrantedAuthorityService.determineAuthorities(authenticationResults, dataSource)
        assertNotNull(s)

        List formNames = ['SCACRSE', 'GOADISC', 'GORCMSC']
        def t = BannerGrantedAuthorityService.filterAuthorities(formNames, authentication)
        assertNotNull(t)
    }
    @Ignore
    void testGetAuthorityForFormNameAndPatternList () {
        logout()
        login (EDITABLE_USER, "u_pick_it")

        def s = BannerGrantedAuthorityService.getAuthority('SCACRSE', [(AccessPrivilege.READONLY),(AccessPrivilege.READWRITE)])
        assertNotNull(s)
    }
    @Ignore
    void testGetAuthorityForFormNameAndPattern () {
        logout()
        login (EDITABLE_USER, "u_pick_it")

        def s = BannerGrantedAuthorityService.getAuthority('SCACRSE', AccessPrivilege.READWRITE)
        assertNotNull(s)
    }
    @Ignore
    void testGetAuthorityForAnyPattern1 () {
        logout()
        login (EDITABLE_USER, "u_pick_it")

        def authority = BannerGrantedAuthorityService.getAuthorityForAnyAccessPrivilegeType('SCACRSE')
        assertNotNull authority
    }
    @Ignore
    void testIsReadonlyPattern () {
        logout()
        login (READONLY_USER, "u_pick_it")

        assertEquals true, BannerGrantedAuthorityService.isFormReadonly('SCACRSE')
    }

    @Ignore
    void testIsReadWritePattern() {
        logout()
        login (EDITABLE_USER, "u_pick_it")

        assertEquals true, BannerGrantedAuthorityService.isFormEditable('SCACRSE')
    }


    @Ignore
    void testUserRoles () {
        logout()
        login (READONLY_USER, "u_pick_it")
        def roles
        assertNotNull  BannerGrantedAuthorityService.getUserRoles()
    }

}



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

    @Test
    void testDetermineAuthorities() {

        Authentication authentication = bannerAuthenticationProvider.authenticate(new UsernamePasswordAuthenticationToken(EDITABLE_USER, "u_pick_it"))
        Map authenticationResults = [ name:           authentication.name,
                                      credentials:    authentication.credentials,
                                      oracleUserName: authentication.name,
                                      valid:          true ].withDefault { k -> false }

        def s = BannerGrantedAuthorityService.determineAuthorities(authenticationResults, dataSource)
        assertNotNull(s)
    }

    @Test
    void testFilterAuthoritiesFromFormContext() {

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

    @Test
    void testFilterAuthoritiesForFormNames() {

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

    @Test
    void testGetAuthorityForFormNameAndPatternList () {
        login (EDITABLE_USER, "u_pick_it")

        def s = BannerGrantedAuthorityService.getAuthority('SCACRSE', [(AccessPrivilege.READONLY),(AccessPrivilege.READWRITE)])
        assertNotNull(s)
    }

    @Test
    void testGetAuthorityForFormNameAndPattern () {
        login (EDITABLE_USER, "u_pick_it")

        def s = BannerGrantedAuthorityService.getAuthority('SCACRSE', AccessPrivilege.READWRITE)
        assertNotNull(s)
    }

    @Test
    void testGetAuthorityForAnyPattern1 () {
        login (EDITABLE_USER, "u_pick_it")

        def authority = BannerGrantedAuthorityService.getAuthorityForAnyAccessPrivilegeType('SCACRSE')
        assertNotNull authority
    }

    @Test
    void testIsReadonlyPattern () {
        login (READONLY_USER, "u_pick_it")

        assertEquals true, BannerGrantedAuthorityService.isFormReadonly('SCACRSE')
    }

    @Test
    void testIsReadWritePattern() {
        login (EDITABLE_USER, "u_pick_it")

        assertEquals true, BannerGrantedAuthorityService.isFormEditable('SCACRSE')
    }


    @Test
    void testGetSelfServiceUserRole () {
        login (READONLY_USER, "u_pick_it")
        def roles = BannerGrantedAuthorityService.getSelfServiceUserRole()
        assertNotNull roles
    }

    @Test
    void testGetSelfServiceDistinctUserRole(){
        login(READONLY_USER, "u_pick_it")
        def success
        def roles = BannerGrantedAuthorityService.getSelfServiceDistinctUserRole()
        assertTrue(roles.size()<=1000)
        Set<String> dupRoles = new HashSet<String>(roles)
        assertTrue(dupRoles.size()==roles.size())
    }

}



/*******************************************************************************
 Copyright 2009-2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

package net.hedtech.banner.security

import grails.util.Holders
import groovy.sql.Sql
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken

/**
 * Intergration test cases for banner authentication provider
 */
class BannerAuthenticationProviderTests extends BaseIntegrationTestCase {

    private BannerAuthenticationProvider provider
    def conn
    Sql sqlObj
    def usage
    public final String LFMI = "LFMI"
    public final String DEFAULT = "DEFAULT"
    def dataSource


    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        conn = dataSource.getConnection()
        sqlObj = new Sql( conn )
        provider = Holders.applicationContext.getBean("bannerAuthenticationProvider")
    }

    @After
    public void tearDown() {
        sqlObj.close()
        conn.close()
        super.tearDown();
    }

    @Ignore
    @Test
    public void testBannerAuthentiationWithSpecificUsage() {
        Holders.config.ssbEnabled = false
        Holders?.config?.productName = "testApp_LFMI";
        Holders?.config?.banner?.applicationName = "testApp_LFMI";

        def auth = bannerAuthenticationProvider.authenticate( new UsernamePasswordAuthenticationToken( "GRAILS_USER", "u_pick_it" ) )

        assertEquals "WATSON JOHN", auth.fullName

    }

    @Test
    public void testBannerAuthentiationWithDefaultUsage() {
        Holders.config.ssbEnabled = false
        Holders?.config?.productName = "Student";
        Holders?.config?.banner?.applicationName = "testApp";

        def existingUser = [name: "GRAILS_USER", pin: "u_pick_it"]

        def auth = bannerAuthenticationProvider.authenticate( new UsernamePasswordAuthenticationToken( "GRAILS_USER", "u_pick_it" ) )

        assertEquals "WATSON JOHN", auth.fullName

    }

    @Test
    public void testBannerAuthentiationWithOutUsage() {
        Holders.config.ssbEnabled = false
        Holders?.config?.productName = "testApp";
        Holders?.config?.banner?.applicationName = "testApp";

        def existingUser = [name: "GRAILS_USER", pin: "u_pick_it"]

        def auth = bannerAuthenticationProvider.authenticate( new UsernamePasswordAuthenticationToken( "GRAILS_USER", "u_pick_it" ) )

        assertEquals "WATSON JOHN", auth.fullName

    }

    @Test
    public void testSupports () {
        assertTrue(provider.supports(UsernamePasswordAuthenticationToken.class))
        Holders.config.administrativeBannerEnabled = true
        assertTrue(provider.supports(UsernamePasswordAuthenticationToken.class))
    }

    @Test
    public void testGetFullName () {
        assertNotNull(provider.getFullName("HOP510001", dataSource))
        assertNotNull(provider.getFullName("ADVISOR1", dataSource))
    }

}

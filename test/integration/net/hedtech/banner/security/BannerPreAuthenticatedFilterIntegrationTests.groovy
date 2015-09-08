/*******************************************************************************
 Copyright 2009-2014 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

package net.hedtech.banner.security

import groovy.sql.Sql
import net.hedtech.banner.testing.BaseIntegrationTestCase
import grails.util.Holders
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.mock.web.MockFilterChain
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder


class BannerPreAuthenticatedFilterIntegrationTests extends BaseIntegrationTestCase {

    def bannerPreAuthenticatedFilter

    @Before
    public void setUp() {
        createPerson()
        def bannerID = generateBannerId()
        def bannerPIDM = generatePidm()
        Holders?.config.banner.sso.authenticationAssertionAttribute = "UDC_IDENTIFIER"
        Holders?.config.banner.sso.authenticationProvider = "external"
    }
    @After
    public void tearDown() {
        deletePerson()
        logout()
    }

    @Test
    void testAdminDoFilter() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/ssb/foo/secure/super/somefile.html");
        request.strippedServletPath =  "/ssb/foo/secure/super/somefile.html"
        request.addHeader("UDC_IDENTIFIER", "E52CE2A2B7E89BC2E0401895D626728A")

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        bannerPreAuthenticatedFilter.doFilter(request, response, chain);

        assertNotNull(SecurityContextHolder.context.getAuthentication())

    }
    @Test
    void testSSBDoFilter() {
        if (!isSsbEnabled()) return
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/ssb/foo/secure/super/somefile2.html");
        request.strippedServletPath =  "/ssb/foo/secure/super/somefile.html"
        request.addHeader("UDC_IDENTIFIER", "99999SAML99999")

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        bannerPreAuthenticatedFilter.doFilter(request, response, chain);

        assertNotNull(SecurityContextHolder.context.getAuthentication())
    }

    @Test
    void testAttributeNull() {

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/ssb/foo/secure/super/somefile2.html");
        request.strippedServletPath =  "/ssb/foo/secure/super/somefile.html"
        // do not add any headers

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        try {

            bannerPreAuthenticatedFilter.doFilter(request, response, chain);
        } catch(Exception e) {
            assertEquals("System is configured for external authentication and identity assertion UDC_IDENTIFIER is null", e.message)
        }

    }

    @Test
    void testBannerUserNotFound() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/ssb/foo/secure/super/somefile2.html");
        request.strippedServletPath =  "/ssb/foo/secure/super/somefile.html"
        request.addHeader("UDC_IDENTIFIER", "2")


        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        try {

            bannerPreAuthenticatedFilter.doFilter(request, response, chain);
        } catch(Exception e) {
            assertEquals("System is configured for external authentication, identity assertion 2 does not map to a Banner user", e.message)
        }

    }

    @Test
    void testFilterSkip() {
        //URL specified does not match to any Intercept url

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/foo/secure/super/somefile2.html");
        request.strippedServletPath =  "/foo/secure/super/somefile.html"
        request.addHeader("UDC_IDENTIFIER", "2")


        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        bannerPreAuthenticatedFilter.doFilter(request, response, chain);

        assertNull(SecurityContextHolder.context.getAuthentication())
    }

    @Test
    void testFilterMultiAntUrlMatch() {
        //Intercept URl set is
        //'/external/test/**':         ['ROLE_SELFSERVICE-STUDENT_BAN_DEFAULT_M','ROLE_SELFSERVICE-GUEST_BAN_DEFAULT_M'],
        ///'/external/**':              ['IS_AUTHENTICATED_ANONYMOUSLY'],
        //
        if (!isSsbEnabled()) return
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/external/test/foo/super/somefile2.html");
        request.strippedServletPath =  "/external/test/foo/super/somefile.html"
        request.addHeader("UDC_IDENTIFIER", "99999SAML99999")


        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        bannerPreAuthenticatedFilter.doFilter(request, response, chain);

        assertNotNull(SecurityContextHolder.context.getAuthentication())
    }
    /**
     * Please put all the custom tests in this protected section to protect the code
     * from being overwritten on re-generation
     */
    /*PROTECTED REGION ID(MenuAndToolbarPreference_custom_integration_test_methods) ENABLED START*/
    /*PROTECTED REGION END*/

    private def isSsbEnabled() {
        Holders.config.ssbEnabled instanceof Boolean ? Holders.config.ssbEnabled : false
    }

    private def generateBannerId() {
        def configFile = new File("${System.properties['user.home']}/.grails/banner_configuration.groovy")
        def slurper = new ConfigSlurper(grails.util.GrailsUtil.environment)
        def config = slurper.parse(configFile.toURI().toURL())
        def url = config.get("bannerDataSource").url
        def sql = Sql.newInstance(url,   //  db =  new Sql( connectInfo.url,
                "baninst1",
                "u_pick_it",
                'oracle.jdbc.driver.OracleDriver')
        String idSql = """select gb_common.f_generate_id bannerId from dual """
        def bannerValues = sql.firstRow(idSql)

        sql?.close() // note that the test will close the connection, since it's our current session's connection

        return bannerValues.bannerId
    }


    private def generatePidm() {
        def configFile = new File("${System.properties['user.home']}/.grails/banner_configuration.groovy")
        def slurper = new ConfigSlurper(grails.util.GrailsUtil.environment)
        def config = slurper.parse(configFile.toURI().toURL())
        def url = config.get("bannerDataSource").url
        def sql = Sql.newInstance(url,   //  db =  new Sql( connectInfo.url,
                "baninst1",
                "u_pick_it",
                'oracle.jdbc.driver.OracleDriver')
        String idSql = """select gb_common.f_generate_pidm pidm from dual """
        def bannerValues = sql.firstRow(idSql)

        sql?.close() // note that the test will close the connection, since it's our current session's connection


        return bannerValues.pidm
    }

    private void generateSpridenRecord(bannerId, bannerPidm) {
        def configFile = new File("${System.properties['user.home']}/.grails/banner_configuration.groovy")
        def slurper = new ConfigSlurper(grails.util.GrailsUtil.environment)
        def config = slurper.parse(configFile.toURI().toURL())
        def url = config.get("bannerDataSource").url
        def sql = Sql.newInstance(url,   //  db =  new Sql( connectInfo.url,
                "baninst1",
                "u_pick_it",
                'oracle.jdbc.driver.OracleDriver')
        sql.call("""
         declare

         Lv_Id_Ref Gb_Identification.Identification_Ref;
         spriden_current Gb_Identification.identification_rec;
         test_pidm spriden.spriden_pidm%type;
         test_rowid varchar2(30);
         begin

         gb_identification.p_create(
         P_ID_INOUT => ${bannerId},
         P_LAST_NAME => 'Miller',
         P_FIRST_NAME => 'Ann',
         P_MI => 'Elizabeth',
         P_CHANGE_IND => NULL,
         P_ENTITY_IND => 'P',
         P_User => User,
         P_ORIGIN => 'banner',
         P_NTYP_CODE => NULL,
         P_DATA_ORIGIN => 'banner',
         P_PIDM_INOUT => ${bannerPidm},
         P_Rowid_Out => Test_Rowid);
         end ;
         """)

        sql.close()
    }



    private def generateUDCIDMappingPIDM(pidm) {
        def configFile = new File("${System.properties['user.home']}/.grails/banner_configuration.groovy")
        def slurper = new ConfigSlurper(grails.util.GrailsUtil.environment)
        def config = slurper.parse(configFile.toURI().toURL())
        def url = config.get("bannerDataSource").url
        def db = Sql.newInstance(url,   //  db =  new Sql( connectInfo.url,
                "baninst1",
                "u_pick_it",
                'oracle.jdbc.driver.OracleDriver')

        db.call("""
         declare
         test_rowid varchar2(30);
         begin

         gb_gobumap.p_create(
         p_udc_id => '99999SAML99999',
         p_pidm => ${pidm},
         p_create_date => sysdate,
         p_user_id => 'banner',
         p_data_origin => 'banner',
         P_Rowid_Out => Test_Rowid);

         end ;
         """)


        String idSql = """select GOBUMAP_UDC_ID from gobumap where gobumap_udc_id = '99999SAML99999' """
        def bannerValues = db.firstRow(idSql)
        db.close()
        return bannerValues.GOBUMAP_UDC_ID
    }

    private void deleteUDCIDMappingPIDM() {
        def configFile = new File("${System.properties['user.home']}/.grails/banner_configuration.groovy")
        def slurper = new ConfigSlurper(grails.util.GrailsUtil.environment)
        def config = slurper.parse(configFile.toURI().toURL())
        def url = config.get("bannerDataSource").url
        def db = Sql.newInstance(url,   //  db =  new Sql( connectInfo.url,
                "baninst1",
                "u_pick_it",
                'oracle.jdbc.driver.OracleDriver')

        db.call("""
         declare
         test_rowid varchar2(30);
         begin

         gb_gobumap.p_delete(
         p_udc_id => '99999SAML99999');

         end ;
         """)

        db.close()
    }

    private void createPerson() {
        def bannerID = generateBannerId()
        def bannerPIDM = generatePidm()
        generateSpridenRecord(bannerID, bannerPIDM)
        // add authorities to new user
        def udc_id =  generateUDCIDMappingPIDM(bannerPIDM)

    }

    private void deletePerson() {
        deleteUDCIDMappingPIDM()

    }
}

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
    def grailsApplication

    def bannerPIDM

    public static final String UDC_IDENTIFIER = '99999SAML99999'

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        clearGOBEACC()
        createPerson()

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
        Holders.config.ssbEnabled = false
        Holders?.config.grails.plugin.springsecurity.interceptUrlMap.remove("/**")
        Holders?.config.grails.plugin.springsecurity.interceptUrlMap.put('/ssb/**', ['ROLE_SELFSERVICE-STUDENT_BAN_DEFAULT_M', 'ROLE_SELFSERVICE-GUEST_BAN_DEFAULT_M'])

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/ssb/foo");

        request.addHeader("UDC_IDENTIFIER", UDC_IDENTIFIER)
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        def authenticationResults = bannerPreAuthenticatedFilter.doFilter(request, response, chain);

        assertEquals SecurityContextHolder.context.getAuthentication().user.pidm, Integer.valueOf(bannerPIDM.intValue())

        assertNotNull(SecurityContextHolder.context.getAuthentication())


    }


    @Test
    void testAttributeNull() {


        Holders.config.ssbEnabled = false
        Holders?.config.grails.plugin.springsecurity.interceptUrlMap.remove("/**")
        Holders?.config.grails.plugin.springsecurity.interceptUrlMap.put('/ssb/**', ['ROLE_SELFSERVICE-STUDENT_BAN_DEFAULT_M', 'ROLE_SELFSERVICE-GUEST_BAN_DEFAULT_M'])

        MockHttpServletRequest request = new MockHttpServletRequest();

        request.setRequestURI("/ssb/foo");

        SecurityContextHolder.context?.authentication = null  //clear context

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();


        def authenticationResults = bannerPreAuthenticatedFilter.doFilter(request, response, chain);

        def msg = request.getSession()?.SPRING_SECURITY_LAST_EXCEPTION?.getMessage()
        assertEquals msg, "System is configured for external authentication and identity assertion UDC_IDENTIFIER is null"
        assertEquals response.getRedirectedUrl(), "/login/error"
    }


    @Test
    void testBannerUserNotFound() {
        Holders.config.ssbEnabled = false
        MockHttpServletRequest request = new MockHttpServletRequest();
        Holders?.config.grails.plugin.springsecurity.interceptUrlMap.remove("/**")
        Holders?.config.grails.plugin.springsecurity.interceptUrlMap.put('/ssb/**', ['ROLE_SELFSERVICE-STUDENT_BAN_DEFAULT_M', 'ROLE_SELFSERVICE-GUEST_BAN_DEFAULT_M'])

        def udc_identifier = "2"
        request.addHeader("UDC_IDENTIFIER", udc_identifier)

        SecurityContextHolder.context?.authentication = null  //clear context

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        def authenticationResults = bannerPreAuthenticatedFilter.doFilter(request, response, chain);

        assertEquals response.getRedirectedUrl(), "/login/error"

        def msg = request.getSession()?.SPRING_SECURITY_LAST_EXCEPTION?.getMessage()

        assertEquals msg, "System is configured for external authentication, identity assertion 2 does not map to a Banner user"
        assertNull(SecurityContextHolder.context.getAuthentication())

    }


    @Test
    void testFilterSkip() {
        Holders.config.ssbEnabled = false
        Holders?.config.grails.plugin.springsecurity.interceptUrlMap.remove("/**")
        Holders?.config.grails.plugin.springsecurity.interceptUrlMap.put('/ssb/**', ['IS_AUTHENTICATED_ANONYMOUSLY'])

        MockHttpServletRequest request = new MockHttpServletRequest();

        request.setRequestURI("/ssb/foo");
        request.addHeader("UDC_IDENTIFIER", UDC_IDENTIFIER)

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        SecurityContextHolder.context?.authentication = null  //clear context

        bannerPreAuthenticatedFilter.doFilter(request, response, chain);

        assertNull(SecurityContextHolder.context.getAuthentication())
    }


    @Test
    void testFilterMultiAntUrlMatch() {
        Holders.config.ssbEnabled = false
        //Intercept URl set is
        //'/external/test/**':         ['ROLE_SELFSERVICE-STUDENT_BAN_DEFAULT_M','ROLE_SELFSERVICE-GUEST_BAN_DEFAULT_M'],
        ///'/external/**':              ['IS_AUTHENTICATED_ANONYMOUSLY'],
        //
        Holders?.config.grails.plugin.springsecurity.interceptUrlMap.remove("/**")
        Holders?.config.grails.plugin.springsecurity.interceptUrlMap.put('/external/test/**', ['ROLE_SELFSERVICE-STUDENT_BAN_DEFAULT_M', 'ROLE_SELFSERVICE-GUEST_BAN_DEFAULT_M'])
        Holders?.config.grails.plugin.springsecurity.interceptUrlMap.put('/external/**', ['IS_AUTHENTICATED_ANONYMOUSLY'])
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/external/test/foo/super/somefile2.html");

        request.addHeader("UDC_IDENTIFIER", UDC_IDENTIFIER)

        SecurityContextHolder.context?.authentication = null  //clear context

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        bannerPreAuthenticatedFilter.doFilter(request, response, chain);

        assertNotNull(SecurityContextHolder.context.getAuthentication())

        assertEquals SecurityContextHolder.context.getAuthentication().user.pidm, Integer.valueOf(bannerPIDM.intValue())
    }

    //----------------------------- Helper Methods ------------------------------

    private def isSsbEnabled() {
        Holders.config.ssbEnabled instanceof Boolean ? Holders.config.ssbEnabled : false
    }


    private def generateBannerId() {

        def sql = getDB();

        String idSql = """select gb_common.f_generate_id bannerId from dual """
        def bannerValues = sql.firstRow(idSql)

        sql?.close() // note that the test will close the connection, since it's our current session's connection

        return bannerValues.bannerId
    }


    private def generatePidm() {

        def sql = getDB();

        String idSql = """select gb_common.f_generate_pidm pidm from dual """
        def bannerValues = sql.firstRow(idSql)

        sql?.close() // note that the test will close the connection, since it's our current session's connection

        return bannerValues.pidm
    }


    private void generateSpridenRecord(bannerId, bannerPidm) {

        def sql = getDB();

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

        sql.commit()
        sql.close()
    }


    private def generateUDCIDMappingPIDM(pidm) {

        def db = getDB();

        db.call("""
         declare
         test_rowid varchar2(30);
         begin

         gb_gobumap.p_create(
         p_udc_id => ${UDC_IDENTIFIER},
         p_pidm => ${pidm},
         p_create_date => sysdate,
         p_user_id => 'banner',
         p_data_origin => 'banner',
         P_Rowid_Out => Test_Rowid);

         end ;
         """)


        String idSql = """select GOBUMAP_UDC_ID from gobumap where gobumap_udc_id = '${UDC_IDENTIFIER}' """
        def bannerValues = db.firstRow(idSql)
        def spridenId
        def sqlStatement2 = '''SELECT spriden_id, gobumap_pidm FROM gobumap,spriden WHERE spriden_pidm = gobumap_pidm AND spriden_change_ind is null AND gobumap_udc_id = ?'''
        db.eachRow(sqlStatement2, [UDC_IDENTIFIER]) { row ->
            spridenId = row.spriden_id
            pidm = row.gobumap_pidm
        }

        db.commit()
        db.close()

        return bannerValues.GOBUMAP_UDC_ID
    }


    private void deleteUDCIDMappingPIDM() {

        def db = getDB();

        db.call("""
         declare
         test_rowid varchar2(30);
         begin

         gb_gobumap.p_delete(
         p_udc_id => ${UDC_IDENTIFIER});

         end ;
         """)

        db.commit()
        db.close()
    }


    private void createPerson() {
        def bannerID = generateBannerId()
        bannerPIDM = generatePidm()
        generateSpridenRecord(bannerID, bannerPIDM)
        // add authorities to new user
        def udc_id = generateUDCIDMappingPIDM(bannerPIDM)
        createGOBEACC(bannerPIDM)
    }

    private void deletePerson() {
        deleteUDCIDMappingPIDM()
        deletePersonData(bannerPIDM)
        deleteGOBEACC(bannerPIDM)
    }


    private void deletePersonData(pidm) {

        def db = getDB();

        db.executeUpdate("delete spriden where spriden_pidm=${pidm}")
        db.commit()
        db.close()
    }


    private void createGOBEACC(pidm) {

        def db = getDB();

        db.executeUpdate("insert into gobeacc(gobeacc_pidm, gobeacc_username, gobeacc_user_id, gobeacc_activity_date) values (${pidm},'GRAILS_USER',user,sysdate)")
        db.commit()
        db.close()
    }


    private void deleteGOBEACC(pidm) {

        def db = getDB();

        db.executeUpdate("delete gobeacc where gobeacc_pidm=${pidm}")
        db.commit()
        db.close()
    }


    private void clearGOBEACC() {

        def db = getDB();
        db.executeUpdate("delete gobeacc where gobeacc_username = 'GRAILS_USER'")
        db.commit()
        db.close()
    }

    private getDB() {
        def configFile = new File("${System.properties['user.home']}/.grails/banner_configuration.groovy")
        def slurper = new ConfigSlurper(grails.util.GrailsUtil.environment)
        def config = slurper.parse(configFile.toURI().toURL())
        def url = config.get("bannerDataSource").url
        def db = Sql.newInstance(url,   //  db =  new Sql( connectInfo.url,
                "baninst1",
                "u_pick_it",
                'oracle.jdbc.driver.OracleDriver')
        db
    }
}



package net.hedtech.banner.security

import grails.spring.BeanBuilder
import grails.util.Environment
import grails.util.Holders
import groovy.sql.Sql
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.apache.commons.dbcp.BasicDataSource
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.context.ApplicationContext
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
        conn = dataSource.getConnection()
        sqlObj = new Sql( conn )
        provider = new BannerAuthenticationProvider()
        super.setUp()
    }

    @After
    public void tearDown() {
        sqlObj.close()
        conn.close()
        super.tearDown();
    }

    @Test
    public void testBannerAuthentiationWithSpecificUsage() {
        Holders.config.ssbEnabled = false
        Holders?.config?.productName = "testApp";
        Holders?.config?.banner?.applicationName = "testApp";
        usage = LFMI

        deleteDisplayNameRule(usage);
        insertDisplayNameRule(usage);

        def existingUser = [name: "GRAILS_USER", pin: "u_pick_it"]

        def auth = provider.authenticate(new TestAuthenticationRequest(existingUser))

        assertEquals "GRAILS_USER", auth.fullName

        deleteDisplayNameRule();

    }

    @Test
    public void testBannerAuthentiationWithDefaultUsage() {
        Holders.config.ssbEnabled = false
        Holders?.config?.productName = "testApp";
        Holders?.config?.banner?.applicationName = "testApp";
        usage = DEFAULT

        deleteDisplayNameRule(usage);
        insertDisplayNameRule(usage);

        def existingUser = [name: "GRAILS_USER", pin: "u_pick_it"]

        def auth = provider.authenticate(new TestAuthenticationRequest(existingUser))

        assertEquals "GRAILS_USER", auth.fullName

        deleteDisplayNameRule();

    }

    @Test
    public void testBannerAuthentiationWithOutUsage() {
        Holders.config.ssbEnabled = false
        Holders?.config?.productName = "testApp";
        Holders?.config?.banner?.applicationName = "testApp";
        usage = null

        deleteDisplayNameRule(usage);
        insertDisplayNameRule(usage);

        def existingUser = [name: "GRAILS_USER", pin: "u_pick_it"]

        def auth = provider.authenticate(new TestAuthenticationRequest(existingUser))

        assertEquals "GRAILS_USER", auth.fullName

        deleteDisplayNameRule();

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

    private void insertDisplayNameRule(usage) {

        if (usage != null) {
            sqlObj.executeUpdate("INSERT INTO GURNHIR(GURNHIR_PRODUCT,GURNHIR_APPLICATION,GURNHIR_PAGE,GURNHIR_SECTION,GURNHIR_USAGE,GURNHIR_ACTIVE_IND," +
                    "GURNHIR_MAX_LENGTH,GURNHIR_ACTIVITY_DATE,GURNHIR_USER_ID,GURNHIR_DATA_ORIGIN)SELECT 'testApp','testApp',null,null,'" + usage + "','Y',2000," +
                    "SYSDATE,'BASELINE','BANNER' FROM dual where not exists (select 'x' from gurnhir where gurnhir_product='testApp' and " +
                    "gurnhir_application is null and gurnhir_page is null and gurnhir_section is null)");
        } else {
            sqlObj.executeUpdate("INSERT INTO GURNHIR(GURNHIR_PRODUCT,GURNHIR_APPLICATION,GURNHIR_PAGE,GURNHIR_SECTION,GURNHIR_USAGE,GURNHIR_ACTIVE_IND," +
                    "GURNHIR_MAX_LENGTH,GURNHIR_ACTIVITY_DATE,GURNHIR_USER_ID,GURNHIR_DATA_ORIGIN)SELECT 'testApp','testApp',null,null,null,'Y',2000," +
                    "SYSDATE,'BASELINE','BANNER' FROM dual where not exists (select 'x' from gurnhir where gurnhir_product='testApp' and " +
                    "gurnhir_application is null and gurnhir_page is null and gurnhir_section is null)");
        }
        sqlObj.commit();
    }

    private void deleteDisplayNameRule() {
        sqlObj.executeUpdate("DELETE GURNHIR WHERE GURNHIR_PRODUCT='testApp'");
        sqlObj.commit();
    }

    private void deleteDisplayNameRule(usage) {
        def result
        def deleteQueryWithoutUsage = "DELETE FROM GURNHIR WHERE GURNHIR_PRODUCT = 'testApp' AND GURNHIR_APPLICATION = 'testApp'"
        if (usage != null) {
            result = sqlObj.executeUpdate(deleteQueryWithoutUsage + " AND GURNHIR_USAGE = '" + usage + "'");
        } else {
            sqlObj.executeUpdate(deleteQueryWithoutUsage);
        }
        if (result && result == 0) {
            sqlObj.executeUpdate(deleteQueryWithoutUsage);
        }
    }


}

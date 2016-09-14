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

/**
 * Intergration test cases for banner authentication provider
 */
class BannerAuthenticationProviderTests extends BaseIntegrationTestCase {

    public static final String PERSON_HOSWEB002 = 'HOSWEB002'
    private BannerAuthenticationProvider provider
    def conn
    def usage
    public final String LFMI = "LFMI"
    public final String DEFAULT = "DEFAULT"
    def dataSource


    @Before
    public void setUp() {
        provider = Holders.applicationContext.getBean("bannerAuthenticationProvider")
        conn = dataSource.getConnection()
    }

    @After
    public void tearDown() {
        conn.close()
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

    private void insertDisplayNameRule(usage) {
        def db = getDB();

        if (usage != null) {
            db.executeUpdate("INSERT INTO GURNHIR(GURNHIR_PRODUCT,GURNHIR_APPLICATION,GURNHIR_PAGE,GURNHIR_SECTION,GURNHIR_USAGE,GURNHIR_ACTIVE_IND," +
                    "GURNHIR_MAX_LENGTH,GURNHIR_ACTIVITY_DATE,GURNHIR_USER_ID,GURNHIR_DATA_ORIGIN)SELECT 'testApp','testApp',null,null,'" + usage + "','Y',2000," +
                    "SYSDATE,'BASELINE','BANNER' FROM dual where not exists (select 'x' from gurnhir where gurnhir_product='testApp' and " +
                    "gurnhir_application is null and gurnhir_page is null and gurnhir_section is null)");
        } else {
            db.executeUpdate("INSERT INTO GURNHIR(GURNHIR_PRODUCT,GURNHIR_APPLICATION,GURNHIR_PAGE,GURNHIR_SECTION,GURNHIR_USAGE,GURNHIR_ACTIVE_IND," +
                    "GURNHIR_MAX_LENGTH,GURNHIR_ACTIVITY_DATE,GURNHIR_USER_ID,GURNHIR_DATA_ORIGIN)SELECT 'testApp','testApp',null,null,null,'Y',2000," +
                    "SYSDATE,'BASELINE','BANNER' FROM dual where not exists (select 'x' from gurnhir where gurnhir_product='testApp' and " +
                    "gurnhir_application is null and gurnhir_page is null and gurnhir_section is null)");
        }
        db.commit();
        db.close();
    }

    private void deleteDisplayNameRule() {
        def db = getDB();

        db.executeUpdate("DELETE GURNHIR WHERE GURNHIR_PRODUCT='testApp'");
        db.commit();
        db.close();
    }

    private void deleteDisplayNameRule(usage) {
        def db = getDB();
        def result
        def deleteQueryWithoutUsage = "DELETE FROM GURNHIR WHERE GURNHIR_PRODUCT = 'testApp' AND GURNHIR_APPLICATION = 'testApp'"
        if (usage != null) {
            result = db.executeUpdate(deleteQueryWithoutUsage + " AND GURNHIR_USAGE = '" + usage + "'");
        } else {
            db.executeUpdate(deleteQueryWithoutUsage);
        }
        if (result && result == 0) {
            db.executeUpdate(deleteQueryWithoutUsage);
        }
        db.commit();
        db.close();
    }

    private static getDB() {
        def configFile = new File("${System.properties['user.home']}/.grails/banner_configuration.groovy")
        def slurper = new ConfigSlurper(Environment.getCurrent().getName())
        def config = slurper.parse(configFile.toURI().toURL())
        def url = config.get("bannerDataSource").url
        def db = Sql.newInstance(url,   //  db =  new Sql( connectInfo.url,
                "baninst1",
                "u_pick_it",
                'oracle.jdbc.driver.OracleDriver')
        db
    }
}

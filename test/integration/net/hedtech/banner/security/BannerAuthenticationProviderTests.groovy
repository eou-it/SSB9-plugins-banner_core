package net.hedtech.banner.security

import grails.spring.BeanBuilder
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
class BannerAuthenticationProviderTests extends BaseIntegrationTestCase{

    public static final String PERSON_HOSWEB002 = 'HOSWEB002'
    private BannerAuthenticationProvider provider
    def conn
    Sql sqlObj
    def testUser
    def usage
    public final String LFMI= "LFMI"
    public final String DEFAULT= "DEFAULT"
    def dataSource



    @Before
    public void setUp() {
        ApplicationContext testSpringContext = createUnderlyingDataSourceBean()
        dataSource.underlyingDataSource =  testSpringContext.getBean("underlyingDataSource")

        provider = Holders.applicationContext.getBean("bannerAuthenticationProvider")
        conn = dataSource.getConnection()
        sqlObj = new Sql( conn )
    }

    @After
    public void tearDown() {
        sqlObj.close()
        dataSource.underlyingDataSource =  null;

    }



    @Test
    public void testBannerAuthentiationWithSpecificUsage () {
        Holders.config.ssbEnabled = false
        Holders?.config?.productName ="testApp";
        Holders?.config?.banner.applicationName ="testApp";
        usage=LFMI

        insertDisplayNameRule(usage);

        def existingUser = [ name: "CSZKARAD",pin:"u_pick_it"]

        def auth = provider.authenticate( new TestAuthenticationRequest( existingUser ) )

        assertEquals "Szkaradnik, Cindy" , auth.fullName

        deleteDisplayNameRule();

    }

    @Test
    public void testBannerAuthentiationWithDefaultUsage () {
        Holders.config.ssbEnabled = false
        Holders?.config?.productName ="testApp";
        Holders?.config?.banner.applicationName ="testApp";
        usage=DEFAULT

        insertDisplayNameRule(usage);

        def existingUser = [ name: "CSZKARAD",pin:"u_pick_it"]

        def auth = provider.authenticate( new TestAuthenticationRequest( existingUser ) )

        assertEquals "Cindy Szkaradnik" , auth.fullName

        deleteDisplayNameRule();

    }

    @Test
    public void testBannerAuthentiationWithOutUsage () {
        Holders.config.ssbEnabled = false
        Holders?.config?.productName ="testApp";
        Holders?.config?.banner.applicationName ="testApp";
        usage=null

        insertDisplayNameRule(usage);

        def existingUser = [ name: "CSZKARAD",pin:"u_pick_it"]

        def auth = provider.authenticate( new TestAuthenticationRequest( existingUser ) )

        assertEquals "Cindy Szkaradnik" , auth.fullName

        deleteDisplayNameRule();

    }

    private void insertDisplayNameRule(usage){
        def db = getDB();

        if(usage!=null){
            db.executeUpdate("INSERT INTO GURNHIR(GURNHIR_PRODUCT,GURNHIR_APPLICATION,GURNHIR_PAGE,GURNHIR_SECTION,GURNHIR_USAGE,GURNHIR_ACTIVE_IND," +
                    "GURNHIR_MAX_LENGTH,GURNHIR_ACTIVITY_DATE,GURNHIR_USER_ID,GURNHIR_DATA_ORIGIN)SELECT 'testApp','testApp',null,null,'"+usage+"','Y',2000," +
                    "SYSDATE,'BASELINE','BANNER' FROM dual where not exists (select 'x' from gurnhir where gurnhir_product='testApp' and " +
                    "gurnhir_application is null and gurnhir_page is null and gurnhir_section is null)");
        }else{
            db.executeUpdate("INSERT INTO GURNHIR(GURNHIR_PRODUCT,GURNHIR_APPLICATION,GURNHIR_PAGE,GURNHIR_SECTION,GURNHIR_USAGE,GURNHIR_ACTIVE_IND," +
                    "GURNHIR_MAX_LENGTH,GURNHIR_ACTIVITY_DATE,GURNHIR_USER_ID,GURNHIR_DATA_ORIGIN)SELECT 'testApp','testApp',null,null,null,'Y',2000," +
                    "SYSDATE,'BASELINE','BANNER' FROM dual where not exists (select 'x' from gurnhir where gurnhir_product='testApp' and " +
                    "gurnhir_application is null and gurnhir_page is null and gurnhir_section is null)");
        }
        db.commit();
        db.close();
    }

    private void deleteDisplayNameRule(){
        def db = getDB();

        db.executeUpdate("DELETE GURNHIR WHERE GURNHIR_PRODUCT='testApp'");
        db.commit();
        db.close();
    }

    private ApplicationContext createUnderlyingDataSourceBean() {
        def bb = new BeanBuilder()
        bb.beans {
            underlyingDataSource(BasicDataSource) {
                maxActive = 5
                maxIdle = 2
                defaultAutoCommit = "false"
                driverClassName = "${Holders.config.bannerDataSource.driver}"
                url = "${Holders.config.bannerDataSource.url}"
                password = "${Holders.config.bannerDataSource.password}"
                username = "${Holders.config.bannerDataSource.username}"
            }
        }
        ApplicationContext testSpringContext = bb.createApplicationContext()
        return testSpringContext
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



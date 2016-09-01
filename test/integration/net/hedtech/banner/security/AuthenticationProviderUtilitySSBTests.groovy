/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
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
 * Integration test for the AuthenticationProviderUtility class.
 **/
class AuthenticationProviderUtilitySSBTests  extends BaseIntegrationTestCase{

    def authenticationProviderUtility
    def dataSource
    def usage
    public final String DEFAULT= "DEFAULT"
    public final String LFMI= "LFMI"
    public static final String UDC_IDENTIFIER = '99999SSB99999'

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()

        ApplicationContext testSpringContext = createUnderlyingSsbDataSourceBean()
        dataSource.underlyingSsbDataSource =  testSpringContext.getBean("underlyingSsbDataSource")

        authenticationProviderUtility = new AuthenticationProviderUtility()

    }

    @After
    public void tearDown() {
        super.tearDown();


    }

    @Test
    void testRetrievalOfRoleBasedTimeouts() {
        def timeouts = authenticationProviderUtility.retrieveRoleBasedTimeOuts( dataSource )
        assertTrue timeouts.size() > 0
    }

    @Test
    void testGetUserFullNameWithDisplayNameRuleWithDefaultUsage(){
        Holders?.config.ssbEnabled = true
        Holders?.config.ssbOracleUsersProxied = false

        Holders?.config?.productName ="testApp";
        Holders?.config?.banner.applicationName ="testApp";

        def bannerID = generateBannerId();
        def bannerPidm = generatePidm();
        usage=DEFAULT

        insertDisplayNameRule(usage);
        generateSpridenRecord(bannerID, bannerPidm);
        addStudentRoleToSpriden(bannerPidm);
        createGOBEACC(bannerPidm, bannerID);

        def bannerUDCID = generateUDCIDMappingPIDM(bannerPidm)

        def authResults = authenticationProviderUtility.getMappedUserForUdcId(bannerUDCID, dataSource )

        def fullName=authenticationProviderUtility.getUserFullName(bannerPidm,authResults["name"],dataSource);

        assertEquals "Ann Elizabeth Miller", fullName

        deleteUDCIDMappingPIDM()
        deleteSpriden(bannerPidm)
        deleteDisplayNameRule();
    }

    @Test
    void testGetUserFullNameWithDisplayNameRuleWithLFMIUsage(){
        Holders?.config.ssbEnabled = true
        Holders?.config.ssbOracleUsersProxied = false

        Holders?.config?.productName ="testApp";
        Holders?.config?.banner.applicationName ="testApp";

        def bannerID = generateBannerId();
        def bannerPidm = generatePidm();
        usage=LFMI

        insertDisplayNameRule(usage);
        generateSpridenRecord(bannerID, bannerPidm);
        addStudentRoleToSpriden(bannerPidm);
        createGOBEACC(bannerPidm, bannerID);


        def bannerUDCID = generateUDCIDMappingPIDM(bannerPidm)

        def authResults = authenticationProviderUtility.getMappedUserForUdcId(bannerUDCID, dataSource )

        def fullName=authenticationProviderUtility.getUserFullName(bannerPidm,authResults["name"],dataSource);


        assertEquals "Miller, Ann E.", fullName

        deleteUDCIDMappingPIDM()
        deleteSpriden(bannerPidm)
        deleteDisplayNameRule();
    }

    @Test
    void testGetUserFullNameWithDisplayNameRuleWithoutUsage(){
        Holders?.config.ssbEnabled = true
        Holders?.config.ssbOracleUsersProxied = false

        Holders?.config?.productName ="testApp";
        Holders?.config?.banner.applicationName ="testApp";

        def bannerID = generateBannerId();
        def bannerPidm = generatePidm();
        usage=null

        insertDisplayNameRule(usage);
        generateSpridenRecord(bannerID, bannerPidm);
        addStudentRoleToSpriden(bannerPidm);
        createGOBEACC(bannerPidm, bannerID);

        def bannerUDCID = generateUDCIDMappingPIDM(bannerPidm)

        def authResults = authenticationProviderUtility.getMappedUserForUdcId(bannerUDCID, dataSource )

        def fullName=authenticationProviderUtility.getUserFullName(bannerPidm,authResults["name"],dataSource);

        assertEquals "Ann Elizabeth Miller", fullName

        deleteUDCIDMappingPIDM()
        deleteSpriden(bannerPidm)
        deleteDisplayNameRule();
    }

    @Test
    void testGetUserFullNameWithDisplayNameRuleWithoutRule(){
        Holders?.config.ssbEnabled = true
        Holders?.config.ssbOracleUsersProxied = false

        Holders?.config?.productName ="testApp";
        Holders?.config?.banner.applicationName ="testApp";

        def bannerID = generateBannerId();
        def bannerPidm = generatePidm();

        generateSpridenRecord(bannerID, bannerPidm);
        addStudentRoleToSpriden(bannerPidm);
        createGOBEACC(bannerPidm, bannerID);

        def bannerUDCID = generateUDCIDMappingPIDM(bannerPidm)

        def authResults = authenticationProviderUtility.getMappedUserForUdcId(bannerUDCID, dataSource )

        def fullName=authenticationProviderUtility.getUserFullName(bannerPidm,authResults["name"],dataSource);

        assertEquals "Ann Elizabeth Miller", fullName

        deleteUDCIDMappingPIDM()
        deleteSpriden(bannerPidm)
    }

    @Test
    void testGetMappedUserForUdcIdOracleNull() {
        Holders?.config.ssbEnabled = true
        Holders?.config.ssbOracleUsersProxied = false

        def bannerPidm = generatePidm()
        def bannerId = "SSSTNDT"

        generateSpridenRecord(bannerId, bannerPidm)
        addStudentRoleToSpriden(bannerPidm)

        def bannerUDCID = generateUDCIDMappingPIDM(bannerPidm)

        def authResults = authenticationProviderUtility.getMappedUserForUdcId(bannerUDCID, dataSource )

        assertNotNull(authResults)
        assertNull(authResults["oracleUserName"])

        deleteUDCIDMappingPIDM()
        deleteSpriden(bannerPidm)
    }

    @Test
    void testGetMappedUserForUdcIdOracleNotNull() {
        Holders?.config.ssbEnabled = true
        Holders?.config.ssbOracleUsersProxied = false

        def bannerID = generateBannerId()
        def bannerPidm = generatePidm()

        generateSpridenRecord(bannerID, bannerPidm)
        addStudentRoleToSpriden(bannerPidm)
        createGOBEACC(bannerPidm, bannerID)

        def bannerUDCID = generateUDCIDMappingPIDM(bannerPidm)

        def authResults = authenticationProviderUtility.getMappedUserForUdcId(bannerUDCID, dataSource )

        assertNotNull(authResults)
        assertNotNull(authResults["oracleUserName"])

        deleteUDCIDMappingPIDM()
        deleteSpriden(bannerPidm)
    }

    private ApplicationContext createUnderlyingSsbDataSourceBean() {
        def bb = new BeanBuilder()
        bb.beans {
            underlyingSsbDataSource(BasicDataSource) {
                maxActive = 5
                maxIdle = 2
                defaultAutoCommit = "false"
                driverClassName = "${Holders.config.bannerSsbDataSource.driver}"
                url = "${Holders.config.bannerSsbDataSource.url}"
                password = "${Holders.config.bannerSsbDataSource.password}"
                username = "${Holders.config.bannerSsbDataSource.username}"
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

    private void addStudentRoleToSpriden(pidm) {

        def db = getDB();

        db.executeUpdate("Insert Into Twgrrole ( Twgrrole_Pidm, Twgrrole_Role, Twgrrole_Activity_Date) values ( ${pidm}, 'STUDENT', Sysdate)")
        db.commit()
        db.executeUpdate("INSERT INTO SGBSTDN (SGBSTDN_PIDM,SGBSTDN_TERM_CODE_EFF,SGBSTDN_STST_CODE,SGBSTDN_LEVL_CODE,SGBSTDN_STYP_CODE,SGBSTDN_TERM_CODE_ADMIT,SGBSTDN_CAMP_CODE,SGBSTDN_RESD_CODE,SGBSTDN_COLL_CODE_1,SGBSTDN_DEGC_CODE_1,SGBSTDN_MAJR_CODE_1,SGBSTDN_ACTIVITY_DATE,SGBSTDN_BLCK_CODE,SGBSTDN_PRIM_ROLL_IND,SGBSTDN_PROGRAM_1,SGBSTDN_DATA_ORIGIN,SGBSTDN_USER_ID,SGBSTDN_SURROGATE_ID,SGBSTDN_VERSION) values (${pidm},'201410','AS','UG','S','201410','M','R','AS','BA','HIST',to_date('02-MAR-14','DD-MON-RR'),'NUTR','N','BA-HIST','Banner','BANPROXY',SGBSTDN_SURROGATE_ID_SEQUENCE.nextval,1)")
        db.commit()
        db.close()

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

    private void deleteSpriden(pidm) {

        def db = getDB();

        db.executeUpdate("delete spriden where spriden_pidm=${pidm}")
        db.commit()
        db.close()
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

    private def generateBannerId() {

        def sql = getDB();

        String idSql = """select gb_common.f_generate_id bannerId from dual """
        def bannerValues = sql.firstRow(idSql)

        sql?.close() // note that the test will close the connection, since it's our current session's connection

        return bannerValues.bannerId
    }

    private void createGOBEACC(pidm, bannerId) {

        def db = getDB();

        db.executeUpdate("insert into gobeacc(gobeacc_pidm, gobeacc_username, gobeacc_user_id, gobeacc_activity_date) values (${pidm},${bannerId},user,sysdate)")
        db.commit()
        db.close()
    }

}

/*******************************************************************************
 Copyright 2019 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.audit


import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import grails.util.Holders
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.Test
import org.springframework.web.context.request.RequestContextHolder
import net.hedtech.banner.general.utility.PersonalPreference

@Integration
@Rollback
class LoginAuditIntegrationTests extends BaseIntegrationTestCase {

    private String appName
    private String appId
    private Date auditTime
    private String loginId
    private String ipAddress
    private String userAgent
    private Date lastModified
    private String lastModifiedBy
    private Long id
    private Long version
    private Integer pidm
    private String logonComment
    private String dataOrigin


    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        logout()
        loginSSB('HOSH00001', '111111')
        appName = Holders.grailsApplication.config.info.app.name
        appId = Holders.config.info.app.appId
        auditTime = new Date()
        loginId = 'HOSH00010'
        ipAddress = InetAddress.getLocalHost().getHostAddress()
        userAgent = System.getProperty('os.name')
        lastModified =  new Date()
        lastModifiedBy = 'Test User'
        pidm = 49436
        logonComment = 'Test Comment'
        dataOrigin = 'Banner'

    }

    @After
    public void tearDown() {
        super.tearDown()
    }

    @AfterClass
    public static void cleanUp() {
        RequestContextHolder.resetRequestAttributes()
    }

    @Test
    void testCreateLoginAudit() {
        LoginAudit loginAudit = newLoginAudit()
        loginAudit.save(failOnError: true, flush: true)
        assertNotNull loginAudit?.id

        loginAudit = loginAudit.refresh()
        assertEquals 0L, loginAudit.version
        assertNotNull loginAudit.appId
    }

    @Test
    void testNotNullLoginId() {
        LoginAudit loginAudit = newLoginAudit()
        loginAudit.save(failOnError: true, flush: true)
        assertNotNull loginAudit.loginId
    }

    @Test
    void testDeleteLoginId() {
        LoginAudit loginAudit = newLoginAudit()
        loginAudit.save(failOnError: true, flush: true)
        assertNotNull loginAudit.id
        def id = loginAudit.id
        loginAudit.delete()
        assertNull loginAudit.get(id)
    }

    @Test
    void testUpdateLoginId() {
        LoginAudit loginAudit = newLoginAudit()
        loginAudit.save(failOnError: true, flush: true)
        loginAudit = loginAudit.refresh()

        assertNotNull loginAudit.id
        assertEquals 0L, loginAudit.version

        //Update the entity
        loginAudit.loginId = "PSA 2"
        loginAudit = loginAudit.save(failOnError: true, flush: true)


        loginAudit = loginAudit.get(loginAudit.id)
        assertEquals "PSA 2", loginAudit.loginId

    }


    @Test
    void testToString() {
        LoginAudit loginAudit = newLoginAudit()
        loginAudit.save(failOnError: true, flush: true)
        LoginAudit loginAudit1 = LoginAudit.fetchByLoginId(loginId)
        String LoginIdToString = loginAudit1.toString()
        assertNotNull LoginIdToString
        

    }

    @Test
    void testFetchByNullLoginId() {
        LoginAudit loginAudit = LoginAudit.fetchByLoginId(null)
        assertNull loginAudit
    }

    @Test
    void testFetchByNullAppId() {
        LoginAudit loginAudit = LoginAudit.fetchByAppId(null)
        assertNull loginAudit
    }

    @Test
    void testFetchByValidLoginId() {
        LoginAudit loginAudit = newLoginAudit()
        loginAudit.save(failOnError: true, flush: true)
        LoginAudit auditPage = LoginAudit.fetchByLoginId(loginId)
        assertNotNull auditPage
    }

    @Test
    void testFetchByValidAppId() {
        LoginAudit loginAudit = newLoginAudit()
        loginAudit.save(failOnError: true, flush: true)
        LoginAudit auditPage = LoginAudit.fetchByAppId(appId)
        assertNotNull auditPage
    }


    @Test
    void testEqualsClass() {
        LoginAudit loginAudit = new LoginAudit()
        PersonalPreference personalPreference=new PersonalPreference()
        assertFalse loginAudit.equals(personalPreference)
    }

    @Test
    void testHashCode() {
        LoginAudit loginAudit = newLoginAudit()
        loginAudit.save(failOnError: true, flush: true)
        LoginAudit loginAudit1 = LoginAudit.fetchByLoginId(loginId)
        int LoginIdHashCode = loginAudit1.hashCode()
        assertNotNull LoginIdHashCode
    }


    private LoginAudit newLoginAudit() {
        LoginAudit loginAudit = new LoginAudit(

                auditTime: auditTime,
                loginId: loginId,
                pidm:pidm,
                appId:appId,
                lastModified: lastModified,
                lastModifiedBy: lastModifiedBy,
                dataOrigin: dataOrigin,

                ipAddress: ipAddress,
                logonComment: logonComment,
                userAgent: userAgent,
                id: id

        )
        return loginAudit
    }
}

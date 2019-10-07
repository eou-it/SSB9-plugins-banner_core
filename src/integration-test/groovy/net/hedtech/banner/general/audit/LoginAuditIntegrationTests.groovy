/*******************************************************************************
 Copyright 2019 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.audit


import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import grails.util.Holders
import groovy.sql.Sql
import net.hedtech.banner.security.BannerGrantedAuthorityService
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.Test
import static groovy.test.GroovyAssert.shouldFail
import org.springframework.orm.hibernate5.HibernateOptimisticLockingFailureException
import org.springframework.web.context.request.RequestContextHolder
import net.hedtech.banner.general.utility.PersonalPreference

@Integration
@Rollback
class LoginAuditIntegrationTests extends BaseIntegrationTestCase {

    def appId

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        logout()
        loginSSB('HOSH00001', '111111')
        appId="Test PSA"

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
        List loginAuditList = LoginAudit.fetchByLoginId(loginAudit.loginId)
        assertNotNull loginAuditList
       assertTrue(loginAuditList.size() >= 1 )
    }

    @Test
    void testFetchByNullLoginId() {
        List loginAudit = LoginAudit.fetchByLoginId(null)
        assertEquals loginAudit.size() , 0
    }

    @Test
    void testFetchByValidLoginId() {
        LoginAudit loginAudit = newLoginAudit()
        loginAudit.save(failOnError: true, flush: true)
        List auditPage = LoginAudit.fetchByLoginId(loginAudit.loginId)
        assertEquals auditPage.size() , 1
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
        List loginAudit1 = LoginAudit.fetchByLoginId(loginAudit.loginId)
        int LoginIdHashCode = loginAudit1.hashCode()
        assertNotNull LoginIdHashCode
    }

    @Test
    public void testSerialization() {
        try {
            LoginAudit loginAudit = newLoginAudit()
            loginAudit.save(failOnError: true, flush: true)
            ByteArrayOutputStream out = new ByteArrayOutputStream()
            ObjectOutputStream oos = new ObjectOutputStream(out)
            oos.writeObject(loginAudit)
            oos.close()

            byte[] bytes = out.toByteArray()
            LoginAudit loginAudit1 = null
            new ByteArrayInputStream(bytes).withObjectInputStream(getClass().classLoader) { is ->
                loginAudit1 = (LoginAudit) is.readObject()
                is.close()
            }
            assertEquals loginAudit1, loginAudit

        } catch (e) {
            e.printStackTrace()
        }
    }

    @Test
    void testEqualsLastModifiedEqual() {
        LoginAudit loginAudit1 = new LoginAudit(appId: "TestId",lastModified: new Date(12,2,12))
        LoginAudit loginAudit2 = new LoginAudit(appId: "TestId1",lastModified: new Date(12,2,14))
        assertFalse loginAudit2==loginAudit1
    }

    @Test
    void testEqualsLastModifiedNotEqual() {
        LoginAudit loginAudit1 = new LoginAudit(appId: "TestId",lastModified: new Date(12,2,12))
        LoginAudit loginAudit2 = new LoginAudit(appId: "TestId1",lastModified: new Date(12,2,12))
        assertFalse loginAudit2==loginAudit1
    }

    @Test
    void testEqualsLastModifiedAppNameNotEqual() {
        LoginAudit loginAudit1 = new LoginAudit(appId: "TestId",lastModified: new Date(12,2,12))
        LoginAudit loginAudit2 = new LoginAudit(appId: "TestId",lastModified: new Date(12,2,12))
        assertTrue loginAudit2==loginAudit1
    }

    @Test
    void testEqualsDataOriginNotEqual() {
        LoginAudit loginAudit1 = new LoginAudit(appId: "TestId",lastModified: new Date(12,2,12),dataOrigin: "GENERAL")
        LoginAudit loginAudit2 = new LoginAudit(appId: "TestId",lastModified: new Date(12,2,12),dataOrigin: "BANNER")
        assertFalse loginAudit2==loginAudit1
    }

    /*@Test
    void testOptimisticLock() {
        LoginAudit loginAudit = newLoginAudit()
        save loginAudit

        def sql= new Sql(sessionFactory.getCurrentSession().connection())
        sql.executeUpdate("update general.GURASSL set GURASSL_VERSION = 999 where GURASSL_SURROGATE_ID = ?", [loginAudit.id])

        //Try to update the entity
        loginAudit.appId = 'Test AppId'
        shouldFail(HibernateOptimisticLockingFailureException) {
            loginAudit.save(flush: true)
        }
    }
*/
    private LoginAudit newLoginAudit() {
        def user = BannerGrantedAuthorityService.getUser()
        def request = RequestContextHolder.getRequestAttributes()?.request
        TimeZone.setDefault(TimeZone.getTimeZone('UTC'))
        LoginAudit loginAudit = new LoginAudit(
                auditTime: new Date(),
                loginId: user.username,
                pidm: user.pidm,
                appId: appId,
                lastModified: new Date(),
                lastModifiedBy: appId,
                dataOrigin: Holders.config.dataOrigin,
                ipAddress: request.getRemoteAddr(),
                logonComment: 'Login successful.',
                userAgent: System.getProperty('os.name'),
                version: 0L
        )
        return loginAudit
    }
}

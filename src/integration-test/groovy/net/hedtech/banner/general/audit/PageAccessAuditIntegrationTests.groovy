/*******************************************************************************
 Copyright 2019 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.audit

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import grails.util.Holders
import net.hedtech.banner.security.BannerGrantedAuthorityService
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.Test
import org.springframework.web.context.request.RequestContextHolder
import net.hedtech.banner.general.utility.PersonalPreference

@Integration
@Rollback
class PageAccessAuditIntegrationTests extends BaseIntegrationTestCase {

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
    void testCreateSelfServiceAccess() {
        PageAccessAudit pageAccessAudit = getPageAccessAudit()
        pageAccessAudit.save(failOnError: true, flush: true)
    }

    @Test
    void testNotNullLoginId() {
        PageAccessAudit pageAccessAudit = getPageAccessAudit()
        pageAccessAudit.save(failOnError: true, flush: true)
        assertNotNull pageAccessAudit.loginId
    }

    @Test
    void testDeleteSelfServiceAccess() {
        PageAccessAudit pageAccessAudit = getPageAccessAudit()
        pageAccessAudit.save(failOnError: true, flush: true)
        assertNotNull pageAccessAudit.id
        def id = pageAccessAudit.id
        pageAccessAudit.delete()
        assertNull pageAccessAudit.get(id)
    }

    @Test
    void testUpdateSelfServiceAccess() {
        PageAccessAudit pageAccessAudit = getPageAccessAudit()
        pageAccessAudit.save(failOnError: true, flush: true)
        pageAccessAudit = pageAccessAudit.refresh()

        assertNotNull pageAccessAudit.id
        assertEquals 0L, pageAccessAudit.version
        assertEquals "Banner", pageAccessAudit.dataOrigin

        //Update the Version entity
        pageAccessAudit.loginId = "PSA 2"
        pageAccessAudit = pageAccessAudit.save(failOnError: true, flush: true)


        pageAccessAudit = pageAccessAudit.get(pageAccessAudit.id)
        assertEquals "PSA 2", pageAccessAudit.loginId
    }


    @Test
    void testToString() {
        PageAccessAudit pageAccessAudit = getPageAccessAudit()
        pageAccessAudit.save(failOnError: true, flush: true)

        PageAccessAudit pageAccessAudit1 = PageAccessAudit.fetchByLoginId(pageAccessAudit.loginId)
        String pageAccessAuditToString = pageAccessAudit1.toString()
        assertNotNull pageAccessAuditToString
    }

    @Test
    void testFetchByNullLoginId() {
        PageAccessAudit pageAccessAudit = PageAccessAudit.fetchByLoginId(null)
        assertNull pageAccessAudit
    }

    @Test
    void testFetchByNullAppId() {
        PageAccessAudit pageAccessAudit = PageAccessAudit.fetchByAppId(null)
        assertNull pageAccessAudit
    }

    @Test
    void testFetchByValidLoginId() {
        PageAccessAudit pageAccessAudit = getPageAccessAudit()
        pageAccessAudit.save(failOnError: true, flush: true)

        PageAccessAudit pageAccessAudit1 = PageAccessAudit.fetchByLoginId(pageAccessAudit.loginId)
        assertNotNull pageAccessAudit1
    }

    @Test
    void testFetchByValidAppId() {
        PageAccessAudit selfServicePageAccess = getPageAccessAudit()
        selfServicePageAccess.save(failOnError: true, flush: true)

        PageAccessAudit selfServicePage = PageAccessAudit.fetchByAppId(selfServicePageAccess.appId)
        assertNotNull selfServicePage
    }


    @Test
    void testEqualsClass() {
        PageAccessAudit selfServicePageAccess = new PageAccessAudit()
        PersonalPreference personalPreference=new PersonalPreference()
        assertFalse selfServicePageAccess.equals(personalPreference)
    }

    @Test
    void testHashCode() {
        PageAccessAudit pageAccessAudit = getPageAccessAudit()
        pageAccessAudit.save(failOnError: true, flush: true)
        PageAccessAudit pageAccessAudit1 = PageAccessAudit.fetchByLoginId(pageAccessAudit.loginId)
        int pageAccessAuditToHash = pageAccessAudit1.hashCode()
        assertNotNull pageAccessAuditToHash
    }

    @Test
    public void testSerialization() {
        try {
            PageAccessAudit pageAccessAudit = getPageAccessAudit()
            pageAccessAudit.save(failOnError: true, flush: true)
            ByteArrayOutputStream out = new ByteArrayOutputStream()
            ObjectOutputStream oos = new ObjectOutputStream(out)
            oos.writeObject(pageAccessAudit)
            oos.close()

            byte[] bytes = out.toByteArray()
            PageAccessAudit pageAccessAudit1
            new ByteArrayInputStream(bytes).withObjectInputStream(getClass().classLoader) { is ->
                pageAccessAudit1 = (PageAccessAudit) is.readObject()
                is.close()
            }
            assertEquals pageAccessAudit1, pageAccessAudit

        } catch (e) {
            e.printStackTrace()
        }
    }

    @Test
    void testEqualsLastModifiedEqual() {
        PageAccessAudit pageAccessAudit1 = new PageAccessAudit(appId: "TestId",lastModified: new Date(12,2,12))
        PageAccessAudit pageAccessAudit2 = new PageAccessAudit(appId: "TestId1",lastModified: new Date(12,2,14))
        assertFalse pageAccessAudit2==pageAccessAudit1
    }

    @Test
    void testEqualsLastModifiedNotEqual() {
        PageAccessAudit pageAccessAudit1 = new PageAccessAudit(appId: "TestId",lastModified: new Date(12,2,12))
        PageAccessAudit pageAccessAudit2 = new PageAccessAudit(appId: "TestId1",lastModified: new Date(12,2,12))
        assertFalse pageAccessAudit2==pageAccessAudit1
    }

    @Test
    void testEqualsLastModifiedAppNameNotEqual() {
        PageAccessAudit pageAccessAudit1 = new PageAccessAudit(appId: "TestId",lastModified: new Date(12,2,12))
        PageAccessAudit pageAccessAudit2 = new PageAccessAudit(appId: "TestId",lastModified: new Date(12,2,12))
        assertTrue pageAccessAudit2==pageAccessAudit1
    }

    @Test
    void testEqualsDataOriginNotEqual() {
        PageAccessAudit pageAccessAudit1 = new PageAccessAudit(appId: "TestId",lastModified: new Date(12,2,12),dataOrigin: "GENERAL")
        PageAccessAudit pageAccessAudit2 = new PageAccessAudit(appId: "TestId",lastModified: new Date(12,2,12),dataOrigin: "BANNER")
        assertFalse pageAccessAudit2==pageAccessAudit1
    }


    private PageAccessAudit getPageAccessAudit() {
        def user = BannerGrantedAuthorityService.getUser()
        PageAccessAudit selfServicePageAccess = new PageAccessAudit(
                auditTime: new Date(),
                loginId: user.username,
                pidm: user.pidm,
                appId: appId,
                lastModified: new Date(),
                lastModifiedBy: appId,
                dataOrigin: Holders.config.dataOrigin,
                ipAddress: InetAddress.getLocalHost().getHostAddress(),
                pageUrl: "test Pageid"
        )
        return selfServicePageAccess
    }


}

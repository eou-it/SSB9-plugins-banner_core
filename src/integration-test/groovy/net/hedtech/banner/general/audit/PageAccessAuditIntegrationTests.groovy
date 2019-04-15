/*******************************************************************************
 Copyright 2019 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.audit

import grails.config.ConfigProperties
import grails.gorm.transactions.Rollback
import grails.gorm.transactions.Transactional
import grails.testing.mixin.integration.Integration
import grails.util.Holders
import net.hedtech.banner.general.audit.PageAccessAudit
import net.hedtech.banner.security.BannerGrantedAuthorityService
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.Test
import org.springframework.web.context.request.RequestContextHolder

@Integration
@Rollback
class PageAccessAuditIntegrationTests extends BaseIntegrationTestCase {

    private String appName
    private String appId
    private String loginId
    private String pageUrl
    private Integer pidm
    private String dataOrigin
    private String lastModifiedBy
    private String ipAddress
    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        logout()
        loginSSB('HOSH00001', '111111')
        def user = BannerGrantedAuthorityService.getUser()
        pidm = user.pidm
        loginId = user.username
        ipAddress = InetAddress.getLocalHost().getHostAddress()
        appId = Holders.config.app.appId
        appName = Holders.config.info.app.name
        dataOrigin = Holders.config.dataOrigin
        lastModifiedBy = Holders.config.app.appId
        pageUrl = "test Pageid"
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
        pageAccessAudit.version = 123L
        pageAccessAudit = pageAccessAudit.save(failOnError: true, flush: true)


        pageAccessAudit = pageAccessAudit.get(pageAccessAudit.id)
        assertEquals 123L, pageAccessAudit.version
    }


    @Test
    void testToString() {
        PageAccessAudit pageAccessAudit = getPageAccessAudit()
        pageAccessAudit.save(failOnError: true, flush: true)

        PageAccessAudit pageAccessAudit1 = PageAccessAudit.fetchByLoginId(loginId)
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

        PageAccessAudit pageAccessAudit1 = PageAccessAudit.fetchByLoginId(loginId)
        assertNotNull pageAccessAudit1
    }

    @Test
    void testFetchByValidAppId() {
        PageAccessAudit selfServicePageAccess = getPageAccessAudit()
        selfServicePageAccess.save(failOnError: true, flush: true)

        PageAccessAudit selfServicePage = PageAccessAudit.fetchByAppId(appId)
        assertNotNull selfServicePage
    }


    @Test
    void testEqualsClass() {
        PageAccessAudit selfServicePageAccess = new PageAccessAudit()
        ConfigProperties configProperties=new ConfigProperties()
        assertFalse selfServicePageAccess.equals(configProperties)
    }

    @Test
    void testEqualsIs() {
        PageAccessAudit selfServicePageAccess = new PageAccessAudit()
        assertTrue selfServicePageAccess.equals(selfServicePageAccess)
    }

    @Test
    void testHashCode() {
        PageAccessAudit pageAccessAudit = getPageAccessAudit()
        pageAccessAudit.save(failOnError: true, flush: true)
        PageAccessAudit pageAccessAudit1 = PageAccessAudit.fetchByLoginId(loginId)
        int pageAccessAuditToHash = pageAccessAudit1.hashCode()
        assertNotNull pageAccessAuditToHash
    }


    private PageAccessAudit getPageAccessAudit() {
        PageAccessAudit selfServicePageAccess = new PageAccessAudit(
                auditTime: new Date(),
                loginId: loginId,
                pidm:pidm,
                appId:appId,
                lastModified: new Date(),
                lastModifiedBy: lastModifiedBy,
                dataOrigin: dataOrigin,
                ipAddress: ipAddress,
                pageUrl: pageUrl

        )
        return selfServicePageAccess
    }


}

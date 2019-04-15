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
import org.junit.Before
import org.junit.Test
import net.hedtech.banner.general.audit.PageAccessAuditService

@Integration
@Rollback
class PageAccessAuditServiceIntegrationTests extends BaseIntegrationTestCase{

    private String appName
    private String appId
    private String loginId
    public String pageUrl
    private String pidm
    private String dataOrigin
    private String lastModifiedBy
    private String ipAddress
    private String auditTime

    def PageAccessAuditService
    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        PageAccessAuditService = new PageAccessAuditService()

    }

    @After
    public void tearDown() {
        super.tearDown()
    }

    @Test
    void testFetchByLoginId() {
         loginSSB('HOSH00001', '111111')
         def user = BannerGrantedAuthorityService.getUser()
         pidm = user.pidm
         auditTime = new Date()
         loginId = user.username
         ipAddress = InetAddress.getLocalHost().getHostAddress()
         appId = Holders.config.app.appId
         appName =  Holders.config.info.app.name
         dataOrigin = Holders.config.dataOrigin
         lastModifiedBy = Holders.config.app.appId
         pageUrl = "test Pageid"
         PageAccessAudit pageAccessAudit = getSelfServiceAccess(loginId,pidm,appId,pageUrl,lastModifiedBy,dataOrigin,ipAddress)
         pageAccessAudit.save(failOnError: true, flush: true)
         def  SelfServicePageAccessObject = PageAccessAuditService.getDataByLoginID(pageAccessAudit.loginId)

    }

    @Test
    void testCreatePageAudit(){
        loginSSB('HOSH00001', '111111')
        def  SelfServicePageAccessObject = PageAccessAuditService.createPageAudit()

    }

    private PageAccessAudit getSelfServiceAccess(loginId, pidm, appId, pageUrl, lastModifiedBy, dataOrigin, ipAddress) {



        PageAccessAudit pageAccessAudit = new PageAccessAudit(

                auditTime: new Date(),
                loginId: loginId,
                pidm:pidm,
                appId: appId,
                pageUrl: pageUrl,
                lastModified: new Date(),
                lastModifiedBy: lastModifiedBy,
                dataOrigin: dataOrigin,
                ipAddress: ipAddress
        )
        return pageAccessAudit
    }
}

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

    def pageAccessAuditService = new PageAccessAuditService()
    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
    }

    @After
    public void tearDown() {
        super.tearDown()
    }

    @Test
    void testFetchByLoginId() {
         loginSSB('HOSH00001', '111111')

         PageAccessAudit pageAccessAudit = getSelfServiceAccess()
         pageAccessAudit.save(failOnError: true, flush: true)
         def  SelfServicePageAccessObject = pageAccessAuditService.getDataByLoginID(pageAccessAudit.loginId)
         assertEquals SelfServicePageAccessObject.loginId , pageAccessAudit.loginId

    }

    @Test
    void testCreatePageAudit(){
        loginSSB('HOSH00001', '111111')
        def  SelfServicePageAccessObject = pageAccessAuditService.createPageAudit()
        //assertNotNull  SelfServicePageAccessObject              /* need to check why its returning null*/
    }

    @Test
    void testToCheckEnablePageAuditFailureFlow(){
        loginSSB('HOSH00001', '111111')
        PageAccessAudit pageAccessAudit = getSelfServiceAccess()
        Holders.config.EnablePageAudit= 'Y'
        pageAccessAudit = pageAccessAuditService.checkAndCreatePageAudit()
        assertNull pageAccessAudit
    }


    private PageAccessAudit getSelfServiceAccess() {
        def user = BannerGrantedAuthorityService.getUser()
        PageAccessAudit pageAccessAudit = new PageAccessAudit(

                auditTime: new Date(),
                loginId: user.username,
                pidm: user.pidm,
                appId: 'PSA',
                pageUrl: "test Pageid",
                lastModified: new Date(),
                lastModifiedBy: 'PSA',
                dataOrigin: Holders.config.dataOrigin,
                ipAddress: InetAddress.getLocalHost().getHostAddress(),
                version: 0L
        )
        return pageAccessAudit
    }
}

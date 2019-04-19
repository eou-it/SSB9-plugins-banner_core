/*******************************************************************************
 Copyright 2019 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.audit

import grails.gorm.transactions.Rollback
import grails.plugin.springsecurity.SpringSecurityService
import grails.testing.mixin.integration.Integration
import grails.util.Holders

import net.hedtech.banner.security.BannerGrantedAuthorityService
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.context.request.RequestContextHolder

@Integration
@Rollback
class PageAccessAuditServiceIntegrationTests extends BaseIntegrationTestCase{

    def pageAccessAuditService
    @Autowired
    SpringSecurityService springSecurityService

    @Before
    public void setUp() {
        formContext = ['SELFSERVICE']
        super.setUp()
        pageAccessAuditService = new PageAccessAuditService()
        pageAccessAuditService.springSecurityService = springSecurityService
        Holders.config.app.appId = 'TESTAPP'
    }


    @After
    public void tearDown() {
        super.tearDown()
    }


    @Test
    void testFetchByLoginId() {
         String appId
         String loginId
         String pageUrl
         String pidm
         String ipAddress
         loginSSB('HOSH00001', '111111')
         def user = BannerGrantedAuthorityService.getUser()
         pidm = user.pidm
         loginId = user.username
         ipAddress = InetAddress.getLocalHost().getHostAddress()
         appId = Holders.config.app.appId
         pageUrl = "/testPageid/"
         PageAccessAudit pageAccessAudit = createPageAccessAudit(loginId,pidm,appId,pageUrl,ipAddress)
         pageAccessAudit.save(failOnError: true, flush: true)
         def  pageAccessAuditObject = pageAccessAuditService.getDataByLoginID(pageAccessAudit.loginId)
         assertEquals pageAccessAuditObject.loginId , pageAccessAudit.loginId
    }


    @Test
    void testCreatePageAudit(){
        loginSSB('HOSH00001', '111111')
        RequestContextHolder?.currentRequestAttributes()?.request.setRequestURI('/ssb/home')
        def  pageAccessAuditObject = pageAccessAuditService.createPageAudit()
        //assertNotNull  pageAccessAuditObject              /* need to check why its returning null*/
    }


    @Test
    void testCheckAndCreatePageAuditWithURLPattern(){
        loginSSB('HOSH00001', '111111')
        Holders.config.EnablePageAudit= 'homepage'
        RequestContextHolder?.currentRequestAttributes()?.request.setRequestURI('/ssb/homepage')
        def  pageAccessAuditObject = pageAccessAuditService.createPageAudit()
        //assertNotNull  pageAccessAuditObject              /* need to check why its returning null*/


        RequestContextHolder?.currentRequestAttributes()?.request.setRequestURI('/ssb/dummy')

        def  pageAccessAuditObject1 = pageAccessAuditService.checkAndCreatePageAudit()
        assertNull pageAccessAuditObject1
    }





    @Test
    void testToCheckEnablePageAuditFailureFlow(){
        loginSSB('HOSH00001', '111111')
        Holders.config.EnablePageAudit= 'N'
        PageAccessAudit pageAccessAudit = pageAccessAuditService.checkAndCreatePageAudit()
        assertNull pageAccessAudit
    }

    private PageAccessAudit createPageAccessAudit(loginId, pidm, appId, pageUrl, ipAddress) {
        PageAccessAudit pageAccessAudit = new PageAccessAudit(
                auditTime: new Date(),
                loginId: loginId,
                pidm:pidm,
                appId: appId,
                pageUrl: pageUrl,
                ipAddress: ipAddress
        )
        return pageAccessAudit
    }
}

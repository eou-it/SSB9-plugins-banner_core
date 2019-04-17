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


@Integration
@Rollback
class LoginAuditServiceIntegrationTests extends BaseIntegrationTestCase{


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
    private String vpdiCode

    def LoginAuditService
    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        LoginAuditService = new LoginAuditService()

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
        appId = Holders.config.info.app.appId
        dataOrigin = Holders.config.dataOrigin
        lastModifiedBy = Holders.config.info.app.appId
        userAgent = System.getProperty('os.name')
        logonComment = 'Login SuccessFul'
        LoginAudit loginAudit = newLoginAudit(loginId,pidm,appId,lastModifiedBy,userAgent,dataOrigin,ipAddress,logonComment)
        loginAudit.save(failOnError: true, flush: true)
        def  LoginAuditObject = LoginAuditService.getDataByLoginID(loginAudit.loginId)

    }

    @Test
    void testCreateLoginAudit(){
        loginSSB('HOSH00001', '111111')
        def  LoginAuditObject = LoginAuditService.createLoginAudit()

    }

    private LoginAudit newLoginAudit(loginId, pidm, appId,lastModifiedBy,userAgent, dataOrigin, ipAddress,logonComment) {

        LoginAudit loginAudit = new LoginAudit(
                auditTime: new Date(),
                loginId: loginId,
                pidm:pidm,
                appId:appId,
                lastModified: new Date(),
                lastModifiedBy: lastModifiedBy,
                dataOrigin: dataOrigin,
                ipAddress: ipAddress,
                userAgent: userAgent,
                logonComment: logonComment

        )
        return loginAudit
    }
}

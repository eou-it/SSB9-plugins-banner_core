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

    def loginAuditService = new LoginAuditService()
    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        logout()
        loginSSB('HOSH00001', '111111')
    }

    @After
    public void tearDown() {
        super.tearDown()
    }

    @Test
    void testFetchByLoginId() {
        loginSSB('HOSH00001', '111111')
        LoginAudit loginAudit = newLoginAudit()
        loginAudit.save(failOnError: true, flush: true)
        def  loginAuditObject = loginAuditService.getDataByLoginID(loginAudit.loginId)
        assertEquals loginAuditObject.loginId , loginAudit.loginId

    }

    @Test
    void testCreateLoginAudit(){
        loginSSB('HOSH00001', '111111')
        def  loginAuditObject = loginAuditService.createLoginLogoutAudit()
        assertNotNull loginAuditObject

    }

    private LoginAudit newLoginAudit() {

        def user = BannerGrantedAuthorityService.getUser()
        LoginAudit loginAudit = new LoginAudit(
                auditTime: new Date(),
                loginId: user.username,
                pidm: user.pidm,
                appId: 'PSA',
                lastModified: new Date(),
                lastModifiedBy: 'PSA',
                dataOrigin: Holders.config.dataOrigin,
                ipAddress: InetAddress.getLocalHost().getHostAddress(),
                userAgent: System.getProperty('os.name'),
                logonComment: 'Test Comment',
                version: 0L

        )
        return loginAudit
    }
}

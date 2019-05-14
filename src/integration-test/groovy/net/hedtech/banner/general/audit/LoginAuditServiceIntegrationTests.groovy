/*******************************************************************************
 Copyright 2019 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.audit

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import grails.util.GrailsWebMockUtil
import grails.util.Holders
import grails.web.http.HttpHeaders
import grails.web.servlet.context.GrailsWebApplicationContext
import net.hedtech.banner.security.BannerGrantedAuthorityService
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.grails.web.util.GrailsApplicationAttributes
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.context.request.RequestContextHolder

import javax.servlet.http.HttpServletRequest


@Integration
@Rollback
class LoginAuditServiceIntegrationTests extends BaseIntegrationTestCase{

    LoginAuditService loginAuditService

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        logout()
        loginSSB('HOSH00001', '111111')
        Holders.config.app.appId = 'TESTAPP'
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
        loginAuditObject.each { it ->
            assertEquals it.loginId , loginAudit.loginId
        }
    }

    @Test
    void testCreateLoginAudit(){
        loginSSB('HOSH00001', '111111')
        def user = BannerGrantedAuthorityService.getUser()
        MockHttpServletRequest request  =  RequestContextHolder.currentRequestAttributes().request
        request.addHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.103 Safari/537.36")
        def  loginAuditObject = loginAuditService.createLoginLogoutAudit(user.username, user.pidm, 'Login Successful')
        assertNotNull loginAuditObject

    }

    private LoginAudit newLoginAudit() {

        def user = BannerGrantedAuthorityService.getUser()
        LoginAudit loginAudit = new LoginAudit(
                auditTime: new Date(),
                loginId: user.username,
                pidm: user.pidm,
                appId: Holders.config.app.appId ,
                lastModified: new Date(),
                lastModifiedBy: Holders.config.app.appId ,
                dataOrigin: Holders.config.dataOrigin,
                ipAddress: InetAddress.getLocalHost().getHostAddress(),
                userAgent: System.getProperty('os.name'),
                logonComment: 'Test Comment',
                version: 0L

        )
        return loginAudit
    }
}

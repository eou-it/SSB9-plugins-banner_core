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
import org.grails.plugins.testing.GrailsMockHttpServletRequest
import org.grails.web.util.GrailsApplicationAttributes
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.context.request.RequestContextHolder
import org.apache.http.conn.util.InetAddressUtils



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

    @Test
    void testLoginEnableLoginAuditSetY() {
        Holders.config.EnableLoginAudit = 'Y'
        MockHttpServletRequest request  =  RequestContextHolder.currentRequestAttributes().request
        request.addHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.103 Safari/537.36")
        def  loginAuditObject = loginAuditService.getDataByLoginID('HOSH00001')
        loginSSB('HOSH00001', '111111')
        def  loginAuditObject1 = loginAuditService.getDataByLoginID('HOSH00001')
        assertEquals loginAuditObject.size() , loginAuditObject1.size()-1
    }

    @Test
    void testLoginEnableLoginAuditSetN() {
        Holders.config.EnableLoginAudit = 'N'
        MockHttpServletRequest request  =  RequestContextHolder.currentRequestAttributes().request
        request.addHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.103 Safari/537.36")
        def  loginAuditObject = loginAuditService.getDataByLoginID('HOSH00001')
        loginSSB('HOSH00001', '111111')
        def  loginAuditObject1 = loginAuditService.getDataByLoginID('HOSH00001')
        assertEquals loginAuditObject.size() , loginAuditObject1.size()
    }

    @Test
    void testgetloadBalancerIpAddress() {
        loginSSB('HOSH00001', '111111')
        LoginAudit loginAudit = newLoginAudit()
        loginAudit.save(failOnError: true, flush: true)
        MockHttpServletRequest request  =  RequestContextHolder.currentRequestAttributes().request
        def ipAddress = loginAuditService.getClientIpAddress(request)
        assertTrue(InetAddressUtils.isIPv4Address(ipAddress) || InetAddressUtils.isIPv6Address(ipAddress))
    }

    @Test
    void testgetValidClientIpAddress() {
        loginSSB('HOSH00001', '111111')
        LoginAudit loginAudit = newLoginAudit()
        loginAudit.save(failOnError: true, flush: true)
        MockHttpServletRequest request  =  RequestContextHolder.currentRequestAttributes().request
        request.addHeader('X-FORWARDED-FOR','2001:db8:85a3:8d3:1319:8a2e:370:7348')
        def ipAddress = loginAuditService.getClientIpAddress(request)
        assertTrue(InetAddressUtils.isIPv4Address(ipAddress) || InetAddressUtils.isIPv6Address(ipAddress))
    }

    @Test
    void testgetInValidClientIpAddress() {
        loginSSB('HOSH00001', '111111')
        LoginAudit loginAudit = newLoginAudit()
        loginAudit.save(failOnError: true, flush: true)
        MockHttpServletRequest request  =  RequestContextHolder.currentRequestAttributes().request
        request.addHeader('X-FORWARDED-FOR','2001:db8:85a3:8d3:1319:8a2e:370:7348:2001:db8:85a3:8d3:1319:8a2e')
        def ipAddress = loginAuditService.getClientIpAddress(request)
        assertTrue(InetAddressUtils.isIPv4Address(ipAddress) || InetAddressUtils.isIPv6Address(ipAddress))
    }

    @Test
    void testAuditIpAddressSetNForIPV4() {
        Holders.config.EnableLoginAudit='Y'
        Holders.config.AuditIPAddress='N'
        loginSSB('HOSH00001', '111111')
        def user = BannerGrantedAuthorityService.getUser()
        GrailsMockHttpServletRequest request = RequestContextHolder?.currentRequestAttributes()?.request
        request.addHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.103 Safari/537.36")
        request.addHeader('X-FORWARDED-FOR','127.0.0.1')
        def  loginAuditObject = loginAuditService.createLoginLogoutAudit(user.username, user.pidm, 'Login Successful')
        assertEquals loginAuditObject.ipAddress , "Not Available"
    }

    @Test
    void testAuditIpAddressSetNForIPV6() {
        Holders.config.EnableLoginAudit='Y'
        Holders.config.AuditIPAddress='N'
        loginSSB('HOSH00001', '111111')
        def user = BannerGrantedAuthorityService.getUser()
        GrailsMockHttpServletRequest request = RequestContextHolder?.currentRequestAttributes()?.request
        request.addHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.103 Safari/537.36")
        request.addHeader('X-FORWARDED-FOR','2001:db8:85a3:8d3:1319:8a2e:370:7348')
        def  loginAuditObject = loginAuditService.createLoginLogoutAudit(user.username, user.pidm, 'Login Successful')
        assertEquals loginAuditObject.ipAddress , "Not Available"
    }


    @Test
    void testAuditIpAddressSetMaskForIPV4() {
        Holders.config.EnableLoginAudit='Y'
        Holders.config.AuditIPAddress='M'
        loginSSB('HOSH00001', '111111')
        def user = BannerGrantedAuthorityService.getUser()
        GrailsMockHttpServletRequest request = RequestContextHolder?.currentRequestAttributes()?.request
        request.addHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.103 Safari/537.36")
        request.addHeader('X-FORWARDED-FOR','127.0.0.1')
        def  loginAuditObject = loginAuditService.createLoginLogoutAudit(user.username, user.pidm, 'Login Successful')
        assertEquals loginAuditObject.ipAddress , "127.0.0.X"
    }

    @Test
    void testAuditIpAddressSetMaskForIPV6() {
        Holders.config.EnableLoginAudit='Y'
        Holders.config.AuditIPAddress='M'
        loginSSB('HOSH00001', '111111')
        def user = BannerGrantedAuthorityService.getUser()
        GrailsMockHttpServletRequest request = RequestContextHolder?.currentRequestAttributes()?.request
        request.addHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.103 Safari/537.36")
        request.addHeader('X-FORWARDED-FOR','2001:db8:85a3:8d3:1319:8a2e:370:7348')
        def  loginAuditObject = loginAuditService.createLoginLogoutAudit(user.username, user.pidm, 'Login Successful')
        assertEquals loginAuditObject.ipAddress , "2001:db8:85a3:8d3:1319:8a2e:370:XXXX"
    }

    @Test
    void testAuditIpAddressSetY() {
        Holders.config.EnableLoginAudit='Y'
        Holders.config.AuditIPAddress='Y'
        loginSSB('HOSH00001', '111111')
        def user = BannerGrantedAuthorityService.getUser()
        GrailsMockHttpServletRequest request = RequestContextHolder?.currentRequestAttributes()?.request
        request.addHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.103 Safari/537.36")
        String ipAddressTest = request.getRemoteAddr()
        def  loginAuditObject = loginAuditService.createLoginLogoutAudit(user.username, user.pidm, 'Login Successful')
        assertEquals loginAuditObject.ipAddress , ipAddressTest
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

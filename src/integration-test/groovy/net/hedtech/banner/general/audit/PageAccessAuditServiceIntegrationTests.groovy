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
import org.apache.http.conn.util.InetAddressUtils
import org.grails.plugins.testing.GrailsMockHttpServletRequest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.context.request.RequestContextHolder

import javax.servlet.ServletRequest

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
         loginSSB('HOSH00001', '111111')
         PageAccessAudit pageAccessAudit = createPageAccessAudit()
         pageAccessAudit.save(failOnError: true, flush: true)
         def  pageAccessAuditObject = pageAccessAuditService.getDataByLoginID(pageAccessAudit.loginId)
         assertEquals pageAccessAuditObject.loginId , pageAccessAudit.loginId
    }


    @Test
    void testCreatePageAudit(){
        loginSSB('HOSH00001', '111111')
        RequestContextHolder?.currentRequestAttributes()?.request?.setRequestURI('/ssb/home')
        def  pageAccessAuditObject = pageAccessAuditService.createPageAudit()
        assertNotNull  pageAccessAuditObject
    }


    @Test
    void testCheckAndCreatePageAuditWithURLPattern(){
        loginSSB('HOSH00001', '111111')
        Holders.config.EnablePageAudit= 'homepage'
        RequestContextHolder?.currentRequestAttributes()?.request?.setRequestURI('/ssb/homepage')
        def  pageAccessAuditObject = pageAccessAuditService.checkAndCreatePageAudit()
        assertNotNull  pageAccessAuditObject
        assertEquals(pageAccessAuditObject.pageUrl, "/ssb/homepage")

        Holders.config.EnablePageAudit= '%home'
        RequestContextHolder?.currentRequestAttributes()?.request?.setRequestURI('/ssb/homepage')
        def  pageAccessAuditObject2 = pageAccessAuditService.checkAndCreatePageAudit()
        assertNotNull  pageAccessAuditObject2
        assertEquals(pageAccessAuditObject2.pageUrl, "/ssb/homepage")

        Holders.config.EnablePageAudit= 'home%'
        RequestContextHolder?.currentRequestAttributes()?.request?.setRequestURI('/ssb/homepage')
        def  pageAccessAuditObject3 = pageAccessAuditService.checkAndCreatePageAudit()
        assertNotNull  pageAccessAuditObject3
        assertEquals(pageAccessAuditObject3.pageUrl, "/ssb/homepage")
    }

    @Test
    void testCheckAndCreatePageAuditMultipleWithURLPattern(){
        loginSSB('HOSH00001', '111111')
        Holders.config.EnablePageAudit= 'homepage, platformutilities'
        RequestContextHolder?.currentRequestAttributes()?.request?.setRequestURI('/ssb/homepage')
        def  pageAccessAuditObject = pageAccessAuditService.checkAndCreatePageAudit()
        assertNotNull  pageAccessAuditObject
        assertEquals(pageAccessAuditObject.pageUrl, "/ssb/homepage")

        RequestContextHolder?.currentRequestAttributes()?.request?.setRequestURI('/ssb/uiCatalog/platformUtilities')
        def  pageAccessAuditObject2 = pageAccessAuditService.checkAndCreatePageAudit()
        assertNotNull  pageAccessAuditObject2
        assertEquals(pageAccessAuditObject2.pageUrl, "/ssb/uiCatalog/platformUtilities")

        Holders.config.EnablePageAudit= '%home, %utilities'
        RequestContextHolder?.currentRequestAttributes()?.request?.setRequestURI('/ssb/homepage')
        def  pageAccessAuditObject3 = pageAccessAuditService.checkAndCreatePageAudit()
        assertNotNull  pageAccessAuditObject3
        assertEquals(pageAccessAuditObject3.pageUrl, "/ssb/homepage")
        RequestContextHolder?.currentRequestAttributes()?.request?.setRequestURI('/ssb/uiCatalog/platformUtilities')
        def  pageAccessAuditObject4 = pageAccessAuditService.checkAndCreatePageAudit()
        assertNotNull  pageAccessAuditObject4
        assertEquals(pageAccessAuditObject4.pageUrl, "/ssb/uiCatalog/platformUtilities")

        Holders.config.EnablePageAudit= 'home%, platform%'
        RequestContextHolder?.currentRequestAttributes()?.request?.setRequestURI('/ssb/homepage')
        def  pageAccessAuditObject5 = pageAccessAuditService.checkAndCreatePageAudit()
        assertNotNull  pageAccessAuditObject5
        assertEquals(pageAccessAuditObject5.pageUrl, "/ssb/homepage")
        RequestContextHolder?.currentRequestAttributes()?.request?.setRequestURI('/ssb/uiCatalog/platformUtilities')
        def  pageAccessAuditObject6 = pageAccessAuditService.checkAndCreatePageAudit()
        assertNotNull  pageAccessAuditObject6
        assertEquals(pageAccessAuditObject6.pageUrl, "/ssb/uiCatalog/platformUtilities")

        Holders.config.EnablePageAudit= '%, platform%'
        RequestContextHolder?.currentRequestAttributes()?.request?.setRequestURI('/ssb/homepage')
        def  pageAccessAuditObject7 = pageAccessAuditService.checkAndCreatePageAudit()
        assertNotNull  pageAccessAuditObject7
        assertEquals(pageAccessAuditObject7.pageUrl, "/ssb/homepage")
        RequestContextHolder?.currentRequestAttributes()?.request?.setRequestURI('/ssb/uiCatalog/platformUtilities')
        def  pageAccessAuditObject8 = pageAccessAuditService.checkAndCreatePageAudit()
        assertNotNull  pageAccessAuditObject8
        assertEquals(pageAccessAuditObject8.pageUrl, "/ssb/uiCatalog/platformUtilities")
    }

    @Test
    void testCheckEnablePageAuditWithFailureFlow(){
        loginSSB('HOSH00001', '111111')
        Holders.config.EnablePageAudit = 'N'
        PageAccessAudit pageAccessAudit = pageAccessAuditService.checkAndCreatePageAudit()
        assertNull pageAccessAudit

        Holders.config.EnablePageAudit = 'n'
        pageAccessAudit = pageAccessAuditService.checkAndCreatePageAudit()
        assertNull pageAccessAudit

        Holders.config.EnablePageAudit = null
        pageAccessAudit = pageAccessAuditService.checkAndCreatePageAudit()
        assertNull pageAccessAudit

        Holders.config.EnablePageAudit= 'homepage'
        RequestContextHolder?.currentRequestAttributes()?.request?.setRequestURI('/ssb/dummy')
        def  pageAccessAuditObject1 = pageAccessAuditService.checkAndCreatePageAudit()
        assertNull pageAccessAuditObject1
    }

    @Test
    void testCheckWithSecureParameter(){
        loginSSB('HOSH00001', '111111')

        Holders.config.EnablePageAudit= '%'
        GrailsMockHttpServletRequest request = RequestContextHolder?.currentRequestAttributes()?.request
        request.setRequestURI('/ssb/home?username=HOSH00001&password=111111')
        request.setQueryString('username=HOSH00001&password=111111')
        //RequestContextHolder?.currentRequestAttributes()?.request?.setRequestU('/ssb/home?username=HOSH00001&password=111111')
        def  pageAccessAuditObject1 = pageAccessAuditService.checkAndCreatePageAudit()
        assertNotNull pageAccessAuditObject1
    }

    @Test
    void testGetPageAuditConfiguration() {
        Holders.config.EnablePageAudit='N'
        String  pageAuditConfiguration = pageAccessAuditService.getPageAuditConfiguration()
        assertEquals pageAuditConfiguration , 'n'

        Holders.config.EnablePageAudit='%'
        String  pageAuditConfiguration1 = pageAccessAuditService.getPageAuditConfiguration()
        assertEquals pageAuditConfiguration1 , '%'

        Holders.config.EnablePageAudit='RandomValue'
        String  pageAuditConfiguration2 = pageAccessAuditService.getPageAuditConfiguration()
        assertEquals pageAuditConfiguration2 , 'randomvalue'

        Holders.config.EnablePageAudit=''
        String  pageAuditConfiguration3 = pageAccessAuditService.getPageAuditConfiguration()
        assertEquals pageAuditConfiguration3 , 'n'
    }
    
    @Test
    void testgetloadBalancerIpAddress() {
        loginSSB('HOSH00001', '111111')
        PageAccessAudit pageAccessAudit = createPageAccessAudit()
        pageAccessAudit.save(failOnError: true, flush: true)
        MockHttpServletRequest request  =  RequestContextHolder.currentRequestAttributes().request
        String ipAddress = pageAccessAuditService.getClientIpAddress(request)
        assertTrue(InetAddressUtils.isIPv4Address(ipAddress) || InetAddressUtils.isIPv6Address(ipAddress))
    }

    @Test
    void testgetValidClientIpAddress() {
        loginSSB('HOSH00001', '111111')
        PageAccessAudit pageAccessAudit = createPageAccessAudit()
        pageAccessAudit.save(failOnError: true, flush: true)
        MockHttpServletRequest request  =  RequestContextHolder.currentRequestAttributes().request
        request.addHeader('X-FORWARDED-FOR','2001:db8:85a3:8d3:1319:8a2e:370:7348')
        String ipAddress = pageAccessAuditService.getClientIpAddress(request)
        assertTrue(InetAddressUtils.isIPv4Address(ipAddress) || InetAddressUtils.isIPv6Address(ipAddress))
    }

    @Test
    void testgetInValidClientIpAddress() {
        loginSSB('HOSH00001', '111111')
        PageAccessAudit pageAccessAudit = createPageAccessAudit()
        pageAccessAudit.save(failOnError: true, flush: true)
        MockHttpServletRequest request  =  RequestContextHolder.currentRequestAttributes().request
        request.addHeader('X-FORWARDED-FOR','2001:db8:85a3:8d3:1319:8a2e:370:7348:2001:db8:85a3:8d3:1319:8a2e')
        String ipAddress = pageAccessAuditService.getClientIpAddress(request)
        assertTrue(InetAddressUtils.isIPv4Address(ipAddress) || InetAddressUtils.isIPv6Address(ipAddress))
    }

    @Test
    void testAuditIpAddressSetNForIPV4() {
        Holders.config.EnablePageAudit='%'
        Holders.config.AuditIPAddress='N'
        loginSSB('HOSH00001', '111111')
        GrailsMockHttpServletRequest request = RequestContextHolder?.currentRequestAttributes()?.request
        request.setRequestURI('/ssb/home?username=HOSH00001&password=111111')
        request.addHeader('X-FORWARDED-FOR','127.0.0.1')
        PageAccessAudit pageAccessAudit = pageAccessAuditService.checkAndCreatePageAudit()
        assertEquals pageAccessAudit.ipAddress , "NA"
    }

    @Test
    void testAuditIpAddressSetNForIPV6() {
        Holders.config.EnablePageAudit='%'
        Holders.config.AuditIPAddress='N'
        loginSSB('HOSH00001', '111111')
        GrailsMockHttpServletRequest request = RequestContextHolder?.currentRequestAttributes()?.request
        request.setRequestURI('/ssb/home?username=HOSH00001&password=111111')
        request.addHeader('X-FORWARDED-FOR','2001:db8:85a3:8d3:1319:8a2e:370:7348')
        PageAccessAudit pageAccessAudit = pageAccessAuditService.checkAndCreatePageAudit()
        assertEquals pageAccessAudit.ipAddress , "NA"
    }

    @Test
    void testAuditIpAddressSetMaskForIPV4() {
        Holders.config.EnablePageAudit='%'
        Holders.config.AuditIPAddress='MASK'
        loginSSB('HOSH00001', '111111')
        GrailsMockHttpServletRequest request = RequestContextHolder?.currentRequestAttributes()?.request
        request.setRequestURI('/ssb/home?username=HOSH00001&password=111111')
        request.addHeader('X-FORWARDED-FOR','127.0.0.1')
        PageAccessAudit pageAccessAudit = pageAccessAuditService.checkAndCreatePageAudit()
        assertEquals pageAccessAudit.ipAddress , "127.0.0.X"
    }

    @Test
    void testAuditIpAddressSetMaskForIPV6() {
        Holders.config.EnablePageAudit='%'
        Holders.config.AuditIPAddress='MASK'
        loginSSB('HOSH00001', '111111')
        GrailsMockHttpServletRequest request = RequestContextHolder?.currentRequestAttributes()?.request
        request.setRequestURI('/ssb/home?username=HOSH00001&password=111111')
        request.addHeader('X-FORWARDED-FOR','2001:db8:85a3:8d3:1319:8a2e:370:7348')
        PageAccessAudit pageAccessAudit = pageAccessAuditService.checkAndCreatePageAudit()
        assertEquals pageAccessAudit.ipAddress , "2001:db8:85a3:8d3:1319:8a2e:370:XXXX"
    }
    @Test
    void testAuditIpAddressSetY() {
        Holders.config.EnablePageAudit='%'
        Holders.config.AuditIPAddress='Y'
        loginSSB('HOSH00001', '111111')
        GrailsMockHttpServletRequest request = RequestContextHolder?.currentRequestAttributes()?.request
        request.setRequestURI('/ssb/home?username=HOSH00001&password=111111')
        String ipAddressTest = request.getRemoteAddr()
        PageAccessAudit pageAccessAudit = pageAccessAuditService.checkAndCreatePageAudit()
        assertEquals pageAccessAudit.ipAddress , ipAddressTest
    }

    private static PageAccessAudit createPageAccessAudit() {
        def user = BannerGrantedAuthorityService.getUser()
        PageAccessAudit pageAccessAudit = new PageAccessAudit(
                auditTime: new Date(),
                loginId: "TestLogin",
                pidm: 123,
                appId: Holders.config.app.appId,
                pageUrl: "/testPageid/",
                ipAddress: InetAddress.getLocalHost().getHostAddress()
        )
        return pageAccessAudit
    }
}

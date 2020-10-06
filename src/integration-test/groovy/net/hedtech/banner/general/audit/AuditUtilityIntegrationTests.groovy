/*******************************************************************************
 Copyright 2020 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.audit

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import grails.util.Holders
import grails.web.http.HttpHeaders
import net.hedtech.banner.audit.AuditUtility
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.apache.http.conn.util.InetAddressUtils
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.context.request.RequestContextHolder

@Integration
@Rollback
class AuditUtilityIntegrationTests extends BaseIntegrationTestCase {

    def defaultConfig

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
        defaultConfig = Holders.config
    }

    @After
    public void tearDown() {
        super.tearDown()
        Holders.config = defaultConfig
    }

    @Test
    void testGetClientIpAddressWithRequestAsOnlyParameter() {
        MockHttpServletRequest request  =  RequestContextHolder.currentRequestAttributes().request
        def ipAddress = AuditUtility.getClientIpAddress(request)
        assertTrue(InetAddressUtils.isIPv4Address(ipAddress) || InetAddressUtils.isIPv6Address(ipAddress))
    }

    @Test
    void testgetAuditIpAddressConfigurationWithValueAsN() {
        Holders.config.AuditIPAddress = 'N'
        def AuditIPAddressConfig = AuditUtility.getAuditIpAddressConfiguration()
        assertEquals AuditIPAddressConfig , "n"
    }

    @Test
    void testgetAuditIpAddressConfigurationWithValueAsY() {
        Holders.config.AuditIPAddress = 'Y'
        def AuditIPAddressConfig = AuditUtility.getAuditIpAddressConfiguration()
        assertEquals AuditIPAddressConfig , "y"
    }

    @Test
    void testgetAuditIpAddressConfigurationWithValueAsM() {
        Holders.config.AuditIPAddress = 'M'
        def AuditIPAddressConfig = AuditUtility.getAuditIpAddressConfiguration()
        assertEquals AuditIPAddressConfig , "m"
    }

    @Test
    void testgetMaskedIpAddressForIPV4() {
        MockHttpServletRequest request = RequestContextHolder?.currentRequestAttributes()?.request
        request.addHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.103 Safari/537.36")
        request.addHeader('X-FORWARDED-FOR','127.0.0.1')
        def ipAddress = AuditUtility.getMaskedIpAddress(AuditUtility.getClientIpAddress(request))
        assertEquals ipAddress , "127.0.0.X"
    }

    @Test
    void testgetMaskedIpAddressForIPV6() {
        MockHttpServletRequest request = RequestContextHolder?.currentRequestAttributes()?.request
        request.addHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.103 Safari/537.36")
        request.addHeader('X-FORWARDED-FOR','2001:db8:85a3:8d3:1319:8a2e:370:7348')
        def ipAddress = AuditUtility.getMaskedIpAddress(AuditUtility.getClientIpAddress(request))
        assertEquals ipAddress , "2001:db8:85a3:XXX:XXXX:XXXX:XXX:XXXX"
    }
}

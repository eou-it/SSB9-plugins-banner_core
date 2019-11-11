/*******************************************************************************
Copyright 2019 Ellucian Company L.P. and its affiliates.
*******************************************************************************/
package net.hedtech.banner.general.audit

import grails.gorm.transactions.Transactional
import grails.util.Holders
import net.hedtech.banner.audit.AuditUtility
import net.hedtech.banner.service.ServiceBase
import org.springframework.web.context.request.RequestContextHolder
import javax.servlet.http.HttpServletRequest
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Transactional
class LoginAuditService extends ServiceBase {

    public def createLoginLogoutAudit(username, userpidm, comment) {
        try {
            log.debug "In LoginAuditService createLoginLogoutAudit "
            String appId = Holders.config.app.appId
            String loginId = username ?: 'ANONYMOUS'
            HttpServletRequest request = RequestContextHolder.getRequestAttributes()?.request
            String ipAddress = AuditUtility.getClientIpAddress(request, LoginAudit.getConstrainedProperties().get('ipAddress').getMaxSize())
            String userAgent = request.getHeader("User-Agent")

            LoginAudit loginAudit = new LoginAudit()
            loginAudit.setAppId(appId)
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy hh.mm.ss.SS a")
            String utcTime = ZonedDateTime.now(ZoneId.of("UTC")).format(DateTimeFormatter.ofPattern('dd-MMM-yy hh.mm.ss.SS a'))
            Date auditTime = sdf.parse(utcTime)
            loginAudit.setAuditTime(auditTime)
            loginAudit.setLoginId(loginId)
            String ipAddressConfiguration = AuditUtility.getAuditIpAddressConfiguration()
            if (ipAddressConfiguration == 'y') {
                loginAudit.setIpAddress(ipAddress)
            } else if (ipAddressConfiguration == 'm') {
                loginAudit.setIpAddress(AuditUtility.getMaskedIpAddress(ipAddress))
            } else {
                loginAudit.setIpAddress("Not Available")
            }
            loginAudit.setUserAgent(userAgent)
            loginAudit.setLastModifiedBy(loginId)
            loginAudit.setPidm(userpidm as Integer)
            loginAudit.setVersion(0L)
            loginAudit.setLogonComment(comment)
            println " new object ${loginAudit}"
            this.create(loginAudit)
        } catch (Exception ex) {
            log.error("Exception occured while creating loginAudit ${ex.getMessage()}")
        }
    }

    public List getDataByLoginID(loginId) {
        List loginAuditPage = LoginAudit.fetchByLoginId(loginId)
        loginAuditPage
    }
}


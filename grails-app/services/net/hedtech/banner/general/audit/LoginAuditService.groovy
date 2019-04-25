/*******************************************************************************
Copyright 2009-2019 Ellucian Company L.P. and its affiliates.
*******************************************************************************/
package net.hedtech.banner.general.audit


import grails.util.Holders
import net.hedtech.banner.service.ServiceBase
import org.springframework.web.context.request.RequestContextHolder
import javax.servlet.http.HttpServletRequest

public class LoginAuditService extends ServiceBase{

    public def createLoginLogoutAudit(authenticationResults, comment) {
        try {
            log.debug "In LoginAuditService createLoginLogoutAudit "
            String appId = Holders.config.app.appId
            String loginId =  authenticationResults.username ? authenticationResults.username : authenticationResults.name ? authenticationResults.name : 'ANONYMOUS'
            HttpServletRequest request = RequestContextHolder.getRequestAttributes()?.request
            String ipAddress = request.getRemoteAddr()
            String userAgent = request.getHeader("User-Agent")
            Integer pidm = authenticationResults.pidm

            LoginAudit loginAudit = new LoginAudit()
            loginAudit.setAppId(appId)
            loginAudit.setAuditTime(new Date())
            loginAudit.setLoginId(loginId)
            loginAudit.setIpAddress(ipAddress)
            loginAudit.setUserAgent(userAgent)
            loginAudit.setLastModifiedBy(loginId)
            loginAudit.setPidm(pidm)
            loginAudit.setVersion(0L)
            loginAudit.setLogonComment(comment)
            this.create(loginAudit)
        }catch (Exception ex) {
            log.error("Exception occured while creating loginAudit ${ex.getMessage()}")
        }
    }

    public def getDataByLoginID(loginId) {
        LoginAudit loginAuditPage = LoginAudit.fetchByLoginId(loginId)
        return loginAuditPage
    }
}


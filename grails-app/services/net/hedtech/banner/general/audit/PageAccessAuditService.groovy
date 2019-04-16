/*******************************************************************************
 Copyright 2019 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.audit

import grails.util.Holders
import net.hedtech.banner.security.BannerGrantedAuthorityService
import net.hedtech.banner.service.ServiceBase
import grails.gorm.transactions.Transactional
import org.springframework.web.context.request.RequestContextHolder

@Transactional
class PageAccessAuditService extends ServiceBase {

    private String auditTime
    private String appId
    private String loginId
    private String pageUrl
    private Integer pidm
    private String ipAddress

    public def createPageAudit() {
        try {
            def user = BannerGrantedAuthorityService.getUser()
            if (user.hasProperty('pidm')) {
                pidm = user?.pidm
            }
            auditTime = new Date()
            loginId = user?.username
            def request = RequestContextHolder.getRequestAttributes()?.request
            ipAddress = request.getRemoteAddr() // returns 0:0:0:0:0:0:0:1 if executed from localhost
            appId = Holders.config.app.appId
            pageUrl = request.getRequestURI()
            PageAccessAudit pageAccessAudit = new PageAccessAudit()
            pageAccessAudit.setAuditTime(new Date())
            pageAccessAudit.setLoginId(loginId)
            pageAccessAudit.setPidm(pidm)
            pageAccessAudit.setAppId(appId)
            pageAccessAudit.setPageUrl(pageUrl)
            pageAccessAudit.setIpAddress(ipAddress)
            pageAccessAudit.setVersion(0L)
            this.create(pageAccessAudit)
        }catch (Exception ex) {
            log.error("Exception occured while creating PageAccess Audit ${ex.getMessage()}")
        }
    }


    public def getDataByLoginID(getDataByLoginID) {
        PageAccessAudit selfServicePage = PageAccessAudit.fetchByLoginId(getDataByLoginID)
        return selfServicePage;
    }

}

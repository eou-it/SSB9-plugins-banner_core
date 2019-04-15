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
            pidm = user.pidm
            auditTime = new Date()
            loginId = user.username
            def request = RequestContextHolder.getRequestAttributes()?.request
            ipAddress = request.getRemoteAddr()
            println request.getRequestURI()
            //ipAddress = InetAddress.getLocalHost().getHostAddress()
            appId = Holders.config.app.appId
            pageUrl = "test Pageid"

            PageAccessAudit pageAccessAudit = new PageAccessAudit()
            pageAccessAudit.setAuditTime(new Date())
            pageAccessAudit.setLoginId(loginId)
            pageAccessAudit.setPidm(pidm)
            pageAccessAudit.setAppId(appId)
            pageAccessAudit.setPageUrl(pageUrl)
            pageAccessAudit.setIpAddress(ipAddress)
            this.create(pageAccessAudit)
        }catch (Exception ex) {
            log.error("Exception occured while creating PageAudit " + ex.getMessage())
        }
    }


    public def getDataByLoginID(getDataByLoginID) {
        PageAccessAudit selfServicePage = PageAccessAudit.fetchByLoginId(getDataByLoginID)
        return selfServicePage;
    }

}

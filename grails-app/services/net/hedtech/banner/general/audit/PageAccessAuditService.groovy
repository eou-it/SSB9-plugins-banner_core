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

    def springSecurityService

    PageAccessAudit checkAndCreatePageAudit(){
        PageAccessAudit pageAccessAudit
        try {
            def request = RequestContextHolder.getRequestAttributes()?.request
            String enablePageAudit = Holders.config.EnablePageAudit instanceof String ? (Holders.config.EnablePageAudit).toLowerCase() : 'N'
            String pageUrl = (request.getRequestURI())?.toLowerCase()
            if (enablePageAudit?.toLowerCase() != 'N' && (enablePageAudit == '%' || pageUrl?.contains(enablePageAudit))) {
                pageAccessAudit = createPageAudit() as PageAccessAudit
            }
        }
        catch (ex){
            log.error("Exception occured while executing pageAccessAudit " + ex.getMessage())
        }
        pageAccessAudit
    }

    def createPageAudit() {
        try {
            String loginId
            Integer pidm
            def user = BannerGrantedAuthorityService.getUser()
            def userLoginId
            if (springSecurityService.isLoggedIn()){
                if (user.hasProperty('pidm')) {
                    pidm = user?.pidm
                }
                userLoginId = user?.username
            }
            loginId = userLoginId?:'ANONYMOUS'
            def request = RequestContextHolder.getRequestAttributes()?.request
            String ipAddress = request.getRemoteAddr() // returns 0:0:0:0:0:0:0:1 if executed from localhost
            String appId = 'PSA'
            String pageUrl = request.getRequestURI()
            PageAccessAudit pageAccessAudit = new PageAccessAudit()
            pageAccessAudit.setAuditTime(new Date())
            pageAccessAudit.setLoginId(loginId)
            pageAccessAudit.setPidm(pidm)
            pageAccessAudit.setAppId(appId)
            pageAccessAudit.setPageUrl(pageUrl)
            pageAccessAudit.setIpAddress(ipAddress)
            pageAccessAudit.setLastModifiedBy('BANNER')
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

/*******************************************************************************
 Copyright 2019 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.audit

import grails.plugin.springsecurity.SpringSecurityService
import grails.util.Holders
import net.hedtech.banner.audit.AuditUtility
import net.hedtech.banner.security.BannerGrantedAuthorityService
import net.hedtech.banner.service.ServiceBase
import grails.gorm.transactions.Transactional
import javax.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.context.request.RequestContextHolder

import java.text.SimpleDateFormat

@Transactional
class PageAccessAuditService extends ServiceBase {

    @Autowired
    SpringSecurityService springSecurityService


    PageAccessAudit checkAndCreatePageAudit() {
        PageAccessAudit pageAccessAudit = null
        try {
            def request = RequestContextHolder.getRequestAttributes()?.request
            List<String> pageAuditConfigList
            String requestedPageUrl = (request?.getForwardURI())?.toLowerCase()
            String pageAuditConfiguration = getPageAuditConfiguration()
            if (pageAuditConfiguration.toLowerCase() != 'n'){
                pageAuditConfigList = pageAuditConfiguration.split("\\s*,\\s*") as ArrayList<String>
                if (isPageAuditConfigAvailableInRequestPageUrl(pageAuditConfigList, requestedPageUrl)) {
                    pageAccessAudit = createPageAudit() as PageAccessAudit
                }
            }
        }
        catch (ex) {
            log.error("Exception occurred while executing pageAccessAudit " + ex.getMessage())
        }
        return pageAccessAudit
    }


    def createPageAudit() {
        try {
            String loginId
            Integer pidm
            def user = BannerGrantedAuthorityService.getUser()
            String userLoginId = null
            if (springSecurityService.isLoggedIn()) {
                if (user.hasProperty('pidm')) {
                    pidm = user?.pidm
                }
                userLoginId = user?.username
            }
            loginId = userLoginId ?: 'ANONYMOUS'
            HttpServletRequest request = RequestContextHolder.getRequestAttributes()?.request
            String ipAddress = AuditUtility.getClientIpAddress(request,PageAccessAudit.getConstrainedProperties().get('ipAddress').getMaxSize())
            String appId = Holders.config.app.appId
            String requestURI = request?.getForwardURI()

            String queryString = null
            def unsecureQueryParameter = getUnsecureQueryParameter(request.getParameterMap())
            if (!unsecureQueryParameter) {
                queryString = request.getQueryString()
            }
            String pageUrl = queryString ? "${requestURI}?${queryString}" : requestURI
            PageAccessAudit pageAccessAudit = new PageAccessAudit()
            SimpleDateFormat sdf1 = new SimpleDateFormat("dd-MMM-yy hh.mm.ss.SS a")
            sdf1.setTimeZone(TimeZone.getTimeZone("UTC"))
            SimpleDateFormat sdf2 = new SimpleDateFormat("dd-MMM-yy hh.mm.ss.SS a");
            def d1  = sdf1.format(new Date())
            Date auditTime = sdf2.parse(d1, new java.text.ParsePosition(0))
            pageAccessAudit.setAuditTime(auditTime)
            pageAccessAudit.setLoginId(loginId)
            pageAccessAudit.setPidm(pidm)
            pageAccessAudit.setAppId(appId)
            pageAccessAudit.setPageUrl(pageUrl)
            String ipAddressConfiguration = AuditUtility.getAuditIpAddressConfiguration()
            if (ipAddressConfiguration == 'y') {
                pageAccessAudit.setIpAddress(ipAddress)
            } else if (ipAddressConfiguration == 'm') {
                pageAccessAudit.setIpAddress(AuditUtility.getMaskedIpAddress(ipAddress))
            } else {
                pageAccessAudit.setIpAddress("Not Available")
            }
            pageAccessAudit.setLastModifiedBy('BANNER')
            pageAccessAudit.setVersion(0L)
            this.create(pageAccessAudit)
        } catch (Exception ex) {
            log.error("Exception occured while creating PageAccess Audit ${ex.getMessage()}")
        }
    }


    public def getDataByLoginID(getDataByLoginID) {
        PageAccessAudit selfServicePage = PageAccessAudit.fetchByLoginId(getDataByLoginID)
        return selfServicePage
    }


    private static String getPageAuditConfiguration() {
        String pageAuditConfiguration = (Holders.config.EnablePageAudit instanceof String && Holders.config.EnablePageAudit.size() > 0) ? (Holders.config.EnablePageAudit).toLowerCase() : 'n'
        return pageAuditConfiguration
    }

    private static def getUnsecureQueryParameter(Map parameterMap) {
        def unsecureQueryParameter = parameterMap.find { it ->
            it.key?.equalsIgnoreCase('username') || it.key?.equalsIgnoreCase('password')
        }
        return unsecureQueryParameter
    }

    private Boolean isPageAuditConfigAvailableInRequestPageUrl(List<String> pageAuditConfigList, String requestedPageUrl) {
        Boolean isPageAuditConfigAvailable = false
        if (pageAuditConfigList.find { it == '%' }?.length() > 0) {
            isPageAuditConfigAvailable = true
        } else {
            for (String pageAuditConfiguration : pageAuditConfigList) {
                if (requestedPageUrl?.contains(pageAuditConfiguration.replaceAll('%', ''))) {
                    isPageAuditConfigAvailable = true
                    break
                }
            }
        }
        return isPageAuditConfigAvailable
    }

}



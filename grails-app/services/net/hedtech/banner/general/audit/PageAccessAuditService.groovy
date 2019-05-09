/*******************************************************************************
 Copyright 2019 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.audit

import grails.plugin.springsecurity.SpringSecurityService
import grails.util.Holders
import net.hedtech.banner.security.BannerGrantedAuthorityService
import net.hedtech.banner.service.ServiceBase
import grails.gorm.transactions.Transactional
import javax.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.context.request.RequestContextHolder

import javax.servlet.ServletRequest

@Transactional
class PageAccessAuditService extends ServiceBase {

    @Autowired
    SpringSecurityService springSecurityService


    PageAccessAudit checkAndCreatePageAudit(){
        PageAccessAudit pageAccessAudit = null
        try {
            def request = RequestContextHolder.getRequestAttributes()?.request
            List<String> pageAuditConfigList =new ArrayList(Arrays.asList(getPageAuditConfiguration().split("\\s*,\\s*")));
            String requestedPageUrl = (request.getRequestURI())?.toLowerCase()
            if (pageAuditConfigList.size() == 1 && pageAuditConfigList[0].length() == 1 && pageAuditConfigList[0]?.toLowerCase() != 'n' && pageAuditConfigList[0] == '%') {
                pageAccessAudit = createPageAudit() as PageAccessAudit
            }else if (isPageAuditConfigAvailableInRequestPageUrl(pageAuditConfigList,requestedPageUrl)){
                pageAccessAudit = createPageAudit() as PageAccessAudit
            }
        }
        catch (ex){
            log.error("Exception occurred while executing pageAccessAudit " + ex.getMessage())
        }
        return pageAccessAudit
    }


    def createPageAudit() {
        try {
            String loginId
            Integer pidm
            def user = BannerGrantedAuthorityService.getUser()
            String userLoginId =  null
            if (springSecurityService.isLoggedIn()){
                if (user.hasProperty('pidm')) {
                    pidm = user?.pidm
                }
                userLoginId = user?.username
            }
            loginId = userLoginId?:'ANONYMOUS'
            HttpServletRequest request = RequestContextHolder.getRequestAttributes()?.request
            String ipAddress = request.getRemoteAddr() // returns 0:0:0:0:0:0:0:1 if executed from localhost
            String appId = Holders.config.app.appId
            String requestURI = request.getRequestURI()
            String queryString = null
            def unsecureQueryParameter = getUnsecureQueryParameter(request.getParameterMap())
            if(!unsecureQueryParameter){
                queryString = request.getQueryString()
            }
            String pageUrl = queryString ? "${requestURI}?${queryString}" : requestURI
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


    private static String getPageAuditConfiguration(){
        String pageAuditConfiguration = (Holders.config.EnablePageAudit instanceof String && Holders.config.EnablePageAudit.size() > 0)  ? (Holders.config.EnablePageAudit).toLowerCase() : 'n'
        return pageAuditConfiguration
    }

    private static def getUnsecureQueryParameter(Map parameterMap){
        def unsecureQueryParameter = parameterMap.find{it ->
            it.key?.equalsIgnoreCase('username') || it.key?.equalsIgnoreCase('password')
        }
        return unsecureQueryParameter
    }

    private boolean isPageAuditConfigAvailableInRequestPageUrl(List<String> pageAuditConfigList, String requestedPageUrl){
        def isPageAuditConfigAvailable = false
        for (String pageAuditConfiguration: pageAuditConfigList){
            if(requestedPageUrl?.contains(pageAuditConfiguration.replaceAll('%',''))){
                isPageAuditConfigAvailable = true
                break
            }
        }
        return isPageAuditConfigAvailable
    }
}


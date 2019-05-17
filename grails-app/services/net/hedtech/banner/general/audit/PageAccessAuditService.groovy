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
            List<String> pageAuditConfigList =getPageAuditConfiguration().split("\\s*,\\s*") as ArrayList<String>
            String requestedPageUrl = (request.getRequestURI())?.toLowerCase()
            if (isPageAuditConfigAvailableInRequestPageUrl(pageAuditConfigList,requestedPageUrl)){
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
            //String ipAddress = request.getRemoteAddr() // returns 0:0:0:0:0:0:0:1 if executed from localhost
            String ipAddress = getClientIpAdress(request);
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

    private Boolean isPageAuditConfigAvailableInRequestPageUrl(List<String> pageAuditConfigList, String requestedPageUrl){
        Boolean isPageAuditConfigAvailable = false
        if (pageAuditConfigList.find{it == '%'}?.length()>0){
            isPageAuditConfigAvailable = true
        }else{
            for (String pageAuditConfiguration: pageAuditConfigList){
                if(requestedPageUrl?.contains(pageAuditConfiguration.replaceAll('%',''))){
                    isPageAuditConfigAvailable = true
                    break
                }
            }
        }
        return isPageAuditConfigAvailable
    }

    private static String getClientIpAddress(request){
        String ipAddressList = request.getHeader("X-FORWARDED-FOR");
        //String ipAddressList = "2001:db8:85a3:8d3:1319:8a2e, 70.41.3.18, 150.172.238.178"
        String clientIpAddress
        if (ipAddressList?.length() > 0)  {
            String ipAddress = ipAddressList.split(",")[0];
            if ( ipAddress.length() <= PageAccessAudit.getConstrainedProperties().get('ipAddress').getMaxSize() ) {
                clientIpAddress =  ipAddress
            } else {
                clientIpAddress = request.getRemoteAddr();
                log.error("Exception occured while getting clientIpAddress:Ip Address too long.")
            }
        }else{
            clientIpAddress = request.getRemoteAddr();
        }
        return clientIpAddress;
    }
}


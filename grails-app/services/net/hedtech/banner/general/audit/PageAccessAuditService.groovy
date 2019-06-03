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
            String requestedPageUrl = (request?.getForwardURI())?.toLowerCase()
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
            String ipAddress = getClientIpAddress(request);
            String appId = Holders.config.app.appId
            String requestURI = request?.getForwardURI()

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
            if(getAuditIpAddressConfigration()=='y'){
                pageAccessAudit.setIpAddress(ipAddress)
            }
            else if(getAuditIpAddressConfigration()=='mask'){
                pageAccessAudit.setIpAddress(getMaskedIpAddress(ipAddress))
            }
            else {
                pageAccessAudit.setIpAddress("Not Available")
            }
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

    public String getAuditIpAddressConfigration() {
        String auditIpAddressConfiguration = (Holders.config.AuditIPAddress instanceof String && Holders.config.AuditIPAddress.size() > 0) ? (Holders.config.AuditIPAddress).toLowerCase() : 'n'
        return auditIpAddressConfiguration
    }

    public String getMaskedIpAddress(String ipAddress) {
            String maskedIpAddress
            String Ipv6orIpv4Separator = ipAddress.contains(':')? ":" : "."
            int LastIndexOfIpv6orIpv4Separator= ipAddress.lastIndexOf(Ipv6orIpv4Separator)
            maskedIpAddress = ipAddress.substring(0, LastIndexOfIpv6orIpv4Separator + 1) + appendX(ipAddress,LastIndexOfIpv6orIpv4Separator)
            return maskedIpAddress
    }

    public String appendX(String ipAddress,int lastIndexOfCh) {
        String X=""
        int StartMasking = ipAddress.substring(lastIndexOfCh+1).length()
        for (int i = 0; i < StartMasking; i++) {
            X+="X"
        }
        return X
    }
}


/*******************************************************************************
Copyright 2019 Ellucian Company L.P. and its affiliates.
*******************************************************************************/
package net.hedtech.banner.general.audit

import grails.gorm.transactions.Transactional
import grails.util.Holders
import net.hedtech.banner.service.ServiceBase
import org.springframework.web.context.request.RequestContextHolder
import javax.servlet.http.HttpServletRequest

@Transactional
class LoginAuditService extends ServiceBase{

    public def createLoginLogoutAudit(username, userpidm, comment) {
        try {
            log.debug "In LoginAuditService createLoginLogoutAudit "
            String appId = Holders.config.app.appId
            String loginId =  username?: 'ANONYMOUS'
            HttpServletRequest request = RequestContextHolder.getRequestAttributes()?.request
            String ipAddress = getClientIpAddress(request);
            String userAgent = request.getHeader("User-Agent")

            LoginAudit loginAudit = new LoginAudit()
            loginAudit.setAppId(appId)
            loginAudit.setAuditTime(new Date())
            loginAudit.setLoginId(loginId)
            if(getAuditIpAddressConfigration()=='y'){
                loginAudit.setIpAddress(ipAddress)
            }
            else if(getAuditIpAddressConfigration()=='mask'){
                loginAudit.setIpAddress(getMaskedIpAddress(ipAddress))
            }
            else {
                loginAudit.setIpAddress("NA")
            }
            loginAudit.setUserAgent(userAgent)
            loginAudit.setLastModifiedBy(loginId)
            loginAudit.setPidm(userpidm as Integer)
            loginAudit.setVersion(0L)
            loginAudit.setLogonComment(comment)
            this.create(loginAudit)
        }catch (Exception ex) {
            log.error("Exception occured while creating loginAudit ${ex.getMessage()}")
        }
    }

    public List getDataByLoginID(loginId) {
        List loginAuditPage = LoginAudit.fetchByLoginId(loginId)
        return loginAuditPage
    }

    public static String getClientIpAddress(request){
        String ipAddressList = request.getHeader("X-FORWARDED-FOR");
        String clientIpAddress
        if (ipAddressList?.length() > 0)  {
            String ipAddress = ipAddressList.split(",")[0];
            if ( ipAddress.length() <= LoginAudit.getConstrainedProperties().get('ipAddress').getMaxSize() ) {
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


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
            String ipAddress = getClientIpAdress(request);
            String userAgent = request.getHeader("User-Agent")

            LoginAudit loginAudit = new LoginAudit()
            loginAudit.setAppId(appId)
            loginAudit.setAuditTime(new Date())
            loginAudit.setLoginId(loginId)
            loginAudit.setIpAddress(ipAddress)
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
}


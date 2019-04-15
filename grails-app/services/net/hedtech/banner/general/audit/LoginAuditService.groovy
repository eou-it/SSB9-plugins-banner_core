/*******************************************************************************
 Copyright 2019 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.audit

import grails.gorm.transactions.Transactional
import grails.util.Holders
import net.hedtech.banner.security.BannerGrantedAuthorityService
import net.hedtech.banner.service.ServiceBase
import org.springframework.dao.InvalidDataAccessResourceUsageException

@Transactional
class LoginAuditService extends ServiceBase {

    private String appId
    private Date auditTime
    private String loginId
    private String ipAddress
    private String osUser
    private Date lastModified
    private String lastModifiedBy
    private Integer pidm
    private String logonComment
    private String dataOrigin

    public def createLoginAudit() {

        try {

        def user = BannerGrantedAuthorityService.getUser()
        appId = Holders.config.info.app.appId
        auditTime = new Date()
        loginId = user.username
        ipAddress = InetAddress.getLocalHost().getHostAddress()
        osUser = System.getProperty('os.name')
        lastModified =  new Date()
        lastModifiedBy = Holders.config.info.app.appId
        pidm = user.pidm
        dataOrigin = Holders.config.dataOrigin


        LoginAudit loginAudit = new LoginAudit()
        loginAudit.setAppId(appId)
        loginAudit.setAuditTime(auditTime)
        loginAudit.setLoginId(loginId)
        loginAudit.setIpAddress(ipAddress)
        loginAudit.setUserAgent(osUser)
        loginAudit.setLastModified(lastModified)
        loginAudit.setLastModifiedBy(lastModifiedBy)
        loginAudit.setPidm(pidm)
        loginAudit.setDataOrigin(dataOrigin)
        loginAudit.setLogonComment(logonComment)
        this.create(loginAudit)
        }catch (InvalidDataAccessResourceUsageException ex) {
            log.error("Exception occured while executing seedUserPreferenceConfig " + ex.getMessage())
        }
    }
    public def getDataByLoginID(getDataByLoginID) {
        LoginAudit loginAuditPage = LoginAudit.fetchByLoginId(getDataByLoginID)
        return loginAuditPage;
    }

}

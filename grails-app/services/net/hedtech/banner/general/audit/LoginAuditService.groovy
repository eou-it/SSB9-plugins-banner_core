/*******************************************************************************
Copyright 2009-2019 Ellucian Company L.P. and its affiliates.
*******************************************************************************/
package net.hedtech.banner.general.audit

import grails.gorm.transactions.Transactional
import grails.util.Holders
import groovy.sql.Sql
import groovy.util.logging.Slf4j
import net.hedtech.banner.general.audit.LoginAudit
import net.hedtech.banner.security.BannerAuthenticationEvent
import net.hedtech.banner.security.BannerGrantedAuthorityService
import net.hedtech.banner.service.ServiceBase
import org.springframework.context.ApplicationListener
import org.springframework.dao.InvalidDataAccessResourceUsageException
import org.springframework.web.context.request.RequestContextHolder

@Slf4j
@Transactional
public class LoginAuditService extends ServiceBase implements ApplicationListener<BannerAuthenticationEvent>  {

    def dataSource // injected by Spring
    private String appId
    private Date auditTime
    private String loginId
    private String ipAddress
    private String userAgent
    private Date lastModified
    private String lastModifiedBy
    private Integer pidm
    private String logonComment
    private String dataOrigin
    private Long version

    public void onApplicationEvent( BannerAuthenticationEvent event ) {
        log.debug "In LoginAuditService  onApplicationEvent with ${event}"
        if (event.isSuccess) {
            userLogin event
        } else {
           loginViolation event
        }
    }


    public void userLogin( BannerAuthenticationEvent event ) {
        log.debug "In LoginAuditService  userLogin with event = ${event}"
        def conn
        def sql
        try {
            conn = dataSource.unproxiedConnection
            sql = new Sql( conn )
            if (event.userName?.size() > 0)  {
                def truncateUserName = event.userName[0..(event.userName.size() > 29 ? 29 : event.userName.size() - 1)]
                sql.call("begin g\$_security.g\$_check_logon_rules('BAN9',?); commit; end;",[truncateUserName])
            }
        } catch (Exception e) {
            log.error "Exception occured in userLogin with error = ${e}"
            e.printStackTrace()
        } finally {
            conn?.close()
        }
    }

    public void loginViolation( BannerAuthenticationEvent event  ) {
        log.debug "In LoginAuditService  loginViolation with event = ${event}"
        def conn
        def sql
        try {
            conn = dataSource.unproxiedConnection
            sql = new Sql( conn )
            if (event.userName?.size() > 0 && event.module?.size() > 0 )  {
                def truncateUserName = event.userName[0..(event.userName.size() > 29 ? 29 : event.userName.size() - 1)]
                sql.call("begin g\$_security.g\$_create_log_record(?,?,?,?); commit; end;",[truncateUserName,event.module,event.message, event.severity])
            }
        } catch (Exception e) {
            log.error "Exception occured in loginViolation with error = ${e}"
            e.printStackTrace()
        } finally {
            conn?.close()
        }
    }



    public def createLoginAudit(authenticationResults) {

        try {

            def user = BannerGrantedAuthorityService.getUser()
            appId = Holders.config.app.appId
            auditTime = new Date()
            loginId = authenticationResults.name
            def request = RequestContextHolder.getRequestAttributes()?.request
            ipAddress = request.getRemoteAddr()
            userAgent = request.getHeader("User-Agent");
            lastModified =  new Date()
            lastModifiedBy = authenticationResults.fullName
            pidm = authenticationResults.pidm
            dataOrigin = Holders.config.dataOrigin
            version = 0L


            LoginAudit loginAudit = new LoginAudit()
            loginAudit.setAppId(appId)
            loginAudit.setAuditTime(auditTime)
            loginAudit.setLoginId(loginId)
            loginAudit.setIpAddress(ipAddress)
            loginAudit.setUserAgent(userAgent)
            loginAudit.setLastModified(lastModified)
            loginAudit.setLastModifiedBy(lastModifiedBy)
            loginAudit.setPidm(pidm)
            loginAudit.setDataOrigin(dataOrigin)
            loginAudit.setVersion(version)
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


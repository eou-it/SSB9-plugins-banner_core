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


@Transactional
public class LoginAuditService extends ServiceBase implements ApplicationListener<BannerAuthenticationEvent>  {

    def dataSource // injected by Spring

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



    public def createLoginLogoutAudit(authenticationResults,comment) {

        try {
            String appId = Holders.config.app.appId
            Date auditTime = new Date()
            String loginId =  authenticationResults.username ? authenticationResults.username : authenticationResults.name ? authenticationResults.name : 'ANONYMOUS'
            def request = RequestContextHolder.getRequestAttributes()?.request
            String ipAddress = request.getRemoteAddr()
            String userAgent = request.getHeader("User-Agent")
            Date lastModified =  new Date()
            String lastModifiedBy = 'Banner'
            Integer pidm = authenticationResults.pidm
            String dataOrigin = Holders.config.dataOrigin
            Long version = 0L
            String logonComment = comment


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
            log.error("Exception occured while executing loginAudit " + ex.getMessage())
        }
    }

    public def getDataByLoginID(getDataByLoginID) {
        LoginAudit loginAuditPage = LoginAudit.fetchByLoginId(getDataByLoginID)
        return loginAuditPage;
    }
}


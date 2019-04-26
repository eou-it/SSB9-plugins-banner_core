/*******************************************************************************
 Copyright 2009-2019 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.service


import groovy.sql.Sql
import net.hedtech.banner.security.BannerAuthenticationEvent
import org.springframework.context.ApplicationListener

class BannerLoginAuditService implements ApplicationListener<BannerAuthenticationEvent> {

    def dataSource // injected by Spring

    public void onApplicationEvent( BannerAuthenticationEvent event ) {
        if (event.isSuccess) {
            userLogin event
        } else {
            loginViolation event
        }
    }


    public void userLogin( BannerAuthenticationEvent event ) {
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
            e.printStackTrace()
        } finally {
            conn?.close()
        }
    }

    public void loginViolation( BannerAuthenticationEvent event  ) {
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
            e.printStackTrace()
        } finally {
            conn?.close()
        }
    }
}

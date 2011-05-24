/** *****************************************************************************
 © 2010 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */


package com.sungardhe.banner.service

import groovy.sql.Sql
import com.sungardhe.banner.security.BannerAuthenticationEvent
import org.springframework.context.ApplicationListener

public class LoginAuditService implements ApplicationListener<BannerAuthenticationEvent> {

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
            sql.call("begin g\$_security.g\$_check_logon_rules('BAN9',?); commit; end;",[event.userName])
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
            if (event.userName.size() > 0 && event.module.size() > 0 )
                sql.call("begin g\$_security.g\$_create_log_record(?,?,?,?); commit; end;",[event.userName,event.module,event.message, event.severity])
        } catch (Exception e) {
            e.printStackTrace()
        } finally {
            conn?.close()
        }
    }
}


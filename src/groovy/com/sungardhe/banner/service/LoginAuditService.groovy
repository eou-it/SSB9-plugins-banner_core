/*********************************************************************************
 Copyright 2009-2011 SunGard Higher Education. All Rights Reserved.
 This copyrighted software contains confidential and proprietary information of 
 SunGard Higher Education and its subsidiaries. Any use of this software is limited 
 solely to SunGard Higher Education licensees, and is further subject to the terms 
 and conditions of one or more written license agreements between SunGard Higher 
 Education and the licensee in question. SunGard is either a registered trademark or
 trademark of SunGard Data Systems in the U.S.A. and/or other regions and/or countries.
 Banner and Luminis are either registered trademarks or trademarks of SunGard Higher 
 Education in the U.S.A. and/or other regions and/or countries.
 **********************************************************************************/


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


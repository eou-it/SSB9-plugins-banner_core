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
            def name = event.userName
            conn = dataSource.unproxiedConnection
            sql = new Sql( conn )
            def count = 0
            sql.eachRow("select GURLOGN_BAN9_LOGON_COUNT from GURLOGN where upper(GURLOGN_USER) =upper(?)", [name]) { row ->
                count = row.GURLOGN_BAN9_LOGON_COUNT
            }
            if (count == 0) {
                sql.execute("""insert into GURLOGN(GURLOGN_USER, GURLOGN_BAN9_LOGON_COUNT, GURLOGN_BAN9_LAST_LOGON_DATE,
                    GURLOGN_BAN9_FIRST_LOGON_DATE) values (?,?,sysdate,sysdate)""", [name, 1])
                sql.commit()
                return
            } else if (count == null) {
                sql.executeUpdate("""update GURLOGN set GURLOGN_BAN9_LOGON_COUNT = ?, GURLOGN_BAN9_LAST_LOGON_DATE = sysdate,
                    GURLOGN_BAN9_FIRST_LOGON_DATE = sysdate where GURLOGN_USER = ?""", [1,  name])
                sql.commit()
            } else {
                sql.executeUpdate("""update GURLOGN set GURLOGN_BAN9_LOGON_COUNT = ?, GURLOGN_BAN9_LAST_LOGON_DATE = sysdate
                    where GURLOGN_USER = ?""", [count + 1, name])
                sql.commit()
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
            java.sql.Date date = new java.sql.Date( System.currentTimeMillis() )
            sql.execute("""insert into bansecr.guralog(GURALOG_OBJECT, GURALOG_USERID, GURALOG_REASON, GURALOG_SEVERITY_LEVEL,
                GURALOG_ACTIVITY_DATE) values (?,?,?,?,?)""", [event.module, event.userName, event.message, event.severity, date])
            sql.commit()
        } catch (Exception e) {
            e.printStackTrace()
        } finally {
            conn?.close()
        }
    }
}


/** *****************************************************************************

 © 2011 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */

package com.sungardhe.banner.service

import groovy.sql.Sql
import org.springframework.web.context.request.RequestContextHolder

public class DefaultLoaderService {

    def dataSource                  // injected by Spring
    def dateFormatMap = [ 1:"MDY", 2:"DMY", 3:"YMD" ]

    public void loadDefault( def userName ) {
        def conn
        def sql
        def defaultMap = new HashMap()
        try {
            conn = dataSource.unproxiedConnection
            sql = new Sql(conn)
            int dateFormat
            sql.eachRow("select GUBINST_DATE_DEFAULT_FORMAT from gubinst ") { row ->
                dateFormat = row.GUBINST_DATE_DEFAULT_FORMAT
            }
            def mappedDateFormat
            if(dateFormat)
                mappedDateFormat = dateFormatMap[ dateFormat ]
            defaultMap.DATE_DEFAULT_FORMAT = mappedDateFormat

            Date lastLogonDate
            sql.eachRow( "select gurlogn_hrzn_last_logon_date from gurlogn where gurlogn_user = ?", [ userName.toUpperCase()]) { row ->
                lastLogonDate = row.gurlogn_hrzn_last_logon_date
            }
            def strDate
            if(lastLogonDate){
                strDate = lastLogonDate.format("dd-MMM-yyyy")
            } else {
                strDate = new Date().format("dd-MMM-yyyy")
            }
            defaultMap.LAST_LOGON_DATE = strDate

            RequestContextHolder.currentRequestAttributes().request.session.setAttribute("DEFAULTS", defaultMap)
        } catch(Exception e) {
            e.printStackTrace()
        }
            finally {
            conn?.close()
        }
    }
}
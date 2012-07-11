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
import org.springframework.web.context.request.RequestContextHolder
import com.sungardhe.banner.i18n.DateConverterService

public class DefaultLoaderService {

    def dataSource                  // injected by Spring
    def dateFormatMap = [ 1:"MDY", 2:"DMY", 3:"YMD" ]
    def dateConverterService

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
            dateConverterService = new DateConverterService()
            if(lastLogonDate){
                strDate = dateConverterService.convertGregorianToDefaultCalendar(lastLogonDate)
            } else {
                strDate = dateConverterService.convertGregorianToDefaultCalendar(new Date())
            }
            defaultMap.LAST_LOGON_DATE = strDate.toString()

            RequestContextHolder.currentRequestAttributes().request.session.setAttribute("DEFAULTS", defaultMap)
        } catch(Exception e) {
            e.printStackTrace()
        }
            finally {
            conn?.close()
        }
    }
}
/*******************************************************************************
Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
*******************************************************************************/ 

package net.hedtech.banner.service

import groovy.sql.Sql
import org.springframework.web.context.request.RequestContextHolder
import net.hedtech.banner.i18n.DateConverterService

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

package com.sungardhe.banner.service

import groovy.sql.Sql
import org.springframework.web.context.request.RequestContextHolder

public class DefaultLoaderService {

    def dataSource                  // injected by Spring
    def dateFormatMap = [ 1:"MDY", 2:"DMY", 3:"YMD" ]

    public void loadDefault() {
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
            RequestContextHolder.currentRequestAttributes().request.session.setAttribute("DEFAULTS", defaultMap)
        } catch(Exception e) {
            e.printStackTrace()
        }
            finally {
            conn?.close()
        }
    }
}
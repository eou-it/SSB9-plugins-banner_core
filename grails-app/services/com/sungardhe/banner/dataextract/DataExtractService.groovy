/** *******************************************************************************
 Copyright 2009-2011 SunGard Higher Education. All Rights Reserved.
 This copyrighted software contains confidential and proprietary information of
 SunGard Higher Education and its subsidiaries. Any use of this software is limited
 solely to SunGard Higher Education licensees, and is further subject to the terms
 and conditions of one or more written license agreements between SunGard Higher
 Education and the licensee in question. SunGard is either a registered trademark or
 trademark of SunGard Data Systems in the U.S.A. and/or other regions and/or countries.
 Banner and Luminis are either registered trademarks or trademarks of SunGard Higher
 Education in the U.S.A. and/or other regions and/or countries.
 ********************************************************************************* */
package com.sungardhe.banner.dataextract

import org.apache.log4j.Logger
import groovy.sql.Sql
import org.springframework.security.core.context.SecurityContextHolder as SCH


class DataExtractService {

    static transactional = true
    private final Logger log = Logger.getLogger(getClass())
    def sessionFactory                     // injected by Spring
    def dataSource                         // injected by Spring


    def hasDataExtract(id) {
        def dataExtract = false
        def objName

        if (id == null)
            return false


        Sql sql = new Sql(sessionFactory.getCurrentSession().connection())
        sql.eachRow("select * from gubpage where gubpage_name = ?", [id]) {
            objName = it.gubpage_code
        }
        if (objName) {
            def session = sessionFactory.getCurrentSession()
            def resultSet = session.createSQLQuery("SELECT GUBOBJS_EXTRACT_ENABLED_IND FROM GUBOBJS WHERE GUBOBJS_NAME = :objName and GUBOBJS_EXTRACT_ENABLED_IND != 'N'").setString("objName", objName).list()

            resultSet.each() {
                dataExtract = true
            }
        }
        dataExtract
    }
}

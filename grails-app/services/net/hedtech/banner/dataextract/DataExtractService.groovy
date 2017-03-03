/*******************************************************************************
Copyright 2009-2016 Ellucian Company L.P. and its affiliates.
*******************************************************************************/ 
package net.hedtech.banner.dataextract

import groovy.sql.Sql

class DataExtractService {

    static transactional = true
    def sessionFactory                     // injected by Spring
    def dataSource                         // injected by Spring


    def hasDataExtract(id) {
        def dataExtract
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
                dataExtract = it
            }
        }
        dataExtract
    }
}

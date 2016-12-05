/*******************************************************************************
 Copyright 2009-2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.utility

import groovy.sql.Sql
/**
 * To change this template use File | Settings | File Templates.
 */
class GlobalContextMappingService {

    static transactional = true
    def sessionFactory                     // injected by Spring
    def dataSource                         // injected by Spring


    def getGlobalNameByContext(def contextName) {
        Sql sql
        try {
            sql = new Sql(sessionFactory.getCurrentSession().connection())
            def globalName

            sql.call("{$Sql.VARCHAR = call gokgtrn.f_get_banner9_global_name(${contextName})") {globalNameOut -> globalName = globalNameOut }
            globalName
        } catch (e) {
            throw e
        } finally {
            sql?.close()
        }
    }


    def getContextByGlobalName(def globalName) {
        Sql sql
        try {
            sql = new Sql(sessionFactory.getCurrentSession().connection())
            def context

            sql.call("{$Sql.VARCHAR = call gokgtrn.f_get_cntx_name_by_global9(${globalName})") {contextOut -> context = contextOut }
            context
        } catch (e) {
            throw e
        } finally {
            sql?.close()
        }
    }
}

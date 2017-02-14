package net.hedtech.banner.general.utility

import groovy.sql.Sql
import org.apache.log4j.Logger

/**
 * Created by IntelliJ IDEA.
 * User: mhitrik
 * Date: 1/8/13
 * Time: 3:49 PM
 * To change this template use File | Settings | File Templates.
 */
class GlobalContextMappingService {

    static transactional = true
    private final Logger log = Logger.getLogger(GlobalContextMappingService.class)
    def sessionFactory                     // injected by Spring
    def dataSource                         // injected by Spring


    def getGlobalNameByContext(def contextName) {
        Sql sql = new Sql(sessionFactory.getCurrentSession().connection())
        def globalName
        try {
            sql.call("{$Sql.VARCHAR = call gokgtrn.f_get_banner9_global_name(${contextName})") {globalNameOut -> globalName = globalNameOut }
            globalName
        } catch (e) {
            throw e
        } finally {
            sql?.close()
        }
    }


    def getContextByGlobalName(def globalName) {
        Sql sql = new Sql(sessionFactory.getCurrentSession().connection())
        def context
        try {
            sql.call("{$Sql.VARCHAR = call gokgtrn.f_get_cntx_name_by_global9(${globalName})") {contextOut -> context = contextOut }
            context
        } catch (e) {
            throw e
        } finally {
            sql?.close()
        }
    }
}

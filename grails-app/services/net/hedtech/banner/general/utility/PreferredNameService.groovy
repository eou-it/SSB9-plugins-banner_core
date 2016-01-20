/*******************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

package net.hedtech.banner.general.utility

import grails.util.Holders
import groovy.sql.Sql
import org.apache.log4j.Logger


class PreferredNameService {

    static transactional = true
    private final Logger log = Logger.getLogger(getClass())
    def sessionFactory                     // injected by Spring
    def dataSource                         // injected by Spring
    def config = Holders.getConfig()

    public final String PARAM_INDICATOR = "=>"

    def columnNameMap = ["pidm":"p_pidm", "usage":"p_usage"]

    public String getParams(params) {
        println params
        String procParams
        params?.each{ it->
            println "Initial vlaue is "+procParams
            procParams = procParams? procParams+",":''
            println it.key +"- "+it.value
            procParams  = procParams + columnNameMap.get(it.key) + PARAM_INDICATOR + it.value
        }
        println "procParams "+procParams
        return procParams
    }

    public String getName(params){
        String preferredName = ""
        String queryParams = getParams(params)
        println queryParams
        Sql sql = new Sql(sessionFactory.getCurrentSession().connection())
        try {
            sql.call("{$Sql.VARCHAR = call gokname.f_get_name(p_pidm=>${params.pidm},p_usage=>${params.usage})") {preferredNameOut -> preferredName = preferredNameOut }
            println "preferredNameOut is "+preferredName
            return preferredName
        } catch (e) {
            throw e
        } finally {
            sql?.close()
        }
    }

    public String getUsage(String pageName='', String sectionName=''){
        String productName = config.banner?.productName?config.banner?.productName:'Payroll'
        String applicationName = config.banner?.applicationName?config.banner?.applicationName:'Taxes'
        println "productName '"+productName+"'"
        println "applicationName '"+productName+"'"
        String usage
        Sql sql = new Sql(sessionFactory.getCurrentSession().connection())
        try {
            sql.call("{$Sql.VARCHAR = call gokname.f_get_usage(${productName},${applicationName},${pageName},${sectionName})") {usageOut -> usage = usageOut }
            println "usage is "+usage
            return usage
        } catch (e) {
            throw e
        } finally {
            sql?.close()
        }
    }
}

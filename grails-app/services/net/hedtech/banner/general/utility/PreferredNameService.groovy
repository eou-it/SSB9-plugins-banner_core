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

    public String getName(params){
        String preferredName = ""
        Sql sql = new Sql( dataSource.getSsbConnection() )
        try {
            sql.call("{$Sql.VARCHAR = call gokname.f_get_name(${params.pidm}," +
                    "${params.usage}," +
                    "${params.produtname}," +
                    "${params.appname}," +
                    "${params.pageName}," +
                    "${params.sectionname}," +
                    "${params.maxlength}," +
                    "${params.usedata}," +
                    "${params.nametype}," +
                    "${params.entity}," +
                    "${params.id}," +
                    "${params.nameprefix}," +
                    "${params.firstname}," +
                    "${params.mi}," +
                    "${params.surnameprefix}," +
                    "${params.lastname}," +
                    "${params.namesuffix}," +
                    "${params.legalname}," +
                    "${params.prefname}," +
                    "${params.debug})") {preferredNameOut -> preferredName = preferredNameOut }
            return preferredName
        } catch (e) {
            log.error "Error with Preferred Name Procedure gokname.f_get_name . Exception Encountered :"
      } finally {
            sql?.close()
        }
    }

    public String getUsage(String pageName='', String sectionName=''){
        String productName = config?.productName ? Holders?.config?.productName:''
        String applicationName = config?.banner.applicationName ? Holders?.config?.banner.applicationName:''
        String usage
        String result
        Sql sql = new Sql( dataSource.getSsbConnection() )
        try {
            sql.call("{$Sql.VARCHAR = call gokname.f_get_usage(${productName},${applicationName},${pageName},${sectionName})") {usageOut -> result = usageOut }
            usage = result?.substring(4);
            return usage
        } catch (e) {
            log.error "Error with Preferred Name Procedure gokname.f_get_usage . Exception Encountered :"
        } finally {
            sql?.close()
        }
    }
}

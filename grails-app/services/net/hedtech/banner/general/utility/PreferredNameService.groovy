/*******************************************************************************
 Copyright 2015 -2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

package net.hedtech.banner.general.utility

import grails.util.Holders
import groovy.sql.Sql
import net.hedtech.banner.exceptions.ApplicationException
import java.sql.Connection
import java.sql.SQLException
import grails.gorm.transactions.Transactional

@Transactional
class PreferredNameService {

    def dataSource //injected by Spring

    def config = Holders.getConfig()

    public String getName(params, conn) {
        String preferredName = ""
        if(params.pidm instanceof Boolean)
            return preferredName

        //Level level = Sql.LOG.level
        //Sql.LOG.level = java.util.logging.Level.SEVERE
        Sql sql = new Sql( conn )
        try {
            sql.call("{? = call gokname.f_get_name(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}",
                    [
                       Sql.VARCHAR,
                       params.pidm,
                       params.usage,
                       params.productname,
                       params.appname,
                       params.pagename,
                       params.sectionname,
                       params.maxlength,
                       params.usedata,
                       params.nametype,
                       params.entity,
                       params.id,
                       params.nameprefix,
                       params.firstname,
                       params.mi,
                       params.surnameprefix,
                       params.lastname,
                       params.namesuffix,
                       params.legalname,
                       params.prefname,
                       params.debug
                    ]
                )  {  preferredNameOut -> preferredName = preferredNameOut }
         } catch(SQLException ex){
           log.error "SQLException occured while executing Preferred Name Script ", ex
         }
        if(preferredName?.contains("*ERROR*"))  {
            log.error "Error occurred while fetching Preferred Name with error : ${preferredName}"
            throw new ApplicationException(PreferredNameService, preferredName)
        }
        //Sql.LOG.level = level
        return preferredName
    }

    public String getUsage(String pageName='', String sectionName=''){
        String productName = config?.productName ? config?.productName:''
        String applicationName = config?.banner.applicationName ? config?.banner.applicationName:''
        return getUsage(productName, applicationName, pageName, sectionName)
    }

    public String getUsage(String productName,String applicationName,String pageName, String sectionName){
        String usage,result
        Sql sql = new Sql( dataSource.getSsbConnection() )
        try {
            sql.call("{$Sql.VARCHAR = call gokname.f_get_usage(${productName},${applicationName},${pageName},${sectionName})") {usageOut -> result = usageOut }
            usage = result?.substring(4);
        } catch (e) {
            log.trace "Error with Preferred Name Procedure gokname.f_get_usage . Exception Encountered :"
        } finally {
            sql?.close()
        }
        return usage
    }

    public  String getPreferredName(pidm, conn){
        def params = setupPreferredNameParams(pidm)
        String displayName = getName(params,conn)
        log.debug "PreferredNameService.getPreferredName is returning $displayName"
        return displayName
    }

    private  LinkedHashMap setupPreferredNameParams(pidm) {
        def productName = (Holders?.config?.productName) ? (Holders?.config?.productName) : null
        def applicationName = (Holders?.config?.banner.applicationName) ? (Holders?.config?.banner.applicationName) : null
        def params = [:]
        params.put("pidm", pidm)
        if (productName != null)
            params.put("productname", productName)
        if (applicationName != null)
            params.put("appname", applicationName)

        return params
    }

    public  String getPreferredName(pidm) {
        def params = setupPreferredNameParams(pidm)
        return getPreferredName(params)
    }

    public  String getPreferredName(Map params) throws ApplicationException{
        Connection conn = dataSource.getSsbConnection()
        String displayName
        if(conn)    {
            try {
                displayName = getName(params,conn)
            }
            finally{
                conn?.close()
            }
        } else {
            log.trace "PreferredNameService Self Service Connection object is null "
        }
        return displayName
    }
}

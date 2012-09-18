/*******************************************************************************
Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
*******************************************************************************/ 
package net.hedtech.banner.supplemental

import org.hibernate.persister.entity.SingleTableEntityPersister
import org.apache.log4j.Logger
import groovy.sql.Sql
import java.sql.Connection;
import net.hedtech.banner.db.BannerDS as BannerDataSource
import net.hedtech.banner.db.BannerConnection
import org.springframework.context.ApplicationContext
import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.codehaus.groovy.grails.commons.GrailsApplication
import net.hedtech.banner.configuration.SupplementalDataUtils

/**
 * A service used to support persistence of supplemental data.
 */
class SupplementalDataService {

    static transactional = true
    private final Logger log = Logger.getLogger(getClass())

    def dataSource                         // injected by Spring
    def sessionFactory                     // injected by Spring
    def supplementalDataPersistenceManager // injected by Spring
    def grailsApplication                  // injected by Spring

    def supplementalDataConfiguration = [:]


    public def getSupplementalDataConfigurationFor(Class modelClass) {
        supplementalDataConfiguration."${modelClass.name}"
    }


    def init() {
        Map x = sessionFactory.getAllClassMetadata()
        for (Iterator i = x.values().iterator(); i.hasNext();) {
            SingleTableEntityPersister y = (SingleTableEntityPersister) i.next();
            setSDE(y.getName(), SupplementalDataUtils.getTableName(y.getTableName().toUpperCase()))
            // for (int j = 0; j < y.getPropertyNames().length; j++) {
            //     println( " " + y.getPropertyNames()[ j ] + " -> " + (y.getPropertyColumnNames( j ).length > 0 ? y.getPropertyColumnNames( j )[ 0 ] : ""))
            // }
        }
        log.info "SupplementalDataService initialization complete."
    }

    /**
     * Resets the sde attributes if they are altered in run time
     * @param domain
     */
    public def refreshSdeForDomain(domain) {
        def clazz = domain?.getClass()
        def tableName = SupplementalDataUtils.getTableName(sessionFactory.getClassMetadata(domain?.getClass())?.tableName?.toUpperCase())
        def listOrigin = domain?.supplementalProperties?.keySet()?.asList()?.sort()
        def listUpdated = supplementalDataConfiguration?."${clazz.name}"?.keySet()?.asList()?.sort()
        if (!listOrigin.equals(listUpdated)) {
            supplementalDataConfiguration.remove("${clazz.name}")
            resetSDE(domain?.getClass().getName(), tableName)
        }
    }


    /**
     * Appends additional supplemental data configuration for a model. This is used for testing purposes.
     * @param map the additional supplemental data configuration in the form: [ modelClass: [ propertyName: [ required: boolean, dataType: someType ], ], ]
     */
    public void appendSupplementalDataConfiguration(Map map) {
        supplementalDataConfiguration << map
    }


    public boolean supportsSupplementalProperties(Class modelClass) {
        supplementalDataConfiguration.keySet().contains modelClass.name
    }


    public List supplementalPropertyNamesFor(Class modelClass) {
        supplementalDataConfiguration."${modelClass.name}".keySet().asList()
    }


    public boolean hasSupplementalProperties(modelInstance) {
        modelInstance.hasSupplementalProperties()
    }


    public def loadSupplementalDataFor(model) {
        supplementalDataPersistenceManager.loadSupplementalDataFor(model)
    }


    public def persistSupplementalDataFor(model) {
        removeUnsupportedPropertiesFrom(model)
        supplementalDataPersistenceManager.persistSupplementalDataFor(model)
    }


    public def removeSupplementalDataFor(model) {
        supplementalDataPersistenceManager.removeSupplementalDataFor(model)
    }

    public boolean hasSde(id) {

        def sdeFound = false

        if (id == null || id.indexOf("Block") < 0)
            return false

        ApplicationContext ctx = (ApplicationContext) ApplicationHolder.getApplication().getMainContext()
        grailsApplication = (GrailsApplication) ctx.getBean("grailsApplication")

        def domainClass = grailsApplication.getArtefactByLogicalPropertyName("Domain", id.substring(0, id.indexOf("Block")))

        if (domainClass == null)
            return false

        def tableName = SupplementalDataUtils.getTableName(sessionFactory.getClassMetadata(domainClass?.getClazz())?.tableName.toUpperCase())

        if (tableName == null)
            return false

        Sql sql = new Sql(sessionFactory.getCurrentSession().connection())

        try {
            sql.call("{$Sql.VARCHAR = call gb_sde_table.f_exists($tableName)}") { sde ->
                sdeFound = "Y".equals(sde)
            }
            return sdeFound
        } catch (e) {
            log.error("ERROR: Could not SDE set up for table - $tableName . ${e.message}")
            throw e
        } finally {
            sql?.close()
        }
    }


    private def removeUnsupportedPropertiesFrom(model) {
        def supportedNames = supplementalPropertyNamesFor(model.class)
        def supportedProperties = model.supplementalProperties?.findAll { k, v -> k in supportedNames }
        model.supplementalProperties = supportedProperties
    }


    private setSDE(entityName, tableName) {

        boolean found = false
        Connection conn = (dataSource as BannerDataSource).getUnproxiedConnection() as BannerConnection
        Sql db = new Sql(conn)

        String rolePswd = ""
        try {
            db.call("{$Sql.VARCHAR = call g\$_security.g\$_get_role_password_fnc('BAN_DEFAULT_M' ,'SEED-DATA')}") {role -> rolePswd = role }
            String roleM = """SET ROLE "BAN_DEFAULT_M" IDENTIFIED BY "${rolePswd}" """
            db.execute(roleM)
            Connection sessionConnection = db.getConnection()

            def session = sessionFactory.openSession(sessionConnection);
            def resultSet = session.createSQLQuery("SELECT gorsdam_attr_name, gorsdam_attr_reqd_ind, gorsdam_attr_data_type FROM gorsdam WHERE gorsdam_table_name= :tableName").setString("tableName", tableName).list()
            def model = [:]
            def properties = [:]
            def sde = [:]

            resultSet.each() {
                found = true
                properties.required = it[1]
                def attrName = "${it[0]}"
                model."${attrName}" = properties
            }

            if (found) {
                sde."${entityName}" = model
                appendSupplementalDataConfiguration(sde)
                log.debug "SDE Table: ${tableName}"
            }
        } catch (e) {
            log.error("ERROR: Could not establish role set up to the database. ${e.message}")
            throw e
        } finally {
            db?.close()
        }
    }

    private resetSDE(entityName, tableName) {

        boolean found = false
        Sql sql = new Sql(sessionFactory.getCurrentSession().connection())

        def model = [:]
        def properties = [:]
        def sde = [:]

        sql.eachRow("SELECT gorsdam_attr_name, gorsdam_attr_reqd_ind, gorsdam_attr_data_type FROM gorsdam WHERE gorsdam_table_name= ?", [tableName]) {
            found = true
            properties.required = it[1]
            def attrName = "${it[0]}"
            model."${attrName}" = properties
        }

        if (found) {
            sde."${entityName}" = model
            appendSupplementalDataConfiguration(sde)
            log.debug "SDE Table: ${tableName}"
        }
    }
}

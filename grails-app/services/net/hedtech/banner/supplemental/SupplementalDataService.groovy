/*******************************************************************************
Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
*******************************************************************************/
package net.hedtech.banner.supplemental

import net.hedtech.banner.db.BannerDS as BannerDataSource

import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import java.sql.Connection
import net.hedtech.banner.configuration.SupplementalDataUtils
import net.hedtech.banner.db.BannerConnection
import org.apache.log4j.Logger
import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.hibernate.persister.entity.SingleTableEntityPersister
import org.springframework.context.ApplicationContext
import org.hibernate.MappingException
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.annotation.Propagation

/**
 * A service used to support persistence of supplemental data.
 */
class SupplementalDataService {

    static transactional = true
    private final Logger log = Logger.getLogger(getClass())
    private static final Logger staticLogger = Logger.getLogger(SupplementalDataService.class)

    def dataSource                         // injected by Spring
    def sessionFactory                     // injected by Spring
    def supplementalDataPersistenceManager // injected by Spring
    def grailsApplication                  // injected by Spring

    def supplementalDataConfiguration = [:]

    def tableToDomainMap = [:]

    def static session = null

    public String getMappedDomain (String tableName) {
        return tableToDomainMap[tableName]
    }

    public def getSupplementalDataConfigurationFor(Class modelClass) {
        supplementalDataConfiguration."${modelClass.name}"
    }


    def init() {
        Map x = sessionFactory.getAllClassMetadata()
        for (Iterator i = x.values().iterator(); i.hasNext();) {
            SingleTableEntityPersister y = (SingleTableEntityPersister) i.next();

            String underlyingTableName = SupplementalDataUtils.getTableName(y.getTableName().toUpperCase())

            tableToDomainMap[underlyingTableName] = y.getName()

            setSDE(y.getName(), underlyingTableName)
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

    public void markDomainForSupplementalData(model) {
        supplementalDataPersistenceManager.markDomainForSupplementalData(model)
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

    /**
     * Find LOV for a specific lov code and return it in a
     * generic lookup domain object.
     *
     * @param lovCode
     * @param additionalParams - carries the LOV Table info.
     * @return  - generic lookup domain object
     */
    def static findByLov (String lovCode, additionalParams= [:]) {
        def lookupDomainList = []

        if (additionalParams) {
            def lovTable = (additionalParams.lovForm == 'GTQSDLV')?'GTVSDLV':additionalParams.lovForm
            String query = "SELECT * FROM $lovTable"
            query += " WHERE ${lovTable}_CODE='$lovCode'"

            if (lovTable == 'GTVSDLV') {
                if ( additionalParams.lovTableOverride && additionalParams.lovAttributeOverride) {
                    query += " and GTVSDLV_TABLE_NAME='$additionalParams.lovTableOverride'"
                    query += " and GTVSDLV_ATTR_NAME='$additionalParams.lovAttributeOverride'"
                } else {
                    staticLogger.error ("SDE configuration : when LOV_FORM is GTVSDLV, TABLE_OVRD and ATTR_OVRD cannot be empty")
                }
            }

            staticLogger.debug("Querying on SDE Lookup Table started")
            Sql sql = new Sql(ApplicationHolder.getApplication().getMainContext().sessionFactory.getCurrentSession().connection())

            sql.rows(query)?.each { row ->
                createLookupDomainObject(lovTable, additionalParams, row, lookupDomainList)
            }

            staticLogger.debug("Querying on SDE Lookup Table executed" )
            sql.connection.close()
        }
        (lookupDomainList == [])?null:lookupDomainList[0]
    }

    /**
     * Find all LOV objects belong to a validation table.
     *
     * @param additionalParams - info on LOV table
     * @return  - list of generic lookup domain objects
     */
    def static findAllLovs (additionalParams = [:]) {
        def lookupDomainList = []

        if (additionalParams) {
            def lovTable = (additionalParams.lovForm == 'GTQSDLV')?'GTVSDLV':additionalParams.lovForm
            String query = "SELECT * FROM $lovTable"

            if (lovTable == 'GTVSDLV') {
                if ( additionalParams.lovTableOverride && additionalParams.lovAttributeOverride) {
                    query += " where GTVSDLV_TABLE_NAME='$additionalParams.lovTableOverride'"
                    query += " and GTVSDLV_ATTR_NAME='$additionalParams.lovAttributeOverride'"
                } else {
                    staticLogger.error ("SDE configuration : when LOV_FORM is GTVSDLV, TABLE_OVRD and ATTR_OVRD cannot be empty")
                }
            }

            staticLogger.debug("Querying on SDE Lookup Table started")
            Sql sql = new Sql(ApplicationHolder.getApplication().getMainContext().sessionFactory.getCurrentSession().connection())

            sql.rows(query)?.each { row ->
                createLookupDomainObject(lovTable, additionalParams, row, lookupDomainList)
            }

            staticLogger.debug("Querying on SDE Lookup Table executed" )
            sql.connection.close()
        }

        return (lookupDomainList == [])?([:]):([list:lookupDomainList, totalCount:lookupDomainList.size()])
    }

    /**
     * Filter LOV objects belong to a validation table based on a filter passed-in
     *
     * @param filter
     * @param additionalParams
     * @return - list of generic lookup domain objects
     */
    def static findAllLovs (filter, additionalParams) {
        def lookupDomainList = []

        if (additionalParams) {
            def lovTable = (additionalParams.lovForm == 'GTQSDLV')?'GTVSDLV':additionalParams.lovForm
            String query = "SELECT * FROM $lovTable"
            query += " WHERE (upper(${lovTable}_CODE) like upper('%${filter}%')"
            if (additionalParams.descNotAvailable) {
                // skip the desc part.
            } else {
                query += " OR upper(${lovTable}_DESC) like upper('%${filter}%')"
            }
            query += ")"

            if (lovTable == 'GTVSDLV') {
                if ( additionalParams.lovTableOverride && additionalParams.lovAttributeOverride) {
                    query += " and GTVSDLV_TABLE_NAME='$additionalParams.lovTableOverride'"
                    query += " and GTVSDLV_ATTR_NAME='$additionalParams.lovAttributeOverride'"
                } else {
                    staticLogger.error ("SDE configuration : when LOV_FORM is GTVSDLV, TABLE_OVRD and ATTR_OVRD cannot be empty")
                }
            }

            staticLogger.debug("Querying on SDE Lookup Table started")
            Sql sql = new Sql(ApplicationHolder.getApplication().getMainContext().sessionFactory.getCurrentSession().connection())

            sql.rows(query)?.each { row ->
                createLookupDomainObject(lovTable, additionalParams, row, lookupDomainList)
            }

            staticLogger.debug("Querying on SDE Lookup Table executed" )
            sql.connection.close()
        }
        return (lookupDomainList == [])?([:]):([list:lookupDomainList, totalCount:lookupDomainList.size()])
    }


    static def createLookupDomainObject(lovTable, additionalParams, GroovyRowResult row, ArrayList lookupDomainList) {
        DynamicLookupDomain lookupDomain = new DynamicLookupDomain()

        row.each { prop, propValue ->
            def modelProperty = SupplementalDataUtils.formatProperty(prop, additionalParams.lovForm)
            lookupDomain."${modelProperty}" = propValue
        }
        lookupDomainList << lookupDomain
    }

    /**
     * Find and return the matching domain property names
     * for the given list of table column names.
     *
     * @param domainClass
     * @param tableColumnNames
     * @return
     */
    def getDomainPropertyNames (Class domainClass, tableColumnNames) {
        def columnMappings = [:]

        def metadata = ApplicationHolder.getApplication().getMainContext().sessionFactory.getClassMetadata(domainClass)
        metadata.getPropertyNames().eachWithIndex { propertyName, i ->
            try {
                columnMappings[propertyName] = metadata.getPropertyColumnNames(i)[0]
            } catch (MappingException e){
                // no mapping for this property; so need to skip it.
            }
        }

        columnMappings?.findAll{ String prop, col ->  !prop.startsWith("_")}    // returns keys which are prop names.
    }

    @Transactional(readOnly = true, propagation = Propagation.REQUIRED )
    public boolean shouldShowSDE(def domain, def service) {
        def showSDEWindow = true

        if(service?.respondsTo("extractParams") && service?.respondsTo("fetch")) {
            def content =  service.extractParams( domain.getClass(), domain, log )
            def domainObject = service.fetch(  domain.getClass(), content?.id, log )
            domainObject.properties = content
            showSDEWindow = service.isDirty(domainObject)
        }

        return !showSDEWindow
    }

    public void restoreOriginalSupplementalProperties(domain, service) {
        if(supportsSupplementalProperties( domain.class )) {
            def originalDomain = service.fetch(  domain.getClass(), domain?.id, log )
            domain?.supplementalProperties = originalDomain.supplementalProperties
        }
    }
}

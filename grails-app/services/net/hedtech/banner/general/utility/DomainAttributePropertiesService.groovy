/*******************************************************************************
Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
*******************************************************************************/ 

package net.hedtech.banner.general.utility

import org.springframework.context.ApplicationContext
import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.codehaus.groovy.grails.commons.GrailsApplication
import net.hedtech.banner.configuration.SupplementalDataUtils
import org.codehaus.groovy.grails.commons.DefaultGrailsDomainClass
import org.apache.commons.lang.ClassUtils

import groovy.sql.Sql

class DomainAttributePropertiesService {

    static transactional = true

    def dataSource                         // injected by Spring
    def sessionFactory                     // injected by Spring
    def grailsApplication                  // injected by Spring
    def defaultDateFormat

    public extractClass(domainName) {

        if (!domainName)
            return null

        ApplicationContext ctx = (ApplicationContext) ApplicationHolder.getApplication().getMainContext()
        grailsApplication = (GrailsApplication) ctx.getBean("grailsApplication")

        def domainClass = grailsApplication.getArtefactByLogicalPropertyName("Domain", domainName)

        domainClass.getClazz()

    }

    public extractClassMetadataByName(domainName) {

        if (!domainName)
            return null

        ApplicationContext ctx = (ApplicationContext) ApplicationHolder.getApplication().getMainContext()
        grailsApplication = (GrailsApplication) ctx.getBean("grailsApplication")

        def domainClass = grailsApplication.getArtefactByLogicalPropertyName("Domain", domainName)

        if (domainClass == null)
            return null

        def tableName = SupplementalDataUtils.getTableName(sessionFactory.getClassMetadata(domainClass?.getClazz())?.tableName?.toUpperCase())

        def clazz = sessionFactory.getClassMetadata(domainClass?.getClazz().name)
        def entityMap = [:]

        entityMap.class = domainClass?.getClazz()
        entityMap.className = domainClass?.getClazz().name
        entityMap.entityName = domainClass?.getName()
        entityMap.tableName = tableName

        def mapAttributes = [:]
        for (int j = 0; j < clazz.getPropertyNames().length; j++) {
            def column = [:]

            def a = clazz.getPropertyNames()[j]
            def b = clazz.getPropertyColumnNames(j)[0]?.toUpperCase()

            column.columnName = b

            if (clazz.getPropertyNames()[j].indexOf("_net") < 0) {
                column.columnName = b

                mapAttributes."${a}" = column
            }
        }

        entityMap.attributes = mapAttributes

        def grailsDomainClass = new DefaultGrailsDomainClass(grailsApplication.getClassForName(domainClass?.getClazz().name))

        grailsDomainClass.properties.each {

            def propName = it.name
            def maxSize

            def constraintProperty = grailsDomainClass.constrainedProperties."$propName"
            if (constraintProperty?.propertyName) {

                if (!constraintProperty?.maxSize) {
                    if (ClassUtils.getShortClassName(constraintProperty?.propertyType?.name) == "String" ||
                            constraintProperty?.propertyType?.name.indexOf("net.") == 0) { //association atributes
                        maxSize = getCharLengthForColumn(tableName, entityMap.attributes."${constraintProperty?.propertyName}"?.columnName)
                    } else if (ClassUtils.getShortClassName(constraintProperty?.propertyType?.name) == "Date") {
                        maxSize = getDefaultDateFormat()
                    } else {
                        maxSize = getDataLengthForColumn(tableName, entityMap.attributes."${constraintProperty?.propertyName}"?.columnName)
                    }
                } else {
                    maxSize = constraintProperty?.maxSize
                }

                entityMap.attributes."${constraintProperty?.propertyName}"?.nullable = constraintProperty?.nullable
                entityMap.attributes."${constraintProperty?.propertyName}"?.maxSize = maxSize
                entityMap.attributes."${constraintProperty?.propertyName}"?.min = constraintProperty?.min
                entityMap.attributes."${constraintProperty?.propertyName}"?.max = constraintProperty?.max
                entityMap.attributes."${constraintProperty?.propertyName}"?.inList = constraintProperty?.inList
                entityMap.attributes."${constraintProperty?.propertyName}"?.scale = constraintProperty?.scale
                entityMap.attributes."${constraintProperty?.propertyName}"?.propertyType = ClassUtils.getShortClassName(constraintProperty?.propertyType?.name)

            }  else {
                entityMap.attributes."${it.name}"?.propertyType = ClassUtils.getShortClassName(it.type)

                if (ClassUtils.getShortClassName(it.type) == "String" ||
                        it.name.indexOf("net.") == 0) { //association atributes
                    maxSize = getCharLengthForColumn(tableName, entityMap.attributes."${it.name}"?.columnName)
                } else if (ClassUtils.getShortClassName(it.type) == "Date") {
                    maxSize = getDefaultDateFormat()
                } else {
                    maxSize = getDataLengthForColumn(tableName, entityMap.attributes."${it.name}"?.columnName)
                }

                entityMap.attributes."${it.name}"?.maxSize = maxSize
            }
        }

        return entityMap

    }

    public extractClassMetadataByNonDomain(domainClass) {

        if (!domainClass)
            return null

        ApplicationContext ctx = (ApplicationContext) ApplicationHolder.getApplication().getMainContext()
        grailsApplication = (GrailsApplication) ctx.getBean("grailsApplication")


        def tableName = SupplementalDataUtils.getTableName(sessionFactory.getClassMetadata(domainClass)?.tableName?.toUpperCase())

        def clazz = sessionFactory.getClassMetadata(domainClass?.name)
        if(!clazz) {
            return null
        }
        def entityMap = [:]

        entityMap.class = domainClass
        entityMap.className = domainClass?.name
        entityMap.entityName = domainClass?.getName()
        entityMap.tableName = tableName

        def mapAttributes = [:]
        for (int j = 0; j < clazz.getPropertyNames().length; j++) {
            def column = [:]

            def a = clazz.getPropertyNames()[j]
            def b = clazz.getPropertyColumnNames(j)[0]?.toUpperCase()

            column.columnName = b

            if (clazz.getPropertyNames()[j].indexOf("_net") < 0) {
                column.columnName = b

                mapAttributes."${a}" = column
            }
        }

        entityMap.attributes = mapAttributes

        def grailsDomainClass = new DefaultGrailsDomainClass(grailsApplication.getClassForName(domainClass?.name))

        grailsDomainClass.properties.each {

            def propName = it.name
            def maxSize

            def constraintProperty = grailsDomainClass.constrainedProperties."$propName"
            if (constraintProperty?.propertyName) {

                if (!constraintProperty?.maxSize) {
                    if (ClassUtils.getShortClassName(constraintProperty?.propertyType?.name) == "String" ||
                            constraintProperty?.propertyType?.name.indexOf("net.") == 0) { //association atributes
                        maxSize = getCharLengthForColumn(tableName, entityMap.attributes."${constraintProperty?.propertyName}"?.columnName)
                    } else if (ClassUtils.getShortClassName(constraintProperty?.propertyType?.name) == "Date") {
                        maxSize = getDefaultDateFormat()
                    } else {
                        maxSize = getDataLengthForColumn(tableName, entityMap.attributes."${constraintProperty?.propertyName}"?.columnName)
                    }
                } else {
                    maxSize = constraintProperty?.maxSize
                }

                entityMap.attributes."${constraintProperty?.propertyName}"?.nullable = constraintProperty?.nullable
                entityMap.attributes."${constraintProperty?.propertyName}"?.maxSize = maxSize
                entityMap.attributes."${constraintProperty?.propertyName}"?.min = constraintProperty?.min
                entityMap.attributes."${constraintProperty?.propertyName}"?.max = constraintProperty?.max
                entityMap.attributes."${constraintProperty?.propertyName}"?.inList = constraintProperty?.inList
                entityMap.attributes."${constraintProperty?.propertyName}"?.scale = constraintProperty?.scale
                entityMap.attributes."${constraintProperty?.propertyName}"?.propertyType = ClassUtils.getShortClassName(constraintProperty?.propertyType?.name)

            }  else {
                entityMap.attributes."${it.name}"?.propertyType = ClassUtils.getShortClassName(it.type)

                if (ClassUtils.getShortClassName(it.type) == "String" ||
                        it.name.indexOf("net.") == 0) { //association atributes
                    maxSize = getCharLengthForColumn(tableName, entityMap.attributes."${it.name}"?.columnName)
                } else if (ClassUtils.getShortClassName(it.type) == "Date") {
                    maxSize = getDefaultDateFormat()
                } else {
                    maxSize = getDataLengthForColumn(tableName, entityMap.attributes."${it.name}"?.columnName)
                }

                entityMap.attributes."${it.name}"?.maxSize = maxSize
            }
        }

        return entityMap

    }
    public extractClassMetadataByPojo(pojo) {
        def entityMap = [:]

        entityMap.class = pojo?.getClass()
        entityMap.className = pojo?.getClass().name
        entityMap.entityName = ClassUtils.getShortClassName(pojo.getClass())

        def mapAttributes = [:]
        pojo.metaClass.getProperties().each {
            if (it.getType().equals(String.class) || it.getType().equals(Integer.class) ||
                    it.getType().equals(Date.class) || it.getType().equals(BigDecimal.class)) {

                def column = [:]

                def a = it.name
                def b = it.name

                column.columnName = b
                column.propertyType = ClassUtils.getShortClassName(it.getType())

                mapAttributes."${a}" = column
            }
        }

        entityMap.attributes = mapAttributes

        return entityMap
    }


    public extractClassMetadataById(domainId) {
        if (!domainId || domainId.indexOf("Block") < 0)
            return null

        def name = domainId.substring(0, domainId.indexOf("Block"))

        if (!name)
            return null

        return extractClassMetadataByName(name)

    }


    def getCharLengthForColumn(tableName, columnName) {
        def maxSize
        def sql = new Sql(sessionFactory.getCurrentSession().connection())

        try {
            sql.eachRow("""
                     SELECT CHAR_LENGTH
                       FROM ALL_TAB_COLUMNS
                       WHERE TABLE_NAME = ?
                       AND COLUMN_NAME = ?
                   """, [tableName, columnName]) {

                maxSize = it[0]

            };

        } finally {
            sql?.close()
        }

        maxSize
    }


    def getDataLengthForColumn(tableName, columnName) {
        def maxSize
        def sql = new Sql(sessionFactory.getCurrentSession().connection())

        try {
            sql.eachRow("""
                     SELECT DATA_LENGTH
                       FROM ALL_TAB_COLUMNS
                       WHERE TABLE_NAME = ?
                       AND COLUMN_NAME = ?
                   """, [tableName, columnName]) {

                maxSize = it[0]

            };
        } finally {
            sql?.close()
        }

        maxSize
    }


    def getDefaultDateFormat() {
        if (defaultDateFormat) {
            defaultDateFormat
        } else {

            def sql = new Sql(sessionFactory.getCurrentSession().connection())

            try {
                sql.eachRow("""
                    select length(g\$_date.get_nls_date_format) from dual
                   """) {

                    defaultDateFormat = it[0]

                };
            } finally {
                sql?.close()
            }
            defaultDateFormat
        }
    }
}

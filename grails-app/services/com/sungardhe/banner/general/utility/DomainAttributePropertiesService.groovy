/** *****************************************************************************
 © 2011 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */

package com.sungardhe.banner.general.utility

import org.springframework.context.ApplicationContext
import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.codehaus.groovy.grails.commons.GrailsApplication
import com.sungardhe.banner.configuration.SupplementalDataUtils
import org.hibernate.persister.entity.SingleTableEntityPersister
import org.codehaus.groovy.grails.commons.DefaultGrailsDomainClass
import org.apache.commons.lang.ClassUtils

import groovy.sql.Sql

class DomainAttributePropertiesService {

    static transactional = true

    def dataSource                         // injected by Spring
    def sessionFactory                     // injected by Spring
    def grailsApplication                  // injected by Spring

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

        def tableName = SupplementalDataUtils.getTableName(sessionFactory.getClassMetadata(domainClass?.getClazz())?.tableName.toUpperCase())

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
            def b = clazz.getPropertyColumnNames(j)[0].toUpperCase()

            column.columnName = b

            if (clazz.getPropertyNames()[j].indexOf("_com") < 0) {
                column.columnName = b

                mapAttributes."${a}" = column
            }
        }

        entityMap.attributes = mapAttributes

        def grailsDomainClass = new DefaultGrailsDomainClass(grailsApplication.getClassForName(domainClass?.getClazz().name))

        grailsDomainClass.properties.each {

            def propName = it.name

            def constraintProperty = grailsDomainClass.constrainedProperties."$propName"
            if (constraintProperty?.propertyName) {

                def maxSize

                if (!constraintProperty?.maxSize) {
                    if (ClassUtils.getShortClassName(constraintProperty?.propertyType?.name) == "String" ||
                                                     constraintProperty?.propertyType?.name.indexOf("com.") == 0 ) { //association atributes
                        maxSize = getCharLengthForColumn(tableName, entityMap.attributes."${constraintProperty?.propertyName}"?.columnName)
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

            }
        }

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

        sql.eachRow("""
                     SELECT CHAR_LENGTH
                       FROM ALL_TAB_COLUMNS
                       WHERE TABLE_NAME = ?
                       AND COLUMN_NAME = ?
                   """, [tableName, columnName]) {

            maxSize = it[0]

        };

        maxSize
    }

    def getDataLengthForColumn(tableName, columnName) {

        def maxSize

        def sql = new Sql(sessionFactory.getCurrentSession().connection())

        sql.eachRow("""
                     SELECT DATA_LENGTH
                       FROM ALL_TAB_COLUMNS
                       WHERE TABLE_NAME = ?
                       AND COLUMN_NAME = ?
                   """, [tableName, columnName]) {

            maxSize = it[0]

        };

        maxSize
    }
}

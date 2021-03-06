/*******************************************************************************
 Copyright 2009-2018 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.query

import grails.util.Holders
import grails.web.context.ServletContextHolder
import groovy.util.logging.Slf4j
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.i18n.MessageHelper
import net.hedtech.banner.query.criteria.CriteriaParam
import org.grails.web.util.GrailsApplicationAttributes
import org.hibernate.hql.internal.ast.QuerySyntaxException
import org.springframework.context.ApplicationContext

/**
 *
 */
@Slf4j
class DynamicFinder {

    def domainClass
    def query
    def tableIdentifier
    def criteria
    def pagingAndSortParams


    public DynamicFinder(domainClass, query, tableIdentifier) {
        this.domainClass = domainClass
        this.query = query
        this.tableIdentifier = tableIdentifier
    }


    public static Map getCriteriaParamsFromParams(data) {
        Map params = new HashMap()
        Set keys = data.keySet()
        if (keys.size() > 0) {
            keys.each { key ->
                if (data[key] instanceof CriteriaParam) {
                    params.put(key, data[key])
                } else {
                    CriteriaParam criteriaParam = new CriteriaParam()
                    criteriaParam.paramKey = key
                    criteriaParam.data = data[key]
                    params.put(key, criteriaParam)
                }

            }
        }
        return params
    }


    public static Map getParamsFromCriteriaParams(data) {
        Map params = new HashMap();
        Set keys = data.keySet()
        if (keys.size() > 0) {
            keys.each { key ->
                if (data[key] instanceof CriteriaParam) {
                    List paramsList = new ArrayList();
                    if (data[key].data instanceof List) {
                        List criteriaParams = data[key].data
                        int numberOfCriteriaParams = criteriaParams.size()
                        for (int counter = 0; counter < numberOfCriteriaParams; counter++) {
                            paramsList.add(criteriaParams.get(counter).data)
                        }
                        params.put(key, paramsList)
                    } else {
                        params.put(key, data[key].data)
                    }

                } else {
                    params.put(key, data[key].data)
                }

            }
        }
        return params
    }


    public def find(filterData, pagingAndSortParams) {

        def filterDataClone = filterData.clone()
        filterDataClone.params = getCriteriaParamsFromParams(filterData.params)

        String queryString = QueryBuilder.buildQuery(query.flattenString(), tableIdentifier, filterDataClone, pagingAndSortParams, domainClass)

        Map params = getParamsFromCriteriaParams(filterDataClone.params)

        try {
            def list = domainClass.findAll(queryString, params, pagingAndSortParams)
            return list
        }  catch(Exception e){
            if (e?.cause instanceof QuerySyntaxException) {
                log.error "Error message: " + e.stackTrace.toString()
                def message = MessageHelper.message("net.hedtech.banner.query.DynamicFinder.QuerySyntaxException")
                throw new ApplicationException(DynamicFinder, message);

            } else {
                throw e
            }
        }

    }


    public def count(filterData) {
        def filterDataClone = filterData.clone()
        filterDataClone.params = getCriteriaParamsFromParams(filterData.params)

        String queryString = QueryBuilder.buildCountQuery(query.flattenString(), tableIdentifier, filterDataClone)

        Map params = getParamsFromCriteriaParams(filterDataClone.params)

        def returnListCount = domainClass.executeQuery(queryString, params)

        return returnListCount[0]
    }


    public static def fetchAll(domainClass, query, tableIdentifier, filterData, pagingAndSortParams) {
        String queryString = QueryBuilder.buildQuery(query.flattenString(), "a", filterData, pagingAndSortParams, domainClass)

        try {
            def list = domainClass.findAll(queryString, filterData.params, pagingAndSortParams)
            return list
        }  catch(Exception e){
            if (e?.cause instanceof QuerySyntaxException) {
                log.error "Error message: " + e.stackTrace
                def message = MessageHelper.message("net.hedtech.banner.query.DynamicFinder.QuerySyntaxException")
                throw new ApplicationException(DynamicFinder, message);

            } else {
                throw e
            }
        }

    }


    public static def countAll(domainClass, query, tableIdentifier, filterData) {
        String queryString = QueryBuilder.buildCountQuery(query.flattenString(), "a", filterData.criteria)

        def returnListCount = domainClass.executeQuery(queryString, filterData.params)

        return returnListCount[0]
    }


    public static ApplicationContext getApplicationContext() {
        return Holders.getGrailsApplication().getMainContext()
    }
}

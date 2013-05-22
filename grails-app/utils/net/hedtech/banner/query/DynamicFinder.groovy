package net.hedtech.banner.query

import org.codehaus.groovy.grails.web.context.ServletContextHolder
import org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes
import org.springframework.context.ApplicationContext
import net.hedtech.banner.query.criteria.CriteriaParam

/*******************************************************************************
Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
*******************************************************************************/ 
class DynamicFinder {

    def domainClass
    def query
    def tableIdentifier
    def criteria
    def pagingAndSortParams

    public DynamicFinder( domainClass, query, tableIdentifier) {
		this.domainClass = domainClass
		this.query = query
		this.tableIdentifier = tableIdentifier
	}

    public static Map getCriteriaParamsFromParams(data) {
        Map params = new HashMap();
        Set keys = data.keySet()
        if(keys.size() > 0){
            keys.each{ key ->
                if(data[key] instanceof CriteriaParam) {
                    params.put(key, data[key])
                }
                else {
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
       if(keys.size() > 0){
           keys.each{ key ->
               if(data[key] instanceof CriteriaParam) {
                    List paramsList = new ArrayList();
                    if(data[key].data instanceof List) {
                        List criteriaParams = data[key].data
                        int numberOfCriteriaParams = criteriaParams.size()
                        for(int counter = 0; counter < numberOfCriteriaParams; counter++) {
                            paramsList.add(criteriaParams.get(counter).data)
                        }
                        params.put(key, paramsList)
                    }
                    else {
                        params.put(key, data[key].data)
                    }

               }
               else {
                  params.put(key, data[key].data)
               }

           }
       }
       return params
   }


   public def find( filterData, pagingAndSortParams ){
       def filterDataClone = filterData.clone()
       filterDataClone.params = getCriteriaParamsFromParams(filterData.params)

        def queryString =  QueryBuilder.buildQuery( query.flattenString(), tableIdentifier, filterDataClone ,pagingAndSortParams )

        Map params = getParamsFromCriteriaParams(filterDataClone.params)

        def list =  domainClass.findAll( queryString, params, pagingAndSortParams )

        return list
    }

    public def count ( filterData ) {
        def filterDataClone = filterData.clone()
        filterDataClone.params = getCriteriaParamsFromParams(filterData.params)

        def queryString =  QueryBuilder.buildCountQuery( query.flattenString(), tableIdentifier, filterDataClone )

        Map params = getParamsFromCriteriaParams(filterDataClone.params)

        def returnListCount = domainClass.executeQuery( queryString, params )

        return returnListCount[0]
    }

    public static def fetchAll ( domainClass, query, tableIdentifier, filterData, pagingAndSortParams) {
        def queryString =  QueryBuilder.buildQuery( query.flattenString(), "a", filterData.criteria,pagingAndSortParams )

        Map params = getParamsFromCriteriaParams(filterData.params)

        def list =  domainClass.findAll( queryString, filterData.params, pagingAndSortParams )

        return list
    }

    public static def countAll (domainClass, query, tableIdentifier, filterData) {
        def queryString =  QueryBuilder.buildCountQuery( query.flattenString(), "a", filterData.criteria )

        def returnListCount = domainClass.executeQuery( queryString, filterData.params )

        return returnListCount[0]
    }

    public static ApplicationContext getApplicationContext() {
        return (ApplicationContext) ServletContextHolder.getServletContext().getAttribute(GrailsApplicationAttributes.APPLICATION_CONTEXT);
    }
}

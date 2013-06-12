package net.hedtech.banner.query

import org.codehaus.groovy.grails.web.context.ServletContextHolder
import org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes
import org.springframework.context.ApplicationContext

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

    public def find( filterData, pagingAndSortParams ){
        def queryString =  QueryBuilder.buildQuery( query.flattenString(), tableIdentifier, filterData.criteria ,pagingAndSortParams )

        def list =  domainClass.findAll( queryString, filterData.params, pagingAndSortParams )

        return list
    }

    public def count ( filterData ) {
        def queryString =  QueryBuilder.buildCountQuery( query.flattenString(), tableIdentifier, filterData.criteria )

        def returnListCount = domainClass.executeQuery( queryString, filterData.params )

        return returnListCount[0]
    }

    public static def fetchAll ( domainClass, query, tableIdentifier, filterData, pagingAndSortParams) {
        def queryString =  QueryBuilder.buildQuery( query.flattenString(), "a", filterData.criteria,pagingAndSortParams )

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

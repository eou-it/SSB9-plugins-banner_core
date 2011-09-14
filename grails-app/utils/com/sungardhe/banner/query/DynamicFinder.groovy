package com.sungardhe.banner.query

import org.springframework.context.ApplicationContext
import org.codehaus.groovy.grails.web.context.ServletContextHolder
import org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes

/*********************************************************************************
 Copyright 2009-2011 SunGard Higher Education. All Rights Reserved.
 This copyrighted software contains confidential and proprietary information of 
 SunGard Higher Education and its subsidiaries. Any use of this software is limited 
 solely to SunGard Higher Education licensees, and is further subject to the terms 
 and conditions of one or more written license agreements between SunGard Higher 
 Education and the licensee in question. SunGard is either a registered trademark or
 trademark of SunGard Data Systems in the U.S.A. and/or other regions and/or countries.
 Banner and Luminis are either registered trademarks or trademarks of SunGard Higher 
 Education in the U.S.A. and/or other regions and/or countries.
 **********************************************************************************/
class DynamicFinder {

    def domainClass
    def query
    def tableIdentifier
    def requiredFields
    def queryableFields
    def criteria
    def pagingAndSortParams
    def headerMap

    public DynamicFinder( domainClass, query, tableIdentifier, requiredFields, queryableFields, headerMap ) {
		this.domainClass = domainClass
		this.query = query
		this.tableIdentifier = tableIdentifier
		this.requiredFields = requiredFields
		this.queryableFields = queryableFields
		this.headerMap = headerMap
	}

    public def find( criteria, pagingAndSortParams ){
       // This is a convenience method to take a more readable multiline string and collapse it down to a one line string
        String.metaClass.flattenString = {
            return delegate.replace( "\n", "" ).replaceAll( /  */, " " )
        }

        def sortColumnName
        if(pagingAndSortParams.sortColumn){
            sortColumnName = headerMap.get(pagingAndSortParams.sortColumn)
        }
        pagingAndSortParams.put("sortColumn",sortColumnName)
        def queryString =  com.sungardhe.banner.query.QueryBuilder.buildQuery( query.flattenString(), tableIdentifier, requiredFields, queryableFields ,criteria,pagingAndSortParams )

        def list =  domainClass.findAll( queryString, criteria, pagingAndSortParams )

        return list
    }

    public def count ( criteria ) {
        String.metaClass.flattenString = {
            return delegate.replace( "\n", "" ).replaceAll( /  */, " " )
        }

        def queryString =  com.sungardhe.banner.query.QueryBuilder.buildCountQuery( query.flattenString(), tableIdentifier, requiredFields, queryableFields ,criteria )
        def returnListCount = domainClass.executeQuery( queryString, criteria )

        return returnListCount[0]
    }

    public static def fetchAll ( domainClass, query, tableIdentifier, requiredFields, queryableFields, criteria, pagingAndSortParams, headerMap) {
        // This is a convenience method to take a more readable multiline string and collapse it down to a one line string
              String.metaClass.flattenString = {
            return delegate.replace( "\n", "" ).replaceAll( /  */, " " )
        }

        def sortColumnName
        if(pagingAndSortParams.sortColumn){
            sortColumnName = headerMap.get(pagingAndSortParams.sortColumn)
        }
        pagingAndSortParams.put("sortColumn",sortColumnName)
        def queryString =  com.sungardhe.banner.query.QueryBuilder.buildQuery( query.flattenString(), "a", requiredFields, queryableFields ,criteria,pagingAndSortParams )

        def list =  domainClass.findAll( queryString, criteria, pagingAndSortParams )

        return list
    }

    public static def countAll (domainClass, query, tableIdentifier, requiredFields, queryableFields, criteria) {
              String.metaClass.flattenString = {
            return delegate.replace( "\n", "" ).replaceAll( /  */, " " )
        }

        def queryString =  com.sungardhe.banner.query.QueryBuilder.buildCountQuery( query.flattenString(), "a", requiredFields, queryableFields ,criteria )
        def returnListCount = domainClass.executeQuery( queryString, criteria )

        return returnListCount[0]
    }

    public static ApplicationContext getApplicationContext() {
        return (ApplicationContext) ServletContextHolder.getServletContext().getAttribute(GrailsApplicationAttributes.APPLICATION_CONTEXT);
    }
}

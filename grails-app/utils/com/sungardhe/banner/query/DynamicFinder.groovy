package com.sungardhe.banner.query

import org.codehaus.groovy.grails.web.context.ServletContextHolder
import org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes
import org.springframework.context.ApplicationContext

/*********************************************************************************
 Copyright 2009-2011 SunGard Higher Education. All Rights Reserved.
 This copyrighted software contains confidential and proprietary information of 
 SunGard Higher Education and its subsidiaries. Any use of this software is limited 
 solely to SunGard Higher Education licensees, and is further subject to the terms 
 and conditions of one or more written license agreements between SunGard Higher 
 Education and the licensee in question. SunGard, Banner and Luminis are either 
 registered trademarks or trademarks of SunGard Higher Education in the U.S.A. 
 and/or other regions and/or countries.
 **********************************************************************************/
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
       // This is a convenience method to take a more readable multiline string and collapse it down to a one line string
        String.metaClass.flattenString = {
            return delegate.replace( "\n", "" ).replaceAll( /  */, " " )
        }

        def queryString =  QueryBuilder.buildQuery( query.flattenString(), tableIdentifier, filterData.criteria ,pagingAndSortParams )

        def list =  domainClass.findAll( queryString, filterData.params, pagingAndSortParams )

        return list
    }

    public def count ( filterData ) {
        String.metaClass.flattenString = {
            return delegate.replace( "\n", "" ).replaceAll( /  */, " " )
        }

        def queryString =  QueryBuilder.buildCountQuery( query.flattenString(), tableIdentifier, filterData.criteria )

        def returnListCount = domainClass.executeQuery( queryString, filterData.params )

        return returnListCount[0]
    }

    public static def fetchAll ( domainClass, query, tableIdentifier, filterData, pagingAndSortParams) {
        // This is a convenience method to take a more readable multiline string and collapse it down to a one line string
        String.metaClass.flattenString = {
            return delegate.replace( "\n", "" ).replaceAll( /  */, " " )
        }

        def queryString =  QueryBuilder.buildQuery( query.flattenString(), "a", filterData.criteria,pagingAndSortParams )

        def list =  domainClass.findAll( queryString, filterData.params, pagingAndSortParams )

        return list
    }

    public static def countAll (domainClass, query, tableIdentifier, filterData) {
        String.metaClass.flattenString = {
            return delegate.replace( "\n", "" ).replaceAll( /  */, " " )
        }

        def queryString =  QueryBuilder.buildCountQuery( query.flattenString(), "a", filterData.criteria )

        def returnListCount = domainClass.executeQuery( queryString, filterData.params )

        return returnListCount[0]
    }

    public static ApplicationContext getApplicationContext() {
        return (ApplicationContext) ServletContextHolder.getServletContext().getAttribute(GrailsApplicationAttributes.APPLICATION_CONTEXT);
    }
}

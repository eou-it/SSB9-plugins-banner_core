package com.sungardhe.banner.query

import org.springframework.context.ApplicationContext
import org.codehaus.groovy.grails.web.context.ServletContextHolder
import org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes

/** *****************************************************************************
 © 2010 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
class DynamicFinder {
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

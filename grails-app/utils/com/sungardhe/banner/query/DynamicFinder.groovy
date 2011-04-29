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
    public static def fetchAll ( domainName, query, tableIdentifier, requiredFields, queryableFields, criteria, pagingAndSortParams, headerMap) {
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

        def clazzObject = getApplicationContext().getBean("grailsApplication").classLoader.loadClass(domainName)
        def list =  clazzObject.findAll( queryString, criteria, pagingAndSortParams )

        return list
    }

    public static ApplicationContext getApplicationContext() {
        return (ApplicationContext) ServletContextHolder.getServletContext().getAttribute(GrailsApplicationAttributes.APPLICATION_CONTEXT);
    }
}

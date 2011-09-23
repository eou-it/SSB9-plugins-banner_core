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
package com.sungardhe.banner.query

class QueryBuilder {

    public static def buildQuery = { query, tableIdentifier,  criteria,pagingAndSortParams ->

        def returnQuery = query

        if ( ! (returnQuery as String).toUpperCase().contains (" WHERE ") ) {
            returnQuery += " WHERE 1=1 "
        }

        criteria.each {
            returnQuery += CriteriaOperatorFactory.operators."${it.operator}"?.dynamicQuery(tableIdentifier,it)
        }

        if( pagingAndSortParams.sortColumn && (pagingAndSortParams.sortDirection!=null) ){
             returnQuery += "order by  $tableIdentifier.$pagingAndSortParams.sortColumn  $pagingAndSortParams.sortDirection"
        } else if(pagingAndSortParams.sortColumn){
              // we are not using tableIdentifier since there is only one table identifier
              // and no need to add table identifier
              // and there is not provision for multiple table identifiers as of now.
              String sort =  (pagingAndSortParams.sortColumn as String).replaceAll("@@table@@", "$tableIdentifier." )
              returnQuery += "order by $sort "
        }

        return returnQuery
    }


    public static def buildCountQuery = { query, tableIdentifier, criteria ->

        def returnQuery = query

        if ( ! (returnQuery as String).toUpperCase().contains (" WHERE ") ) {
            returnQuery += " WHERE 1=1 "
        }
         criteria.each {
             returnQuery += CriteriaOperatorFactory.operators."${it.operator}"?.dynamicQuery(tableIdentifier,it)
        }

        returnQuery = "select count(*) ${returnQuery}"
        return returnQuery
    }
}

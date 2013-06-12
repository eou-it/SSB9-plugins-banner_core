/*******************************************************************************
Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
*******************************************************************************/
package net.hedtech.banner.query

class QueryBuilder {

    public static def buildQuery = { query, tableIdentifier, criteria, pagingAndSortParams ->

        def returnQuery = query

        if ( ! (returnQuery as String).toUpperCase().contains (" WHERE ") ) {
            returnQuery += " WHERE 1=1 "
        }

        criteria.each {
            returnQuery += CriteriaOperatorFactory.operators."${it.operator}"?.dynamicQuery(tableIdentifier,it)
        }

        if (pagingAndSortParams.sortCriteria && pagingAndSortParams.sortCriteria instanceof Collection) {
            def sortParams = pagingAndSortParams.sortCriteria.collect { "$tableIdentifier.$it.sortColumn  $it.sortDirection" }

            returnQuery += "order by " + sortParams.join(", ")
        } else {
            if( pagingAndSortParams.sortColumn && (pagingAndSortParams.sortDirection!=null) ){
                 returnQuery += "order by  $tableIdentifier.$pagingAndSortParams.sortColumn  $pagingAndSortParams.sortDirection"
            } else if(pagingAndSortParams.sortColumn){
                  // we are not using tableIdentifier since there is only one table identifier
                  // and no need to add table identifier
                  // and there is not provision for multiple table identifiers as of now.
                  String sort =  (pagingAndSortParams.sortColumn as String).replaceAll("@@table@@", "$tableIdentifier." )
                  returnQuery += "order by $sort "
            }
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

        returnQuery = "select count(${tableIdentifier}.id) ${returnQuery}"
        return returnQuery
    }

    public static def dynamicGroupby = {tableIdentifier, params ->
        String dynaGroup = ""
        params?.each {key, val ->
            dynaGroup += ", ${tableIdentifier}.${key}"
        }
        return dynaGroup
    }
}

/*******************************************************************************
Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
*******************************************************************************/
package net.hedtech.banner.query

import net.hedtech.banner.query.criteria.Query
import net.hedtech.banner.query.operators.CriteriaOperator
import net.hedtech.banner.query.CriteriaOperatorFactory
import net.hedtech.banner.query.criteria.CriteriaData
import net.hedtech.banner.query.criteria.CriteriaParam
import net.hedtech.banner.query.operators.Operators

class QueryBuilder {
    private static boolean isTimeSet(Object date) {
        boolean isTimeSet = false;

       // if(date instanceof Date) {
            int hours = date.getHours()
            int minutes = date.getMinutes()
            int seconds = date.getSeconds()

            if (hours != 0 ||  minutes != 0 || seconds != 0) {
                isTimeSet = true
            }
        //}
        return isTimeSet
    }

    public static getCriteriaParams(criteria, params, operator) {
        List<CriteriaParam> criteriaParams = new ArrayList<CriteriaParam>();
        def data = params.get(criteria.key)
        criteriaParams.add(getCriteriaParam(criteria.key, data))

        if(operator == Operators.BETWEEN) {
            data = params.get(criteria.key + "_and")
            criteriaParams.add(getCriteriaParam(criteria.key, data))
        }
        return criteriaParams
    }

    private static getCriteriaParam(String paramKey, Object data) {
        if(data instanceof CriteriaParam) {
            return data
        }
        else {
            CriteriaParam param = new CriteriaParam();
            param.paramKey = paramKey;
            param.data = data
            return param;
        }
    }

    public static def buildQuery = { query, tableIdentifier, filterData, pagingAndSortParams ->

        def criteria = filterData.criteria
        def params = filterData.params

        def returnQuery = query

        if ( ! (returnQuery as String).toUpperCase().contains (" WHERE ") ) {
            returnQuery += " WHERE 1=1 "
        }

        //New
        Query newQuery = Query.createQuery(returnQuery);

        criteria.each {

            def operator = it.operator
            //Changed
            //returnQuery += CriteriaOperatorFactory.operators."${operator}"?.dynamicQuery(tableIdentifier,it)

            //CriteriaParam param = new CriteriaParam();
            //param.data = data
            //param.addAttribute("containsTime", true)

            CriteriaData criteriaData = new CriteriaData()
            criteriaData.tableAlias = tableIdentifier;
            criteriaData.tableBindingAttribute = it.binding
            criteriaData.paramKey = it.key
            //criteriaData.addParam(param)
            criteriaData.addParams(getCriteriaParams(it, params, operator))

            CriteriaOperator criteriaOperator = CriteriaOperatorFactory.getCriteriaOperator("${operator}")
            newQuery = Query.and(newQuery, criteriaOperator.getQuery(criteriaData));
        }

        //New
        //returnQuery = newQuery.toString();

        if (pagingAndSortParams.sortCriteria && pagingAndSortParams.sortCriteria instanceof Collection) {
            def sortParams = pagingAndSortParams.sortCriteria.collect { "$tableIdentifier.$it.sortColumn  $it.sortDirection" }

            //returnQuery += "order by " + sortParams.join(", ")
            newQuery.orderBy(sortParams.join(", "))
        } else {
            if( pagingAndSortParams.sortColumn && (pagingAndSortParams.sortDirection!=null) ){
                 //returnQuery += "order by  $tableIdentifier.$pagingAndSortParams.sortColumn  $pagingAndSortParams.sortDirection"
                newQuery.orderBy("$tableIdentifier.$pagingAndSortParams.sortColumn  $pagingAndSortParams.sortDirection")
            } else if(pagingAndSortParams.sortColumn){
                  // we are not using tableIdentifier since there is only one table identifier
                  // and no need to add table identifier
                  // and there is not provision for multiple table identifiers as of now.
                  String sort =  (pagingAndSortParams.sortColumn as String).replaceAll("@@table@@", "$tableIdentifier." )
                  //returnQuery += "order by $sort "
                  newQuery.orderBy(sort)
            }
        }
        returnQuery = newQuery.toString()
        return returnQuery
    }


    public static def buildCountQuery = { query, tableIdentifier, filterData ->

        def criteria = filterData.criteria
        def params = filterData.params

        def returnQuery = query

        if ( ! (returnQuery as String).toUpperCase().contains (" WHERE ") ) {
            returnQuery += " WHERE 1=1 "
        }

        //New
         Query newQuery = Query.createQuery(returnQuery);

         criteria.each {
             def operator = it.operator

             CriteriaData criteriaData = new CriteriaData()
             criteriaData.tableAlias = tableIdentifier;
             criteriaData.tableBindingAttribute = it.binding
             criteriaData.paramKey = it.key
             criteriaData.addParams(getCriteriaParams(it, params, operator))

             CriteriaOperator criteriaOperator = CriteriaOperatorFactory.getCriteriaOperator("${operator}")
             newQuery = Query.and(newQuery, criteriaOperator.getQuery(criteriaData));
        }

        //returnQuery = "select count(${tableIdentifier}.id) ${returnQuery}"
        returnQuery = "select count(${tableIdentifier}.id) " + newQuery.toString()
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

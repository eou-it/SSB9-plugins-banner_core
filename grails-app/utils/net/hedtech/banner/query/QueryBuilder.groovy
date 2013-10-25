/*******************************************************************************
 Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.query

import net.hedtech.banner.query.criteria.CriteriaData
import net.hedtech.banner.query.criteria.CriteriaParam
import net.hedtech.banner.query.criteria.Query
import net.hedtech.banner.query.operators.CriteriaOperator
import net.hedtech.banner.query.operators.Operators

class QueryBuilder {
    private static boolean isTimeSet(Object date) {
        boolean isTimeSet = false;

        // if(date instanceof Date) {
        int hours = date.getHours()
        int minutes = date.getMinutes()
        int seconds = date.getSeconds()

        if (hours != 0 || minutes != 0 || seconds != 0) {
            isTimeSet = true
        }
        //}
        return isTimeSet
    }


    public static getCriteriaParams(criteria, params, operator) {
        List<CriteriaParam> criteriaParams = new ArrayList<CriteriaParam>();
        def data = params.get(criteria.key)
        criteriaParams.add(getCriteriaParam(criteria.key, data))

        if (operator == Operators.BETWEEN) {
            data = params.get(criteria.key + "_and")
            criteriaParams.add(getCriteriaParam(criteria.key, data))
        }
        return criteriaParams
    }


    private static getCriteriaParam(String paramKey, Object data) {
        if (data instanceof CriteriaParam) {
            return data
        } else {
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

        if (!(returnQuery as String).toUpperCase().contains(" WHERE ")) {
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
            if (pagingAndSortParams.sortColumn && (pagingAndSortParams.sortDirection != null)) {
                //returnQuery += "order by  $tableIdentifier.$pagingAndSortParams.sortColumn  $pagingAndSortParams.sortDirection"
                newQuery.orderBy("$tableIdentifier.$pagingAndSortParams.sortColumn  $pagingAndSortParams.sortDirection")
            } else if (pagingAndSortParams.sortColumn) {
                // we are not using tableIdentifier since there is only one table identifier
                // and no need to add table identifier
                // and there is not provision for multiple table identifiers as of now.
                String sort = (pagingAndSortParams.sortColumn as String).replaceAll("@@table@@", "$tableIdentifier.")
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

        if (!(returnQuery as String).toUpperCase().contains(" WHERE ")) {
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

    public static def dynamicGroupby = { tableIdentifier, params ->
        String dynaGroup = ""
        params?.each { key, val ->
            dynaGroup += ", ${tableIdentifier}.${key}"
        }
        return dynaGroup
    }

    /**
     * prepare filter map for dynamic finder given map of criteria from zk page or restful API interface
     * @param map
     * @return map of filters ready for dynamic finder
     */
    public static def getFilterData(Map map) {
        def paramsMap = [:]
        def criteriaMap = []
        def pagingAndSortParams = [:]
        def filtered = createFilters(map)
        // restful operators confined to eq, contains, equals
        def operatorConversions = ["gt": Operators.GREATER_THAN, "equals": Operators.EQUALS,
                "eq": Operators.EQUALS, "lt": Operators.LESS_THAN, "contains": Operators.CONTAINS]

        // Prepare each restfulApi filter (putting it into maps for DynamicFinder)
        filtered.each {
            paramsMap.put(it["field"], it["value"])
            criteriaMap.add([key: it["field"], binding: it["field"], operator: operatorConversions[it["operator"]]])
        }

        // If criteria are passed in with the correct format already, just copy them.
        if (map?.containsKey("params")) paramsMap.putAll(map.params)
        if (map?.containsKey("criteria")) criteriaMap.addAll(map.criteria)
        // pull out the pagination criteria

        if (map?.containsKey("max")) pagingAndSortParams.put("max", map["max"].toInteger())
        if (map?.containsKey("offset")) pagingAndSortParams.put("offset", map["offset"].toInteger())
        if (map?.containsKey("sort")) pagingAndSortParams.put("sort", map["sort"])
        if (map?.containsKey("pagingAndSortParams")) pagingAndSortParams.putAll(map.pagingAndSortParams)

        return [params: paramsMap, criteria: criteriaMap, pagingAndSortParams: pagingAndSortParams]
    }


    public static def createFilters(def map) {
        def filterRE = /filter\[([0-9]+)\]\[(field|operator|value|type)\]=(.*)/
        def filters = [:]
        def matcher

        map.each {
            if (it.key.startsWith('filter')) {
                matcher = (it =~ filterRE)
                if (matcher.count) {
                    // Regex matches are broken up into parts for ease of understanding
                    // toString is called to convert GStrings to strings, which is important to note.
                    def filterNumber = "${matcher[0][1]}".toString()
                    def key = "${matcher[0][2]}".toString()
                    def value = "${matcher[0][3]}".toString()
                    if (!filters.containsKey(filterNumber)) filters.put(filterNumber, [:])
                    filters[filterNumber]?.put(key, value)
                }
            }
        }

        return filters.values()
    }
}

/*******************************************************************************
 Copyright 2009-2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.query

import grails.util.Holders
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.i18n.MessageHelper
import net.hedtech.banner.query.criteria.CriteriaData
import net.hedtech.banner.query.criteria.CriteriaParam
import net.hedtech.banner.query.criteria.Query
import net.hedtech.banner.query.operators.CriteriaOperator
import net.hedtech.banner.query.operators.Operators
import org.grails.core.exceptions.InvalidPropertyException
import org.grails.datastore.mapping.model.types.Association

/**
 *
 */
class QueryBuilder {
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

    public static boolean validateSortColumns(def domainClass, String sortColumnName) {
        def sortColumnArray = sortColumnName.tokenize(",")
        boolean validateResult
            for (int i=0;i<sortColumnArray.size();i++){
                validateResult = validateSortColumnName(domainClass, sortColumnArray[i].trim())
            }
        return validateResult
    }

    public static boolean validateSortColumnName(def domainClass, String sortColumnName) {
        boolean isValidColumn = true;

        def grailsApplication = Holders.grailsApplication
        def domainClassProperties =grailsApplication.mappingContext.getPersistentEntity(domainClass.name)
        def domainArray = sortColumnName.tokenize(".")
        def relDomainClass, relDomainSortColumnName, relationalDomainClassType
        int splitIndex
        try{
            if(domainArray.size()==1){
                if(null == domainClassProperties.getPropertyByName(sortColumnName)){
                    throw new InvalidPropertyException(MessageHelper.message("net.hedtech.banner.query.DynamicFinder.QuerySyntaxException"))
                } else{
                    return isValidColumn
                }
            }
            else {
                for (int i=0;i<domainArray.size()-1;i++)
                {   splitIndex = sortColumnName.indexOf(".")
                    relDomainClass = sortColumnName.substring(0,splitIndex)
                    if(domainClassProperties.getPropertyByName(relDomainClass) instanceof Association){
                        relDomainSortColumnName = sortColumnName.substring(splitIndex+1)
                        relationalDomainClassType = domainClassProperties.getPropertyByName(relDomainClass).getType()
                        domainClassProperties =grailsApplication.mappingContext.getPersistentEntity(relationalDomainClassType.name)
                        sortColumnName=relDomainSortColumnName
                    }
                        else{
                        throw new InvalidPropertyException(MessageHelper.message("net.hedtech.banner.query.DynamicFinder.QuerySyntaxException"))
                    }
                }
                if(domainClassProperties.getPropertyByName(domainArray.last()))
                    return isValidColumn
            }
        } catch (InvalidPropertyException e) {
            def message = MessageHelper.message("net.hedtech.banner.query.DynamicFinder.QuerySyntaxException")
            throw new ApplicationException(QueryBuilder, message)
        }
    }

    public static validateSortOrder(String sortOrder) {
        if(sortOrder?.trim()?.toUpperCase()in['ASC','DESC',''])
            return sortOrder
        else{
            def message = MessageHelper.message("net.hedtech.banner.query.DynamicFinder.QuerySyntaxException")
            throw new ApplicationException(QueryBuilder, message);
        }
    }

    public static def buildQuery = { query, tableIdentifier, filterData, pagingAndSortParams, domainClass ->

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
            //criteriaData.addParam(param)
            criteriaData.addParams(getCriteriaParams(it, params, operator))

            CriteriaOperator criteriaOperator = CriteriaOperatorFactory.getCriteriaOperator("${operator}")
            newQuery = Query.and(newQuery, criteriaOperator.getQuery(criteriaData));
        }

        if (pagingAndSortParams.sortCriteria && pagingAndSortParams.sortCriteria instanceof Collection) {
            def sortParams = pagingAndSortParams.sortCriteria.collect { sortItem ->
                if (validateSortColumns(domainClass,sortItem.sortColumn) && validateSortOrder(sortItem.sortDirection)) {
                    "$tableIdentifier.$sortItem.sortColumn  $sortItem.sortDirection"
                }
            }
            newQuery.orderBy(sortParams.join(", "))
        } else {
            def sortColumn = pagingAndSortParams.sortColumn
            if (pagingAndSortParams.sortColumn && (pagingAndSortParams.sortDirection != null)) {
                validateSortColumns(domainClass,pagingAndSortParams.sortColumn)
                def sortDirection = validateSortOrder(pagingAndSortParams.sortDirection)
                def sortParams = "${tableIdentifier}.${sortColumn} ${sortDirection}"
                newQuery.orderBy(sortParams)
            } else if (pagingAndSortParams.sortColumn) {
                // we are not using tableIdentifier since there is only one table identifier
                // and no need to add table identifier
                // and there is not provision for multiple table identifiers as of now.
                validateSortColumns(domainClass,pagingAndSortParams.sortColumn)
                String sort = (sortColumn as String).replaceAll("@@table@@", "$tableIdentifier.")
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

        def hqlBuilderOperators = ["eq": Operators.EQUALS, "lt": Operators.LESS_THAN, "gt": Operators.GREATER_THAN, "le": Operators.LESS_THAN_EQUALS, "ge": Operators.GREATER_THAN_EQUALS]

        // Prepare each restfulApi filter (putting it into maps for DynamicFinder)
        filtered.each {
            if (hqlBuilderOperators.containsKey(it["operator"])) {
                // For backward compatibility convert old HQLBuilder operator to DynamicFinder operator
                it["operator"] = hqlBuilderOperators[it["operator"]]
            }
            if (it.containsKey("type")) {
                // URL parameter "filter[index][type]" exists.  Either "numeric" or "date".
                if (it["type"] == "num" || it["type"] == "number") {
                    it["value"] = it["value"].toLong()
                } else if (it["type"] == "date") {
                    it["value"] = parseDate(map, it)
                }
            }

            if (it["operator"] == "contains" && !(it["value"].contains("%"))) it["value"] = "%${it["value"]}%"
            else if (it["operator"] == "startswith" && !(it["value"].contains("%"))) it["value"] = "${it["value"]}%"

            paramsMap.put(it["field"], it["value"])
            criteriaMap.add([key: it["field"], binding: it["field"], operator: it["operator"]])
        }

        // If criteria are passed in with the correct format already, just copy them.
        if (map?.containsKey("params")) paramsMap.putAll(map.params)
        if (map?.containsKey("criteria")) criteriaMap.addAll(map.criteria)

        // pull out the pagination criteria
        if (map?.containsKey("max")) pagingAndSortParams.put("max", map["max"].toInteger())
        if (map?.containsKey("offset")) pagingAndSortParams.put("offset", map["offset"].toInteger())
        // sortColumnName
        if (map?.containsKey("sort")) {
            pagingAndSortParams.put("sortColumn", map["sort"])
            if (map?.containsKey("order")) pagingAndSortParams.put("sortDirection", map["order"])
        } else if (map?.containsKey("sortCriteria") && map["sortCriteria"] instanceof Collection) {
            pagingAndSortParams.put("sortCriteria", map["sortCriteria"])
        }
        if (map?.containsKey("pagingAndSortParams")) pagingAndSortParams.putAll(map.pagingAndSortParams)

        return [params: paramsMap, criteria: criteriaMap, pagingAndSortParams: pagingAndSortParams]
    }


    public static def createFilters(def map) {
        def filterRE = /filter\[([0-9]+)\]\[(field|operator|value|type)\]=(.*)/
        def filters = [:]
        def matcher
        // find if the operator is contains or startswith

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


    private static Date parseDate(def params, filter) {
        if (filter.value == null) return null
        //see if its numeric, if so, treat as millis since Epoch
        try {
            Long l = Long.valueOf(filter.value)
            return new Date(l)
        } catch (Exception e) {
            //can't parse as a long
        }
        //try to parse as ISO 8601
        try {
            def cal = javax.xml.datatype.DatatypeFactory.newInstance().newXMLGregorianCalendar(filter.value)
            return cal.toGregorianCalendar().getTime()
        } catch (Exception e) {
            //can't parse as ISO 8601
        }
        //wasn't able to parse as a date
        throw new Exception(params.pluralizedResourceName + filter + "exception")//BadDateFilterException(params.pluralizedResourceName,filter)
    }
}

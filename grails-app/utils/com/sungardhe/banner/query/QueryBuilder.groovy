package com.sungardhe.banner.query

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
class QueryBuilder {

    public static def buildQuery = { query, tableIdentifier, requiredFields, queryableFields, criteria,pagingAndSortParams ->
        // Ensure that the requird fields are indeed present.
        requiredFields.each {
            if (criteria[it] == null) {
                throw new Exception("'$it' is required and not found in $criteria")
            }
        }

        // Validate that the criteria contains only valid codes
        def validKeys = requiredFields + queryableFields.keySet()
        criteria.keySet().each {
            if (!validKeys.contains(it)) {
                throw new Exception("'$it' is not a valid field key.  Valid keys are $validKeys.")
            }
        }

        def returnQuery = query

        queryableFields.each {
            if (criteria[it.key]) {
                if (it.value instanceof String) {
                    returnQuery += " and lower($tableIdentifier.${it.value}) like lower(:${it.key})"
                }
                else {
                    returnQuery += " and ("
                    it.value.eachWithIndex { field, index ->
                        if (index != 0) {
                            returnQuery += " or "
                        }

                        returnQuery += "lower($tableIdentifier.${field.value}) like lower(:${it.key})"
                    }

                    returnQuery += ")"
                }
            }
        }
        if( pagingAndSortParams.sortColumn && (pagingAndSortParams.sortDirection!=null) ){
             returnQuery += "order by  $tableIdentifier.$pagingAndSortParams.sortColumn  $pagingAndSortParams.sortDirection"
        }

        return returnQuery
    }


    public static def buildCountQuery = { query, tableIdentifier, requiredFields, queryableFields, criteria ->
        // Ensure that the requird fields are indeed present.
        requiredFields.each {
            if (criteria[it] == null) {
                throw new Exception("'$it' is required and not found in $criteria")
            }
        }

        // Validate that the criteria contains only valid codes
        def validKeys = requiredFields + queryableFields.keySet()
        criteria.keySet().each {
            if (!validKeys.contains(it)) {
                throw new Exception("'$it' is not a valid field key.  Valid keys are $validKeys.")
            }
        }

        def returnQuery = query

        queryableFields.each {
            if (criteria[it.key]) {
                if (it.value instanceof String) {
                    returnQuery += " and lower($tableIdentifier.${it.value}) like lower(:${it.key})"
                }
                else {
                    returnQuery += " and ("
                    it.value.eachWithIndex { field, index ->
                        if (index != 0) {
                            returnQuery += " or "
                        }

                        returnQuery += "lower($tableIdentifier.${field.value}) like lower(:${it.key})"
                    }

                    returnQuery += ")"
                }
            }
        }
        /*if( pagingAndSortParams.sortColumn && (pagingAndSortParams.sortDirection!=null) ){
             returnQuery += "order by  $tableIdentifier.$pagingAndSortParams.sortColumn  $pagingAndSortParams.sortDirection"
        }*/
        returnQuery = "select count(*) ${returnQuery}"
        return returnQuery
    }
}

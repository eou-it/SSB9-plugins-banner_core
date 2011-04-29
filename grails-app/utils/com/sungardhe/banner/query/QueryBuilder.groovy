package com.sungardhe.banner.query

/** *****************************************************************************
 © 2010 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
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

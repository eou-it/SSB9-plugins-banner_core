/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.query.operators

import net.hedtech.banner.query.criteria.CriteriaData

class NotEqualsIgnoreCaseOperator extends CriteriaOperator {

    public NotEqualsIgnoreCaseOperator() {
        this.label = "net.hedtech.banner.ui.zk.search.advancedSearch.operator.notequals";
        this.operator = "!=";
        this.key = Operators.NOT_EQUALS_IGNORE_CASE
    }

    public String getQueryString(CriteriaData data) {
        if(data.params && data.params.size() > 0) {
           return "lower(${data.tableAlias}.${data.tableBindingAttribute}) != lower(:${data.paramKey})"
        }

    }
}

/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.query.operators

import net.hedtech.banner.query.criteria.CriteriaData

class LessThanOperator extends CriteriaOperator {
    public LessThanOperator() {
        this.label = "net.hedtech.banner.ui.zk.search.advancedSearch.operator.lessthan";
        this.operator = "<";
        this.key = Operators.LESS_THAN
    }

    public String getQueryString(CriteriaData data) {
       if(data.params && data.params.size() > 0) {
           return "${data.tableAlias}.${data.tableBindingAttribute} < :${data.paramKey}"
       }
       return ""
    }
}

/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.query.operators

import net.hedtech.banner.query.criteria.CriteriaData

class NotEqualsOperator extends CriteriaOperator {

    public NotEqualsOperator() {
        this.label = "net.hedtech.banner.ui.zk.search.advancedSearch.operator.notequals";
        this.operator = "!=";
        this.key = Operators.NOT_EQUALS
    }

    public String getQueryString(CriteriaData data) {
        if(data.params && data.params.size() > 0) {
            return "${data.tableAlias}.${data.tableBindingAttribute} != :${data.paramKey}"
        }

    }
}

package net.hedtech.banner.query.operators

import net.hedtech.banner.query.criteria.CriteriaData

class GreaterThanEqualsOperator extends CriteriaOperator {
    public GreaterThanEqualsOperator() {
        this.label = "net.hedtech.banner.ui.zk.search.advancedSearch.operator.greaterthanequals";
        this.operator = ">=";
        this.key = Operators.GREATER_THAN_EQUALS
    }

    public String getQueryString(CriteriaData data) {
       if(data.params && data.params.size() > 0) {
          return "${data.tableAlias}.${data.tableBindingAttribute} >= :${data.paramKey}"
       }
       return ""
    }
}
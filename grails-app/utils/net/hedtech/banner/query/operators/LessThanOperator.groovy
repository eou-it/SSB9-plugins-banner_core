package net.hedtech.banner.query.operators

import net.hedtech.banner.query.criteria.CriteriaData
import net.hedtech.banner.query.criteria.CriteriaParam

class LessThanOperator extends CriteriaOperator {
    public LessThanOperator() {
        this.label = "net.hedtech.banner.ui.zk.search.advancedSearch.operator.lessthan";
        this.operator = "<";
        this.key = Operators.LESS_THAN
    }

    public String getQueryString(CriteriaData data) {
       if(data.params && data.params.size() > 0) {
           CriteriaParam param = data.params.get(0);

           return "${data.tableAlias}.${data.tableBindingAttribute} < :${data.paramKey}"
       }
       return ""
    }
}
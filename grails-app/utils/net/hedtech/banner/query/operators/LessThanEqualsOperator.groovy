package net.hedtech.banner.query.operators

import net.hedtech.banner.query.criteria.CriteriaData
import net.hedtech.banner.query.criteria.CriteriaParam

class LessThanEqualsOperator extends CriteriaOperator {
    public LessThanEqualsOperator() {
        this.label = "net.hedtech.banner.ui.zk.search.advancedSearch.operator.lessthanequals";
        this.operator = "<=";
        this.key = Operators.LESS_THAN_EQUALS
    }

    public String getQueryString(CriteriaData data) {
       if(ifParamsExists(data)) {
           CriteriaParam param = data.params.get(0);

           if(param.data instanceof Date) {
             return new DateLessThanEqualsOperator().getQueryString(data)
           }

           return "${data.tableAlias}.${data.tableBindingAttribute} <= :${data.paramKey}"
       }
       return ""
    }
}
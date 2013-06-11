package net.hedtech.banner.query.operators

import net.hedtech.banner.query.criteria.CriteriaData
import net.hedtech.banner.query.criteria.CriteriaParam

class GreaterThanOperator extends CriteriaOperator {
    public GreaterThanOperator() {
        this.label = "net.hedtech.banner.ui.zk.search.advancedSearch.operator.greaterthan";
        this.operator = ">";
        this.key = Operators.GREATER_THAN
    }

    public String getQueryString(CriteriaData data) {
       if(data.params && data.params.size() > 0) {
           CriteriaParam param = data.params.get(0);

           if(param.data instanceof Date) {
               return new DateGreaterThanOperator().getQueryString(data)
           }
           return "${data.tableAlias}.${data.tableBindingAttribute} > :${data.paramKey}"
       }
       return ""
    }
}
package net.hedtech.banner.newquery.operators

import net.hedtech.banner.newquery.CriteriaData
import net.hedtech.banner.newquery.CriteriaParam

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
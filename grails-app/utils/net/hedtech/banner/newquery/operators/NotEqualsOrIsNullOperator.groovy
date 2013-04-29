package net.hedtech.banner.newquery.operators

import net.hedtech.banner.newquery.CriteriaData
import net.hedtech.banner.newquery.CriteriaParam

class NotEqualsOrIsNullOperator extends CriteriaOperator {
    public NotEqualsOrIsNullOperator() {
        this.label = "net.hedtech.banner.ui.zk.search.advancedSearch.operator.notequalsorisnull";
        this.operator = "!=";
        this.key = Operators.NOT_EQUALS_OR_IS_NULL
    }

    public String getQueryString(CriteriaData data) {
       if(data.params && data.params.size() > 0) {
           CriteriaParam param = data.params.get(0);
           return "${data.tableAlias}.${data.tableBindingAttribute} != :${data.paramKey} OR ${data.tableAlias}.${data.tableBindingAttribute} IS NULL)"
       }
       return ""
    }
}

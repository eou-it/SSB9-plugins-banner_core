package net.hedtech.banner.query.operators

import net.hedtech.banner.query.criteria.CriteriaData
import net.hedtech.banner.query.criteria.CriteriaParam

class EqualsOrIsNullOperator extends CriteriaOperator {
    public EqualsOrIsNullOperator() {
        this.label = "net.hedtech.banner.ui.zk.search.advancedSearch.operator.equalsorisnull";
        this.operator = "=";
        this.key = Operators.EQUALS_OR_IS_NULL
    }

    public String getQueryString(CriteriaData data) {
       if(data.params && data.params.size() > 0) {
           CriteriaParam param = data.params.get(0);
           return "${data.tableAlias}.${data.tableBindingAttribute} = :${data.paramKey} OR ${data.tableAlias}.${data.tableBindingAttribute} IS NULL)"
       }
       return ""
    }
}
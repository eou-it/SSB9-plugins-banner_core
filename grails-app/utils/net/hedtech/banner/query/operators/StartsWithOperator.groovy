package net.hedtech.banner.query.operators

import net.hedtech.banner.query.criteria.CriteriaData
import net.hedtech.banner.query.criteria.CriteriaParam

class StartsWithOperator extends CriteriaOperator {
    public StartsWithOperator() {
        this.label = "net.hedtech.banner.ui.zk.search.advancedSearch.operator.startswith";
        this.operator = "like";
        this.key = Operators.STARTS_WITH
    }

    public String getQueryString(CriteriaData data) {
       if(data.params && data.params.size() > 0) {
           CriteriaParam param = data.params.get(0);
           return "lower(${data.tableAlias}.${data.tableBindingAttribute}) like lower(:${data.paramKey})"
       }
       return ""
    }

    public Object formatValue(Object value) {
        return "${value}%";
    }
}


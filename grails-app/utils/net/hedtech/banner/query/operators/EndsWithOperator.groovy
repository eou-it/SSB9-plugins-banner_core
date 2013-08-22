package net.hedtech.banner.query.operators

import net.hedtech.banner.query.criteria.CriteriaData
import net.hedtech.banner.query.criteria.CriteriaParam

class EndsWithOperator extends CriteriaOperator {
    public EndsWithOperator() {
        this.label = "net.hedtech.banner.ui.zk.search.advancedSearch.operator.endswith";
        this.operator = "like";
        this.key = Operators.ENDS_WITH
    }

    public String getQueryString(CriteriaData data) {
       if(data.params && data.params.size() > 0) {
           CriteriaParam param = data.params.get(0);
           return "lower(${data.tableAlias}.${data.tableBindingAttribute}) like lower(:${data.paramKey})"
       }
       return ""
    }

    public Object formatValue(Object value) {
        return "%${value}";
    }
}

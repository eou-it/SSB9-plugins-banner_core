package net.hedtech.banner.query.operators

import net.hedtech.banner.query.criteria.CriteriaData
import net.hedtech.banner.query.criteria.CriteriaParam

class ContainsOperator extends CriteriaOperator {
     public ContainsOperator() {
        this.label = "net.hedtech.banner.ui.zk.search.advancedSearch.operator.contains";
        this.operator = "like";
        this.key = Operators.CONTAINS;
    }
    
    public String getQueryString(CriteriaData data) {
        if(data.params.size() > 0) {
            CriteriaParam param1 = data.params.get(0);
            return "lower(${data.tableAlias}.${data.tableBindingAttribute}) like lower(:${param1.paramKey})"
        }
        return ""
    }

    public Object formatValue(Object value) {
       return "%${value}%";
    }
}